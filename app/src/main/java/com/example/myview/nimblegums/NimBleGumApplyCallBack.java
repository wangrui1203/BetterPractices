package com.example.myview.nimblegums;

/**
 * @author ray
 * @date 2023/9/28 17:20
 */
public interface NimBleGumApplyCallBack {
    void onShow(int state);
    void onDismiss(int state);
    void onConvert(int state);
    void onSlideDown();
    void onUserLeave();
    void onUserOperate();
    void onOperateEvent(NimbleGumOperateEvent operateEvent);
    void onUiModeChanged(boolean night);
}
