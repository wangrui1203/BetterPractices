package com.ray.standardapp;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.loglib.KLog;
import com.ray.standardsdk.SdkManager;
import com.ray.standardsdk.audio.AudioSdkManager;
import com.ray.standardsdk.base.SdkBase;

import java.util.concurrent.CountDownLatch;

/**
 * 用于加载音频数据的DataLoader
 * 在MVVM架构中属于Model层的组成部分之一
 * @author ray
 * @date 2023/8/17 13:31
 */
public class AudioDataLoader {
    private SdkManager mSdkManager;
    private AudioSdkManager mAudioManager;
    // 同步锁。将异步的Service的连接，改为同步的。
    private CountDownLatch mAudioManagerReady;

    public AudioDataLoader() {
        mAudioManagerReady = new CountDownLatch(1);
        mSdkManager = SdkManager.get(new SdkBase.SdkServiceLifecycleListener<SdkManager>() {
            @Override
            public void onLifecycleChanged(@NonNull final SdkManager sdk, final boolean ready) {
                if (ready) {
                    mAudioManager = sdk.getService(AudioSdkManager.class);
                    mAudioManager.registerAudioCallback(mAudioCallback);
                    mAudioManagerReady.countDown();
                } else {
                    if (mAudioManagerReady.getCount() <= 0) {
                        mAudioManagerReady = new CountDownLatch(1);
                    }
                    mAudioManager = null;
                    // 重新连接
                    sdk.connect();
                }
            }
        });
    }

    private final AudioSdkManager.AudioCallback mAudioCallback = new AudioSdkManager.AudioCallback() {
        @Override
        public void onAudioData(final byte[] data, final int length) {

        }
    };

    public void play() {
        // 实际应该放入线程池中执行
        new Thread(() -> {
            try {
                mAudioManagerReady.await();
            } catch (InterruptedException e) {
                return;
            }
            mAudioManager.play();
            KLog.i("TAG", "play 执行完毕");
        }).start();
    }

    private MutableLiveData<Long> mDurationData;

    public LiveData<Long> getDuration() {
        // 实际应该放入线程池中执行
        new Thread(() -> {
            try {
                mAudioManagerReady.await();
            } catch (InterruptedException e) {
                getDurationData().postValue(0L);
            }
            getDurationData().postValue(mAudioManager.getDuration());
        }).start();
        return getDurationData();
    }

    public void release() {
        mAudioManager.unregisterAudioCallback(mAudioCallback);
        mSdkManager.disconnect();
        mSdkManager = null;
        mAudioManager = null;
    }

    private MutableLiveData<Long> getDurationData() {
        if (mDurationData == null) {
            mDurationData = new MutableLiveData<>();
        }
        return mDurationData;
    }
}
