package com.ray.standardserver.binder;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.SparseArray;
import com.example.loglib.KLog;
import com.ray.standardsdk.ISdk;

import static com.ray.standardsdk.SdkManager.SERVICE_AUDIO;
import static com.ray.standardsdk.SdkManager.SERVICE_INFO;

/**
 * @author ray
 * @date 2023/8/16 17:07
 */
public class MainBinder extends ISdk.Stub{

    private final String TAG = "MainBinder";
    private final SparseArray<IBinder> mCache = new SparseArray<>();
    private final Object mLock = new Object();
    private final Context mContext;

    public MainBinder(final Context context) {
        mContext = context;
    }


    @Override
    public IBinder getService(int serviceType) throws RemoteException {
        KLog.i(TAG, "getService: serviceType = " + serviceType + ", UID: "+ android.os.Process.myUid() + ", PID: " + android.os.Process.myPid());
        IBinder binder = mCache.get(serviceType);
        if(binder != null){
            return binder;
        }
        synchronized (mLock) {
            switch (serviceType) {
                case SERVICE_AUDIO:
                    binder = new AudioBinder(mContext);
                    break;
                case SERVICE_INFO:
                    binder = new InfoBinder();
                    break;
            }
            mCache.put(serviceType, binder);
        }
        return binder;
    }
}
