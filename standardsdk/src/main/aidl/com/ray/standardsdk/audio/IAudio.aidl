// IAudio.aidl
package com.ray.standardsdk.audio;

import com.ray.standardsdk.audio.IAudioCallback;

interface IAudio {

    void play();

    long getDuration();

    void registerAuidoCallback(IAudioCallback callback);
    void unregisterAudioCallback(IAudioCallback callback);
}