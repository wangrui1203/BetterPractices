package com.ray.standardsdk.base;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;

/**
 * @author ray
 * @date 2023/8/4 13:20
 * 服务端提供的业务逻辑的基类
 */
public abstract class SdkManagerBase {
    public static final String TAG = "CAR.SERVICE";

    protected final SdkBase mSdk;

    public SdkManagerBase(SdkBase sdk) {
        mSdk = sdk;
    }

    protected Context getContext() {
        return mSdk.getContext();
    }

    protected Handler getEventHandler() {
        return mSdk.getEventHandler();
    }

    protected <T> T handleRemoteExceptionFromService(RemoteException e, T returnValue) {
        return (T) mSdk.handleRemoteExceptionFromService(e, returnValue);
    }

    protected void handleRemoteExceptionFromService(RemoteException e) {
        mSdk.handleRemoteExceptionFromService(e);
    }

    /**
     * Handle disconnection of car service (=crash). As car service has crashed already, this call
     * should only clean up any listeners / callbacks passed from client. Clearing object passed
     * to car service is not necessary as car service has crashed. Note that SdkManager*Manager will not
     * work any more as all binders are invalid. Client should re-create all SdkManager*Managers when
     * car service is restarted.
     */
    protected abstract void onDisconnected();
}
