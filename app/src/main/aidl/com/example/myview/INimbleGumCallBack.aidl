// INimbleGumCallBack.aidl
package com.ray.mysdk;

import com.ray.mysdk.nimblegums.NimbleGumOperateEvent;
// Declare any non-default types here with import statements

interface INimbleGumCallBack {
    void onShow(int state);
    void onDismiss(int state);
    void onConvert(int state);
    void onUserLeave();
    void onUserOperate();
    void onOperateEvent(in NimbleGumOperateEvent operateEvent);
    void onSlideDown();
    void onUiModeChanged(in boolean night);
}