package com.example.myview.nimblegums;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import com.ray.mysdk.INimbleGumCallBack;

import java.util.HashMap;

import static com.example.myview.nimblegums.NimBleGumDebugConfig.TAG_NBG_BASE_SERVICE;

/**
 * @author ray
 * @date 2023/9/28 17:12
 */
public abstract class NimbleGumSmartComponentBaseService extends Service {
    public static final String CATEGORY = "nio.intent.category.NIMBLE_GUM_SMART_COMPONENT";
    public static final String META_DATA_NAME = "NIMBLE_GUM_SMART_COMPONENT";
    private NimbleGumSmartComponentServiceIml mService;
    protected Context mContext;
    protected HashMap<Long, NimBleGum> mApplyNimBleGums;
    private final NimBleGumRemoteCallbackList mCallbacks = new NimBleGumRemoteCallbackList();
    private Handler mHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler(Looper.getMainLooper());
        mContext = this;
        mApplyNimBleGums = new HashMap<>();
        if (mService == null) {
            mService = new NimbleGumSmartComponentServiceIml();
        }
    }

    protected abstract void onSmartComponent(String type, Bundle args);

    protected NimBleGum onSmartComponentCallBack(NimBleGumApply nimBleGumApply, Listener listener) {
        return null;
    }

    protected void onSmartComponentChanger(long id) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mService == null) {
            mService = new NimbleGumSmartComponentServiceIml();
        }
        return mService;
    }

    class NimBleGumRemoteCallbackList extends RemoteCallbackList<INimbleGumCallBack> {
        @Override
        public void onCallbackDied(INimbleGumCallBack callback, Object cookie) {
            super.onCallbackDied(callback, cookie);
            Log.e(TAG_NBG_BASE_SERVICE, " onCallbackDied ");
            Long nimBleGumApply = (Long) cookie;
            mApplyNimBleGums.remove(nimBleGumApply);
        }
    }

    public class Listener implements NimBleGumInterface.OnChangerListener {
        private NimBleGumApply nimBleGumApply;

        public Listener(NimBleGumApply nimBleGumApply) {
            this.nimBleGumApply = nimBleGumApply;
        }

        @Override
        public void onShow(NimBleGumInterface nbg, int state) {
            int N = mCallbacks.beginBroadcast();
            while (N > 0) {
                N--;
                INimbleGumCallBack listener = mCallbacks.getBroadcastItem(N);
                Long cookie = (Long) mCallbacks.getBroadcastCookie(N);
                if (cookie == nimBleGumApply.id) {
                    try {
                        listener.onShow(state);
                    } catch (RemoteException e) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
        }

        @Override
        public void onDismiss(NimBleGumInterface nbg, int state) {
            int N = mCallbacks.beginBroadcast();
            INimbleGumCallBack unRegisterCallBack = null;
            while (N > 0) {
                N--;
                INimbleGumCallBack listener = mCallbacks.getBroadcastItem(N);
                Long cookie = (Long) mCallbacks.getBroadcastCookie(N);
                if (cookie == nimBleGumApply.id) {
                    try {
                        unRegisterCallBack = listener;
                        listener.onDismiss(state);
                        mApplyNimBleGums.remove(cookie);
                    } catch (RemoteException e) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
            if (unRegisterCallBack != null) {
                mCallbacks.unregister(unRegisterCallBack);
            }

        }

        @Override
        public void onConvert(NimBleGumInterface nbg, int state) {
            int N = mCallbacks.beginBroadcast();
            while (N > 0) {
                N--;
                INimbleGumCallBack listener = mCallbacks.getBroadcastItem(N);
                Long cookie = (Long) mCallbacks.getBroadcastCookie(N);
                if (cookie == nimBleGumApply.id) {
                    try {
                        listener.onConvert(state);
                    } catch (RemoteException e) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
        }

        @Override
        public void onSlideDown(NimBleGumInterface nbg) {
            int N = mCallbacks.beginBroadcast();
            while (N > 0) {
                N--;
                INimbleGumCallBack listener = mCallbacks.getBroadcastItem(N);
                Long cookie = (Long) mCallbacks.getBroadcastCookie(N);
                if (cookie == nimBleGumApply.id) {
                    try {
                        listener.onSlideDown();
                    } catch (RemoteException e) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
        }

        @Override
        public void onUserLeave(NimBleGumInterface nbg) {
            int N = mCallbacks.beginBroadcast();
            while (N > 0) {
                N--;
                INimbleGumCallBack listener = mCallbacks.getBroadcastItem(N);
                Long cookie = (Long) mCallbacks.getBroadcastCookie(N);
                if (cookie == nimBleGumApply.id) {
                    try {
                        listener.onUserLeave();
                    } catch (RemoteException e) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
        }

        @Override
        public void onUserOperate(NimBleGumInterface nbg) {
            int N = mCallbacks.beginBroadcast();
            while (N > 0) {
                N--;
                INimbleGumCallBack listener = mCallbacks.getBroadcastItem(N);
                Long cookie = (Long) mCallbacks.getBroadcastCookie(N);
                if (cookie == nimBleGumApply.id) {
                    try {
                        listener.onUserOperate();
                    } catch (RemoteException e) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
        }

        @Override
        public void onOperateEvent(NimBleGumInterface nbg, NimbleGumOperateEvent operateEvent) {
            int N = mCallbacks.beginBroadcast();
            while (N > 0) {
                N--;
                INimbleGumCallBack listener = mCallbacks.getBroadcastItem(N);
                Long cookie = (Long) mCallbacks.getBroadcastCookie(N);
                if (cookie == nimBleGumApply.id) {
                    try {
                        listener.onOperateEvent(operateEvent);
                    } catch (RemoteException e) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
        }

        @Override
        public void onUiModeChanged(NimBleGumInterface nbg, boolean night) {
            int N = mCallbacks.beginBroadcast();
            while (N > 0) {
                N--;
                INimbleGumCallBack listener = mCallbacks.getBroadcastItem(N);
                Long cookie = (Long) mCallbacks.getBroadcastCookie(N);
                if (cookie == nimBleGumApply.id) {
                    try {
                        listener.onUiModeChanged(night);
                    } catch (RemoteException e) {
                    }
                }
            }
            mCallbacks.finishBroadcast();
        }
    }


    class NimbleGumSmartComponentServiceIml extends INimbleGumSmartComponentService.Stub {

        @Override
        public void onSmartComponent(String type, Bundle bundle) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    NimbleGumSmartComponentBaseService.this.onSmartComponent(type, bundle);
                }
            });
        }

        @Override
        public void onSmartComponentCallBack(NimBleGumApply nimBleGumApply, INimbleGumCallBack cb) throws RemoteException {
            synchronized (mCallbacks) {
                if (cb == null) {
                    return;
                }
                mCallbacks.register(cb, nimBleGumApply.id);
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    NimBleGum nimBleGum = NimbleGumSmartComponentBaseService.this.onSmartComponentCallBack(nimBleGumApply, new Listener(nimBleGumApply));
                    if (nimBleGum == null) {
                        Log.e(TAG_NBG_BASE_SERVICE, " onSmartComponentCallBack null ");
                        mApplyNimBleGums.remove(nimBleGumApply.id);
                        mCallbacks.unregister(cb);
                        return;
                    } else {
                        if (nimBleGum.getOnChangerListener().size() == 0 || mApplyNimBleGums.get(nimBleGumApply.id) == null) {
                            throw new IllegalStateException(" You need to do the following:\n" +
                                    "1. Set the onSmartComponentCallBack parameter listener to the NimBleGum you returned;\n" +
                                    "2. Put NimBleGum into mApplyNimBleGums with NimBleGumApply.id;");
                        }
                    }
                }
            });
        }

        @Override
        public void cancelGum(NimBleGumApply nimBleGumApply) throws RemoteException {
            NimBleGum nimBleGum = mApplyNimBleGums.get(nimBleGumApply.id);
            if (nimBleGum != null) {
                nimBleGum.cancel();
            } else {
                Log.e(TAG_NBG_BASE_SERVICE, " cancelGum nimBleGumApply " + nimBleGumApply.id + " dose not exist");
            }
        }

        @Override
        public void updateGum(NimBleGumApply nimBleGumApply) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    NimBleGum nimBleGum = mApplyNimBleGums.get(nimBleGumApply.id);
                    if (nimBleGum != null) {
                        if (!nimBleGum.lockedTime) {
                            nimBleGum.activityTimeout = nimBleGumApply.activityTimeout;
                            nimBleGum.operatorTimeout = nimBleGumApply.operatorTimeout;
                            NimbleGumManager.getInstance().update(nimBleGum);
                        } else {
                            Log.e(TAG_NBG_BASE_SERVICE, " updateGum time failed lockedTime is true");
                        }
                        nimBleGum.extras = nimBleGumApply.extras;
                        NimbleGumSmartComponentBaseService.this.onSmartComponentChanger(nimBleGumApply.id);
                    } else {
                        Log.e(TAG_NBG_BASE_SERVICE, " updateGum nimBleGumApply " + nimBleGumApply.id + " dose not exist");
                    }
                }
            });
        }

        @Override
        public boolean isShowing(NimBleGumApply nimBleGumApply) throws RemoteException {
            NimBleGum nimBleGum = mApplyNimBleGums.get(nimBleGumApply.id);
            if (nimBleGum != null) {
                return NimbleGumManager.getInstance().isShowing(nimBleGum);
            } else {
                Log.e(TAG_NBG_BASE_SERVICE, " isShowing nimBleGumApply " + nimBleGumApply.id + " dose not exist");
            }
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }
}

