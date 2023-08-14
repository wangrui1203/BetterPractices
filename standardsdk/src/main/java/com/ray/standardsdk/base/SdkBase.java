package com.ray.standardsdk.base;

import android.app.Activity;
import android.app.Service;
import android.content.*;
import android.os.*;
import android.os.Process;
import android.util.Log;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ray.standardsdk.exception.TransactionException;
import kotlin.KotlinNullPointerException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author ray
 * @date 2023/8/4 11:47
 * 为了让子类能够更方便地实现与服务端的连接。
 * 内部实现Service重连机制，并对外暴露connect()、disconnect()、isConnected()等方法
 * https://juejin.cn/post/7236009756530933819?searchId=202308040923021AF10BB194202B6716F8
 */
public abstract class SdkBase<T extends IInterface> {
    //@interface为注解类，jdk1.5之后用于自定义注解
    //保留的环境
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {
            StateType.STATE_UNKNOW,
            StateType.STATE_DISCONNECTED,
            StateType.STATE_CONNECTING,
            StateType.STATE_CONNECTED
    })
    //注释起作用的位置
    @Target({ElementType.TYPE_USE})
    public @interface StateType{
        int STATE_UNKNOW = -1;
        int STATE_DISCONNECTED = 0;
        int STATE_CONNECTING = 1;
        int STATE_CONNECTED = 2;
    }
    protected T mService;
    private final Object mLock = new Object();
    private final Context mContext;
//
//    //用于处理连接事务的Handler，可以由外部决定是否运行在子线程中
    private final Handler mEventHandler;
//    //用于onServiceDisconnected的回调，需要发到主线程里，所以需要一个主线程的Handler来发送消息
    private final Handler mMainThreadEventHandler;
    private final SdkServiceLifecycleListener mLifecycleChangeListener;

    //当前连接状态
    @StateType
    private int mConnectionState = StateType.STATE_UNKNOW;

    //缓存的服务端Binder对象
    private final HashMap<Integer, SdkManagerBase> mServiceMap = new HashMap<>();

    private final Exception mConstructionStack;

    //连接
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (mLock){
                Log.i(getLogTag(), "onServiceConnected: " + name);
                T newService = asInterface(service);
                if(newService == null){
                    Log.w(getLogTag(), "null binder service", new RuntimeException());
                    return;
                }
                if(mService != null && mService.asBinder().equals(newService.asBinder())){
                    //已经连接成功
                    return;
                }
                mConnectionState = StateType.STATE_CONNECTED;
                mService = newService;
            }
            if(mLifecycleChangeListener != null){
                mLifecycleChangeListener.onLifecycleChanged(SdkBase.this, true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            synchronized (mLock){
                Log.w(getLogTag(),"onServiceDisconnected: "+ name);
                if(mConnectionState == StateType.STATE_DISCONNECTED){
                    //当客户端在onServiceDisconnected调用之前调用disconnect时，可能会发生这种情况为disconnected
                    return;
                }
                //释放与更新状态
                handleCarDisconnectLocked();
            }
            if(mLifecycleChangeListener != null){
                mLifecycleChangeListener.onLifecycleChanged(SdkBase.this, false);
            }else{
                //客户端没有监听Service的连接状态，此时直接杀死客户端
                finishClient();
            }
        }
    };

    public SdkBase(@Nullable Context context, @Nullable Handler handler, @Nullable SdkServiceLifecycleListener listener){
        if(context == null){
            mContext = SdkAppGlobal.getApplication();
        }else {
            mContext = context;
        }
        assertNonNullContent(mContext);
        mEventHandler = determineEventHandler(handler);
        mMainThreadEventHandler = determineMainThreadEventHandler(mEventHandler);
        mLifecycleChangeListener = listener;
        if(listener == null){
            mConstructionStack = new RuntimeException();
        }else {
            mConstructionStack = null;
        }
        startConnect();
    }

    private final Runnable mConnectionRetryRunnable = new Runnable() {
        @Override
        public void run() {
            startConnect();
        }
    };

    private final Runnable mConnectionRetryFailedRunnable = new Runnable() {
        @Override
        public void run() {
            mServiceConnection.onServiceDisconnected(new ComponentName(getServicePackage(), getServiceClassName()));
        }
    };
    private int mConnectionRetryCount = 0;
    private boolean mServiceBound = false;

    private void startConnect() {
        ComponentName componentName = new ComponentName(getServicePackage(), getServiceClassName());
        Intent intent = new Intent();
        if(getServiceAction() != null){
            intent.setAction(getServiceAction());
        }
        intent.setComponent(componentName);
        //是否需要启动Service
        if(needStartService()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(intent);
            } else {
                getContext().startService(intent);
            }
        }
        // 绑定Service
        boolean bound = getContext().bindService(intent, mServiceConnection,
                Context.BIND_AUTO_CREATE);
        // 如果需要考虑多用户的问题，可以使用下面的系统方法
//        boolean bound = getContext().bindServiceAsUser(intent, mServiceConnection,
//                Context.BIND_AUTO_CREATE, UserHandle.CURRENT_OR_SELF);
        Log.w(getLogTag(), "startConnect: [bindService] result " + bound);
        // 进入重连状态
        synchronized (mLock) {
            if (!bound) {
                mConnectionRetryCount++;
                if (mConnectionRetryCount > getConnectionRetryCount()) {
                    // 多次重连失败
                    Log.w(getLogTag(), "cannot bind to service after max retry :" + getServiceClassName());
                    mMainThreadEventHandler.post(mConnectionRetryFailedRunnable);
                } else {
                    Log.w(getLogTag(), "cannot bind to service retry ->" + getServiceClassName() + ";" + mConnectionRetryCount + " times");
                    mEventHandler.postDelayed(mConnectionRetryRunnable, getConnectionRetryInterval());
                }
            } else {
                mEventHandler.removeCallbacks(mConnectionRetryRunnable);
                mMainThreadEventHandler.removeCallbacks(mConnectionRetryFailedRunnable);
                mConnectionRetryCount = 0;
                mServiceBound = true;
            }
        }
    }

    private Handler determineMainThreadEventHandler(Handler mEventHandler) {
        Looper mainLooper = Looper.getMainLooper();
        return (mEventHandler.getLooper() == mainLooper) ? mEventHandler : new Handler(mainLooper);
    }

    private Handler determineEventHandler(@Nullable Handler handler) {
        if(handler == null){
            Looper looper = Looper.getMainLooper();
            handler = new Handler(looper);
        }
        return handler;
    }

    //继承关系：
    //Context<-ContextImpl
    //Context<-ContextWrapper<-Application/Service/ContextThemeWrapper
    //ContextThemeWrapper <- Activity
    private void assertNonNullContent(Context context){
        Objects.requireNonNull(context);
        if(context instanceof ContextWrapper
                && ((ContextWrapper) context).getBaseContext() == null){
            throw new KotlinNullPointerException(
                    "ContextWrapper with null base as Context"
            );
        }
    }

    //释放与更新状态
    private void handleCarDisconnectLocked() {
        if (mConnectionState == StateType.STATE_DISCONNECTED) {
            // 当客户端在已回调onServiceDisconnected的情况下调用disconnect时，可能会发生这种情况。
            return;
        }
        mEventHandler.removeCallbacks(mConnectionRetryRunnable);
        mMainThreadEventHandler.removeCallbacks(mConnectionRetryFailedRunnable);
        mConnectionRetryCount = 0;
        //交给内部清理缓存的binder （service）
        teardownManagersBaseLocked();
        mService = null;
        mConnectionState = StateType.STATE_DISCONNECTED;
    }

    private void teardownManagersBaseLocked() {
        // 所有断开连接的处理都应该只进行内部清理。
        for (SdkManagerBase manager : mServiceMap.values()) {
            manager.onDisconnected();
        }
        mServiceMap.clear();
    }

    private void finishClient() {
        if (mContext == null) {
            throw new IllegalStateException("service has crashed, null Context");
        }
        //TODO
        if (mContext instanceof Activity) {
            Activity activity = (Activity) mContext;
            if (!activity.isFinishing()) {
                Log.w(getLogTag(),
                        "service crashed, client not handling it, finish Activity, created "
                                + "from " + mConstructionStack);
                activity.finish();
            }
        //
        } else if (mContext instanceof Service) {
            Service service = (Service) mContext;
            killClient(service.getPackageName() + "," + service.getClass().getSimpleName());
        } else {
            killClient(/* clientInfo= */ null);
        }

    }

    private void killClient(@Nullable String clientInfo) {
        Log.w(getLogTag(), "**service has crashed. Client(" + clientInfo + ") is not handling it."
                        + " Client should use XXX.get(..., ServiceLifecycleListener, .."
                        + ".) to handle it properly. Check pritned callstack to check where other "
                        + "version of XXX.get() was called. Killing the client process**",
                mConstructionStack);
        Process.killProcess(Process.myPid());
    }

    protected Object getLock() {
        return mLock;
    }

    /*****************************子类需要实现的方法********************************/

    // 服务端的包名
    protected abstract String getServicePackage();

    // 服务端Service的类名
    protected abstract String getServiceClassName();

    // 服务端的Action
    protected abstract String getServiceAction();

    // 将服务端的 Binder 对象转换为客户端的具体对象。
    protected abstract T asInterface(IBinder binder);

    protected abstract String getLogTag();

    // 获取本地Binder的缓存
    protected HashMap<Integer, SdkManagerBase> getManagerCache() {
        return mServiceMap;
    }

    // 是否需要调用 startService
    protected abstract boolean needStartService();

    // 获取连接失败时的重试次数
    protected abstract long getConnectionRetryCount();

    // 获取连接失败时的重试间隔，单位 ms
    protected abstract long getConnectionRetryInterval();

    /*****************************对外暴露的方法********************************/

    public Context getContext() {
        return mContext;
    }

    public Handler getEventHandler() {
        return mEventHandler;
    }

    public ServiceConnection getServiceConnection() {
        return mServiceConnection;
    }

    public boolean isConnected() {
        synchronized (mLock) {
            return mService != null;
        }
    }

    public boolean isConnecting() {
        synchronized (mLock) {
            return mConnectionState == StateType.STATE_CONNECTING;
        }
    }
    public void connect() {
        synchronized (mLock) {
            if (mConnectionState != StateType.STATE_DISCONNECTED) {
                Log.w(getLogTag(), "connect: already connected or connecting");
            }
            mConnectionState = StateType.STATE_CONNECTING;
            startConnect();
        }
    }

    public void disconnect() {
        synchronized (mLock) {
            handleCarDisconnectLocked();
            if (mServiceBound) {
                // unbindService 时 ServiceConnection 的 onServiceDisconnected 不会被回调
                mContext.unbindService(mServiceConnection);
                mServiceBound = false;
            }
        }
    }

    public <V> V handleRemoteExceptionFromService(RemoteException e, V returnValue) {
        handleRemoteExceptionFromService(e);
        return returnValue;
    }

    public void handleRemoteExceptionFromService(RemoteException e) {
        if (e instanceof TransactionTooLargeException) {
            Log.w(getLogTag(), "service threw TransactionTooLargeException");
            throw new TransactionException(e, "service threw TransactionTooLargeException");
        } else {
            Log.w(getLogTag(), "Car service has crashed", e);
        }
    }

    public interface SdkServiceLifecycleListener<T extends SdkBase> {
        void onLifecycleChanged(@NonNull T sdk, boolean ready);
    }

}
