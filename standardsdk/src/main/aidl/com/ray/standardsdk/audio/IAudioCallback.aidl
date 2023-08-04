// IAudio.aidl
package com.ray.standardsdk.audio;

import com.ray.standardsdk.audio.IAudioCallback;

interface IAudioCallback {
      void onAudioData(in byte[] data, int length);
}