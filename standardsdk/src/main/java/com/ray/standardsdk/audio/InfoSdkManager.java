package com.ray.standardsdk.audio;

import android.os.IBinder;
import com.ray.standardsdk.base.SdkBase;
import com.ray.standardsdk.base.SdkManagerBase;

/**
 * @author ray
 * @date 2023/8/4 13:25
 */
public class InfoSdkManager extends SdkManagerBase {
    public InfoSdkManager(SdkBase sdk, IBinder iBinder){
        super(sdk);
    }
    @Override
    protected void onDisconnected() {

    }
}
