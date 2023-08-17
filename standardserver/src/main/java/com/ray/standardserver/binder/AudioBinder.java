package com.ray.standardserver.binder;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.example.loglib.KLog;
import com.ray.standardsdk.SdkManager;
import com.ray.standardsdk.audio.IAudio;
import com.ray.standardsdk.audio.IAudioCallback;

/**
 * @author ray
 * @date 2023/8/16 17:07
 */
public class AudioBinder extends IAudio.Stub {

    private final RemoteCallbackList<IAudioCallback> mCallbacks;
    private final Context mContext;
    private final String TAG = "AudioBinder";

    public AudioBinder(Context context) {
        mContext = context;
        mCallbacks = new RemoteCallbackList<>();
    }

    @Override
    public void play() throws RemoteException {
        KLog.i(TAG, "play() called, UID: " + android.os.Process.myUid() + ", PID: " + android.os.Process.myPid());
        mContext.enforceCallingPermission(SdkManager.PERMISSION_AUDIO, "no permission");
    }

    @Override
    public long getDuration() throws RemoteException {
        KLog.i(TAG, "getDuration() called, UID: " + android.os.Process.myUid() + ", PID: " + android.os.Process.myPid());
        mContext.enforceCallingPermission(SdkManager.PERMISSION_AUDIO, "no permission");
        return 1222;
    }

    @Override
    public void registerAuidoCallback(final IAudioCallback callback) throws RemoteException {
        KLog.i(TAG, "registerAuidoCallback() called, UID: " + android.os.Process.myUid() + ", PID: " + android.os.Process.myPid());
        mContext.enforceCallingPermission(SdkManager.PERMISSION_AUDIO, "no permission");
        mCallbacks.register(callback);
    }

    @Override
    public void unregisterAudioCallback(final IAudioCallback callback) throws RemoteException {
        KLog.i(TAG, "unregisterAudioCallback() called, UID: " + android.os.Process.myUid() + ", PID: " + android.os.Process.myPid());
        mContext.enforceCallingPermission(SdkManager.PERMISSION_AUDIO, "no permission");
        mCallbacks.unregister(callback);
    }
}
