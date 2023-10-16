package com.example.myview.nimblegums;

/**
 * @author ray
 * @date 2023/9/28 16:39
 */
public interface NimBleGumInterface {

    interface OnChangerListener {
        void onShow(NimBleGumInterface nbg, int state);

        void onDismiss(NimBleGumInterface nbg, int state);

        void onConvert(NimBleGumInterface nbg, int state);

        void onSlideDown(NimBleGumInterface nbg);

        void onUserLeave(NimBleGumInterface nbg);

        void onUserOperate(NimBleGumInterface nbg);

        void onOperateEvent(NimBleGumInterface nbg, NimbleGumOperateEvent operateEvent);

        void onUiModeChanged(NimBleGumInterface nbg, boolean night);
    }

    interface OnShowListener {
        void onShow(NimBleGumInterface nbg, int state);
    }

    interface OnDismissListener {
        void onDismiss(NimBleGumInterface nbg, int state);
    }

    interface OnConvertListener {
        void onConvert(NimBleGumInterface nbg, int state);
    }

    interface OnSlideDownListener {
        void onSlideDown(NimBleGumInterface nbg);
    }

    interface OnUserLeaveListener {
        void onUserLeave(NimBleGumInterface nbg);
    }

    interface OnUserOperateListener {
        void onUserOperate(NimBleGumInterface nbg);
    }

    interface OnOperateEventListener {
        void onOperateEvent(NimBleGumInterface nbg, NimbleGumOperateEvent operateEvent);
    }

    interface OnUiModeChangerListener {
        void onUiModeChanged(NimBleGumInterface nbg, boolean night);
    }

    void addOnShowListener(OnShowListener listener);

    void removeOnShowListener();

    void addOnDismissListener(OnDismissListener listener);

    void removeOnDismissListener();

    void addOnConvertListener(OnConvertListener listener);

    void removeOnConvertListener();

    void addOnSlideDownListener(OnSlideDownListener listener);

    void removeOnSlideDownListener();

    void addOnUserLeaveListener(OnUserLeaveListener listener);

    void removeOnUserLeaveListener();

    void addOnUserOperateListener(OnUserOperateListener listener);

    void removeOnUserOperateListener();

    void addOnOperateEventListener(OnOperateEventListener listener);

    void removeOnOperateEventListener();

    void addOnUiModeChangerListener(OnUiModeChangerListener listener);

    void removeOnUiModeChangerListener();

    void addOnChangerListener(OnChangerListener listener);

    void removeOnChangerListener();

    void removeOnChangerListeners(OnChangerListener listener);

    void cancel();

    boolean isShowing();

}
