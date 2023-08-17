package com.ray.standardsdk;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ray.standardsdk.audio.AudioSdkManager;
import com.ray.standardsdk.audio.InfoSdkManager;
import com.ray.standardsdk.base.SdkBase;
import com.ray.standardsdk.base.SdkManagerBase;

import java.util.HashMap;

/**
 * @author ray
 * @date 2023/8/11 17:50
 * 将AIDL封装成SDK提供给外部使用。在封装SDK时一般需要遵守以下原则：
 * 简化「客户端」的调用成本
 * 隐藏Service重连机制，使调用方无需关心Service重连的具体实现
 * 减少「客户端」与「服务端」的不必要的通信次数，提高性能
 * 根据需要进行权限验证
 *
 * SdkManager管理服务端的功能，包括信息、音频等。用于对于客户端提供统一的入口，展示如何使用SdkBase
 */
public class SdkManager extends SdkBase<ISdk> {

    public static final String PERMISSION_AUDIO = "com.ray.standardsdk.permission.AUDIO";

    private static final String SERVICE_PACKAGE = "com.ray.standardserver";
    private static final String SERVICE_CLASS = "com.ray.standardserver.StandardService";
    private static final String SERVICE_ACTION = "android.intent.action.STANDARD_SERVICE";

    public static final int SERVICE_AUDIO = 0x1001;
    public static final int SERVICE_INFO = 0x1002;

    private static final long SERVICE_BIND_RETRY_INTERVAL_MS = 500;
    private static final long SERVICE_BIND_MAX_RETRY = 100;

    /**
     * 创建一个Manager对象
     * 是否需要设定为单例，由开发者自行决定
     * @param context 上下文
     * @param handler 用于处理服务端回调的Handler
     * @param listener 用于监听服务端生命周期的listener
     * @return SdkASyncManager
     */
    public static SdkManager get(Context context, Handler handler, SdkBase.SdkServiceLifecycleListener<SdkManager> listener){
        return new SdkManager(context, handler, listener);
    }
    public static SdkManager get() {
        return new SdkManager(null, null, null);
    }

    public static SdkManager get(Context context) {
        return new SdkManager(context, null, null);
    }

    public static SdkManager get(Handler handler) {
        return new SdkManager(null, handler, null);
    }

    public static SdkManager get(SdkServiceLifecycleListener<SdkManager> listener) {
        return new SdkManager(null, null, listener);
    }

    public SdkManager(@Nullable final Context context, @Nullable final Handler handler, @Nullable final SdkServiceLifecycleListener<SdkManager> listener){
        super(context, handler, listener);
    }


    @Override
    protected String getServicePackage() {
        return SERVICE_PACKAGE;
    }

    @Override
    protected String getServiceClassName() {
        return SERVICE_CLASS;
    }

    @Override
    protected String getServiceAction() {
        return SERVICE_ACTION;
    }

    @Override
    protected ISdk asInterface(final IBinder binder) {
        return ISdk.Stub.asInterface(binder);
    }

    @Override
    protected boolean needStartService() {
        return false;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected long getConnectionRetryCount() {
        return SERVICE_BIND_MAX_RETRY;
    }

    @Override
    protected long getConnectionRetryInterval() {
        return SERVICE_BIND_RETRY_INTERVAL_MS;
    }

    public static final String TAG = "CAR.SERVICE";
    public <T extends SdkManagerBase> T getService(@NonNull Class<T> serviceClass) {
        Log.i(TAG, "getService: "+serviceClass.getSimpleName());
        SdkManagerBase manager;
        // 涉及 managerMap 的操作，需要加锁
        synchronized (getLock()) {
            HashMap<Integer, SdkManagerBase> managerMap = getManagerCache();
            if (mService == null) {
                Log.w(TAG, "getService not working while car service not ready");
                return null;
            }
            int serviceType = getSystemServiceType(serviceClass);
            manager = managerMap.get(serviceType);
            if (manager == null) {
                try {
                    IBinder binder = mService.getService(serviceType);
                    if (binder == null) {
                        Log.w(TAG, "getService could not get binder for service:" + serviceType);
                        return null;
                    }
                    manager = createCarManagerLocked(serviceType, binder);
                    if (manager == null) {
                        Log.w(TAG, "getService could not create manager for service:" + serviceType);
                        return null;
                    }
                    managerMap.put(serviceType, manager);
                } catch (RemoteException e) {
                    handleRemoteExceptionFromService(e);
                }
            }
        }
        return (T) manager;
    }

    private int getSystemServiceType(@NonNull Class<?> serviceClass) {
        switch (serviceClass.getSimpleName()) {
            case "AudioSdkManager":
                return SERVICE_AUDIO;
            case "InfoSdkManager":
                return SERVICE_INFO;
            default:
                return -1;
        }
    }

    @Nullable
    private SdkManagerBase createCarManagerLocked(int serviceType, IBinder binder) {
        SdkManagerBase manager = null;
        switch (serviceType) {
            case SERVICE_AUDIO:
                manager = new AudioSdkManager(this, binder);
                break;
            case SERVICE_INFO:
                manager = new InfoSdkManager(this, binder);
                break;
            default:
                // Experimental or non-existing
                break;
        }
        return manager;
    }
}
