package com.ray.standardsdk.audio;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.RequiresPermission;
import com.ray.standardsdk.SdkManager;
import com.ray.standardsdk.base.SdkBase;
import com.ray.standardsdk.base.SdkManagerBase;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 一个使用示例：音频管理类
 * 展示如何使用SdkManagerBase
 * @author ray
 * @date 2023/8/4 13:25
 */
public class AudioSdkManager extends SdkManagerBase {

    private final IAudio mService;
    private final CopyOnWriteArrayList<AudioCallback> mCallbacks;

    public AudioSdkManager(SdkBase sdk, IBinder binder) {
        super(sdk);
        mService = IAudio.Stub.asInterface(binder);
        mCallbacks = new CopyOnWriteArrayList<>();
    }

    private final IAudioCallback.Stub mCallbackImpl = new IAudioCallback.Stub() {
        @Override
        public void onAudioData(byte[] data, int length) throws RemoteException {
            for (AudioCallback callback : mCallbacks) {
                callback.onAudioData(data, length);
            }
        }
    };

    // 提示需要权限
    @RequiresPermission(SdkManager.PERMISSION_AUDIO)
    public void play() {
        try {
            mService.play();
        } catch (RemoteException e) {
            Log.e(TAG, "play: " + e);
            handleRemoteExceptionFromService(e);
        }
    }

    public long getDuration() {
        try {
            return mService.getDuration();
        } catch (RemoteException e) {
            return handleRemoteExceptionFromService(e, 0);
        }
    }


    public void registerAudioCallback(AudioCallback callback) {
        Objects.requireNonNull(callback);
        if (mCallbacks.isEmpty()) {
            registerCallback();
        }
        mCallbacks.add(callback);
    }

    public void unregisterAudioCallback(AudioCallback callback) {
        Objects.requireNonNull(callback);
        if (mCallbacks.remove(callback) && mCallbacks.isEmpty()) {
            unregisterCallback();
        }
    }
    /************* 内部方法 *************/
    /**
     * 向服务端注册回调
     */
    private void registerCallback() {
        try {
            mService.registerAuidoCallback(mCallbackImpl);
        } catch (RemoteException e) {
            Log.e(TAG, "registerAudioCallback: " + e);
            handleRemoteExceptionFromService(e);
        }
    }

    /**
     * 取消注册回调
     */
    private void unregisterCallback() {
        try {
            mService.unregisterAudioCallback(mCallbackImpl);
        } catch (RemoteException e) {
            Log.e(TAG, "unregisterAudioCallback: " + e);
            handleRemoteExceptionFromService(e);
        }
    }

    @Override
    protected void onDisconnected() {

    }


    public abstract static class AudioCallback {

        public void onAudioData(byte[] data, int length) {

        }

    }
}
