// INimbleGumSmartComponentService.aidl
package com.ray.mysdk;

import android.os.Bundle;
import com.ray.mysdk.nimblegums.NimBleGumApply;
import com.ray.mysdk.INimbleGumCallBack;
// Declare any non-default types here with import statements

interface INimbleGumSmartComponentService {

    void onSmartComponent(in String type, in Bundle args);
    void onSmartComponentCallBack(in NimBleGumApply nbg, in INimbleGumCallBack cb);
    void cancelGum(in NimBleGumApply nbg);
    void updateGum(in NimBleGumApply nbg);
    boolean isShowing(in NimBleGumApply nbg);
}