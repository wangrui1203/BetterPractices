package com.ray.standardsdk.base;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Process;
import android.util.Log;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.util.HashMap;

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

    // 将服务端的 Binder 对象转换为客户端的具体对象。
    protected abstract T asInterface(IBinder binder);
    protected abstract String getLogTag();

    /*****************************对外暴露的方法********************************/

    public interface SdkServiceLifecycleListener<T extends SdkBase> {
        void onLifecycleChanged(@NonNull T sdk, boolean ready);
    }

}
