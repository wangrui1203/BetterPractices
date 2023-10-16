package com.example.myview.nimblegums;

import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.*;
import android.util.Log;
import com.example.myview.INimbleGumCallBack;
import com.example.myview.INimbleGumService;
import com.example.myview.INimbleGumSmartComponentService;
import com.ray.mysdk.nimblegums.NimBleGum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.myview.nimblegums.NimBleGumDebugConfig.TAG_NBG_MANAGER;

/**
 * @author ray
 * @date 2023/9/28 16:47
 */
public class NimbleGumManager {
    private static volatile NimbleGumManager instance = null;
    protected Context mContext;
    protected INimbleGumService mService;
    protected Handler mHandler;
    protected NimbleGumCallback mNimbleGumCallback;
    private boolean initialized = false;
    private int mConnectFailed = 0;
    private PackageMonitor mPackageMonitor;
    private List<NimBleGum> mNimBleGumShowSave = Collections.synchronizedList(new ArrayList<NimBleGum>());
    private List<NimBleGum> mNimBleCancelSave = Collections.synchronizedList(new ArrayList<NimBleGum>());
    private List<SmartComponentConnection> mConnectionSave = Collections.synchronizedList(new ArrayList<SmartComponentConnection>());
    private ConcurrentHashMap<String, ComponentName> mApplyType = new ConcurrentHashMap<String, ComponentName>();
    private final ConcurrentHashMap<ComponentName, INimbleGumSmartComponentService> mImplServices =
            new ConcurrentHashMap<ComponentName, INimbleGumSmartComponentService>();


    private NimbleGumManager() {
    }

    public static NimbleGumManager getInstance() {
        if (instance == null) {
            synchronized (NimbleGumManager.class) {
                if (instance == null) {
                    instance = new NimbleGumManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        if (context == null) {
            Log.e(TAG_NBG_MANAGER, " context is null! ");
            return;
        }

        if (initialized) {
            Log.e(TAG_NBG_MANAGER, " Initialization completed ");
            return;
        }

        mContext = context.getApplicationContext();

        mPackageMonitor = new PackageMonitor();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mPackageMonitor, filter);

        mNimbleGumCallback = new NimbleGumCallback();
        mHandler = new Handler(mContext.getMainLooper(), mNimbleGumCallback);

        initialized = true;

        getService();
        getAllApplyType();
    }


    private void getAllApplyType() {
        mApplyType.clear();
        Intent queryIntent = new Intent(Intent.ACTION_MAIN);
        queryIntent.addCategory(NimbleGumSmartComponentBaseService.CATEGORY);
        List<ResolveInfo> resolvedServicesInfo = mContext.getPackageManager().queryIntentServices(queryIntent, PackageManager.GET_META_DATA);
        for (ResolveInfo info : resolvedServicesInfo) {
            ComponentName cn = new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name);
            Bundle bundle = info.serviceInfo.metaData;
            if (bundle != null) {
                Resources resources = null;
                try {
                    resources = mContext.getPackageManager().getResourcesForApplication(info.serviceInfo.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (resources == null)
                    return;
                int resId = 0;
                try {
                    resId = info.serviceInfo.metaData.getInt(info.serviceInfo.packageName + "." + NimbleGumSmartComponentBaseService.META_DATA_NAME);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (resId == 0)
                    return;
                String[] typeList = resources.getStringArray(resId);
                if (typeList != null && typeList.length != 0) {
                    for (int i = 0; i < typeList.length; i++) {
                        if (mApplyType.containsKey(typeList[i])) {
                            Log.e(TAG_NBG_MANAGER, " the same type already exists ");
                        }
                        mApplyType.put(typeList[i], cn);
                    }
                }
            }
        }
    }

    private void getService() {
        Log.i(TAG_NBG_MANAGER, " getService ");
        if (!initialized) {
            Log.i(TAG_NBG_MANAGER, " have no initialized");
        }
        if (mService == null) {
            IBinder binder = ServiceManager.getService("nio_nimblegum_service");
            if (binder != null) {
                mService = INimbleGumService.Stub.asInterface(binder);
                try {
                    binder.linkToDeath(mDeathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (!mNimBleGumShowSave.isEmpty()) {
                    mHandler.sendEmptyMessage(NimbleGumCallback.MSG_TRY_SHOW);
                }
                if (!mNimBleCancelSave.isEmpty()) {
                    mHandler.sendEmptyMessage(NimbleGumCallback.MSG_TRY_CANCEL);
                }
            } else {
                Log.i(TAG_NBG_MANAGER, " getService binder isNull");
                mHandler.sendEmptyMessage(NimbleGumCallback.MSG_BIND_SERVICE);
            }
        }
    }

    private void bindService() {
        Log.i(TAG_NBG_MANAGER, " bindService ");
        mHandler.removeMessages(NimbleGumCallback.MSG_BIND_SERVICE);
        Intent intent = new Intent("NIMBLEGUN_SERVICE");
        intent.setPackage("com.nio.nimblegum");
        boolean success = mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        if (!success) {
            Log.i(TAG_NBG_MANAGER, " bindService success");
            mConnectFailed++;
            if (mConnectFailed <= 20) {
                mHandler.sendEmptyMessageDelayed(NimbleGumCallback.MSG_BIND_SERVICE, 2000);
            } else {
                mConnectFailed = 0;
                mHandler.sendEmptyMessageDelayed(NimbleGumCallback.MSG_BIND_SERVICE, 60000);
            }
        } else {
            Log.i(TAG_NBG_MANAGER, " bindService failed");
            mConnectFailed = 0;
        }
    }

    protected final class NimbleGumCallback implements Handler.Callback {
        public static final int MSG_BIND_SERVICE = 1;
        public static final int MSG_TRY_SHOW = 2;
        public static final int MSG_TRY_CANCEL = 3;

        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case MSG_BIND_SERVICE: {
                    bindService();
                }
                break;
                case MSG_TRY_SHOW: {
                    if (!mNimBleGumShowSave.isEmpty()) {
                        show(mNimBleGumShowSave.get(0));
                    }
                    mNimBleGumShowSave.clear();
                }
                break;
                case MSG_TRY_CANCEL: {
                    if (!mNimBleGumShowSave.isEmpty()) {
                        cancel(mNimBleCancelSave.get(0));
                    }
                    mNimBleCancelSave.clear();
                }
                break;
            }
            return true;
        }
    }

    private final ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG_NBG_MANAGER, " onServiceConnected : " + name);
            mService = INimbleGumService.Stub.asInterface(service);
            if (mService == null) {
                return;
            }
            try {
                service.linkToDeath(mDeathRecipient, 0);
            } catch (Exception e) {
                Log.d(TAG_NBG_MANAGER, " unlinkToDeath error : " + e);
            }
            if (!mNimBleGumShowSave.isEmpty()) {
                mHandler.sendEmptyMessage(NimbleGumCallback.MSG_TRY_SHOW);
            }
            if (!mNimBleCancelSave.isEmpty()) {
                mHandler.sendEmptyMessage(NimbleGumCallback.MSG_TRY_CANCEL);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG_NBG_MANAGER, " onServiceDisconnected : " + name);
            mService = null;
        }
    };

    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.e(TAG_NBG_MANAGER, " binderDied ");
            mService = null;
        }
    };

    public void show(NimBleGum nimBleGum) {
        if (nimBleGum == null) {
            Log.e(TAG_NBG_MANAGER, "show: NimBleGun is null , return ");
            return;
        }
        try {
            if (mService != null) {
                Log.d(TAG_NBG_MANAGER, " call " + nimBleGum);
                mService.enqueueGum(mContext.getPackageName(), nimBleGum, nimBleGum.id, 0, new INimbleGumCallBack.Stub() {
                    @Override
                    public void onShow(int state) throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_NBG_MANAGER, " onShow " + nimBleGum);
                                nimBleGum.setShowState(state);
                                if (nimBleGum.getOnShowListener() != null) {
                                    nimBleGum.getOnShowListener().onShow(nimBleGum, state);
                                }
                                if (nimBleGum.getOnChangerListener() != null) {
                                    for (int i = 0; i < nimBleGum.getOnChangerListener().size(); i++) {
                                        nimBleGum.getOnChangerListener().get(i).onShow(nimBleGum, state);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onDismiss(int state) throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_NBG_MANAGER, " onDismiss " + nimBleGum + " state " + state);
                                if (nimBleGum.getOnDismissListener() != null) {
                                    nimBleGum.getOnDismissListener().onDismiss(nimBleGum, state);
                                }
                                if (nimBleGum.getOnChangerListener() != null) {
                                    for (int i = 0; i < nimBleGum.getOnChangerListener().size(); i++) {
                                        nimBleGum.getOnChangerListener().get(i).onDismiss(nimBleGum, state);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onConvert(int state) throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_NBG_MANAGER, " onConvert " + nimBleGum);
                                nimBleGum.setShowState(state);
                                if (nimBleGum.getOnConvertListener() != null) {
                                    nimBleGum.getOnConvertListener().onConvert(nimBleGum, state);
                                }
                                if (nimBleGum.getOnChangerListener() != null) {
                                    for (int i = 0; i < nimBleGum.getOnChangerListener().size(); i++) {
                                        nimBleGum.getOnChangerListener().get(i).onConvert(nimBleGum, state);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onSlideDown() throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_NBG_MANAGER, " onSlideDown " + nimBleGum);
                                if (nimBleGum.getOnSlideDownListener() != null) {
                                    nimBleGum.getOnSlideDownListener().onSlideDown(nimBleGum);
                                }
                                if (nimBleGum.getOnChangerListener() != null) {
                                    for (int i = 0; i < nimBleGum.getOnChangerListener().size(); i++) {
                                        nimBleGum.getOnChangerListener().get(i).onSlideDown(nimBleGum);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onUserLeave() throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_NBG_MANAGER, " onUserLeave " + nimBleGum);
                                if (nimBleGum.getOnUserLeaveListener() != null) {
                                    nimBleGum.getOnUserLeaveListener().onUserLeave(nimBleGum);
                                }
                                if (nimBleGum.getOnChangerListener() != null) {
                                    for (int i = 0; i < nimBleGum.getOnChangerListener().size(); i++) {
                                        nimBleGum.getOnChangerListener().get(i).onUserLeave(nimBleGum);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onUserOperate() throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_NBG_MANAGER, " onUserOperate " + nimBleGum);
                                if (nimBleGum.getOnUserOperateListener() != null) {
                                    nimBleGum.getOnUserOperateListener().onUserOperate(nimBleGum);
                                }
                                if (nimBleGum.getOnChangerListener() != null) {
                                    for (int i = 0; i < nimBleGum.getOnChangerListener().size(); i++) {
                                        nimBleGum.getOnChangerListener().get(i).onUserOperate(nimBleGum);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onOperateEvent(NimbleGumOperateEvent event) throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_NBG_MANAGER, " onOperateEvent " + nimBleGum);
                                if (nimBleGum.getOnOperateEventListener() != null) {
                                    nimBleGum.getOnOperateEventListener().onOperateEvent(nimBleGum, event);
                                }
                                if (nimBleGum.getOnChangerListener() != null) {
                                    for (int i = 0; i < nimBleGum.getOnChangerListener().size(); i++) {
                                        nimBleGum.getOnChangerListener().get(i).onOperateEvent(nimBleGum, event);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void onUiModeChanged(boolean night) throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG_NBG_MANAGER, " onUiModeChange " + nimBleGum);
                                if (nimBleGum.getOnUiModeChangerListener() != null) {
                                    nimBleGum.getOnUiModeChangerListener().onUiModeChanged(nimBleGum, night);
                                }
                                if (nimBleGum.getOnChangerListener() != null) {
                                    for (int i = 0; i < nimBleGum.getOnChangerListener().size(); i++) {
                                        nimBleGum.getOnChangerListener().get(i).onUiModeChanged(nimBleGum, night);
                                    }
                                }
                            }
                        });
                    }
                });
            } else {
                Log.e(TAG_NBG_MANAGER, " mService is null , call failed ");
                mNimBleGumShowSave.clear();
                mNimBleGumShowSave.add(nimBleGum);
                getService();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void update(NimBleGum nimBleGum) {
        if (nimBleGum == null) {
            Log.e(TAG_NBG_MANAGER, "update: NimBleGun is null , return ");
            return;
        }
        try {
            if (mService != null) {
                mService.updateGum(mContext.getPackageName(), nimBleGum, 0);
            } else {
                Log.e(TAG_NBG_MANAGER, " mService is null , update failed ");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void sendOperatorAction(NimBleGum nimBleGum) {
        if (nimBleGum == null) {
            Log.e(TAG_NBG_MANAGER, "sendOperatorAction: NimBleGun is null , return ");
            return;
        }
        try {
            if (mService != null) {
                mService.sendOperatorAction(mContext.getPackageName(), nimBleGum.id, 0);
            } else {
                Log.e(TAG_NBG_MANAGER, " mService is null , sendOperatorAction failed ");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    public void cancel(NimBleGum nimBleGum) {
        if (nimBleGum == null) {
            Log.e(TAG_NBG_MANAGER, "cancel: NimBleGun is null , return ");
            return;
        }
        try {
            if (mService != null) {
                Log.d(TAG_NBG_MANAGER, " cancel " + nimBleGum);
                mService.cancelGum(mContext.getPackageName(), nimBleGum.id, 0);
            } else {
                Log.e(TAG_NBG_MANAGER, " mService is null , cancel failed ");
                mNimBleCancelSave.clear();
                mNimBleCancelSave.add(nimBleGum);
                getService();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isShowing(NimBleGum nimBleGum) {
        if (nimBleGum == null) {
            Log.e(TAG_NBG_MANAGER, "isShowing: NimBleGun is null , return ");
            return false;
        }
        try {
            if (mService != null) {
                return mService.isShowing(nimBleGum);
            } else {
                Log.e(TAG_NBG_MANAGER, " mService is null , isShowing failed ");
                getService();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void applyGum(String type, Bundle data) {
        if (!mApplyType.containsKey(type)) {
            Log.e(TAG_NBG_MANAGER, " This type " + type + " does not exist ");
            return;
        }
        if (mImplServices.containsKey(mApplyType.get(type))) {
            try {
                mImplServices.get(mApplyType.get(type)).onSmartComponent(type, data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            SmartComponentConnection serviceConnection = new SmartComponentConnection(type, data);
            Intent intent = new Intent();
            intent.setComponent(mApplyType.get(type));
            mContext.bindServiceAsUser(intent, serviceConnection, Context.BIND_AUTO_CREATE, UserHandle.CURRENT);
        }
    }

    public void applyGum(NimBleGumApply nimBleGumApply, NimBleGumApplyCallBack applyCallBack) {
        if (!mApplyType.containsKey(nimBleGumApply.applyType)) {
            Log.e(TAG_NBG_MANAGER, " This type " + nimBleGumApply.applyType + " does not exist ");
            return;
        }
        if (mImplServices.containsKey(mApplyType.get(nimBleGumApply.applyType))) {
            try {
                mImplServices.get(mApplyType.get(nimBleGumApply.applyType)).onSmartComponentCallBack(nimBleGumApply, new ApplyCallback(applyCallBack));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            SmartComponentConnection serviceConnection = new SmartComponentConnection(nimBleGumApply, applyCallBack);
            Intent intent = new Intent();
            intent.setComponent(mApplyType.get(nimBleGumApply.applyType));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                mContext.bindServiceAsUser(intent, serviceConnection, Context.BIND_AUTO_CREATE, UserHandle.CURRENT);
            }
        }
    }

    public void cancelApplyGum(NimBleGumApply nimBleGumApply) {
        if (!mApplyType.containsKey(nimBleGumApply.applyType)) {
            Log.e(TAG_NBG_MANAGER, " This type " + nimBleGumApply.applyType + " does not exist ");
            return;
        }
        if (mImplServices.containsKey(mApplyType.get(nimBleGumApply.applyType))) {
            try {
                Log.d(TAG_NBG_MANAGER, " cancelApplyGum " + nimBleGumApply);
                mImplServices.get(mApplyType.get(nimBleGumApply.applyType)).cancelGum(nimBleGumApply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG_NBG_MANAGER, " This type " + nimBleGumApply.applyType + " services does not run ");
        }
    }

    public void updateApplyGum(NimBleGumApply nimBleGumApply) {
        if (!mApplyType.containsKey(nimBleGumApply.applyType)) {
            Log.e(TAG_NBG_MANAGER, " This type " + nimBleGumApply.applyType + " does not exist ");
            return;
        }
        if (mImplServices.containsKey(mApplyType.get(nimBleGumApply.applyType))) {
            try {
                Log.d(TAG_NBG_MANAGER, " updateApplyGum " + nimBleGumApply);
                mImplServices.get(mApplyType.get(nimBleGumApply.applyType)).updateGum(nimBleGumApply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG_NBG_MANAGER, " This type " + nimBleGumApply.applyType + " services does not run ");
        }
    }

    public boolean isShowingApplyGum(NimBleGumApply nimBleGumApply) {
        if (!mApplyType.containsKey(nimBleGumApply.applyType)) {
            Log.e(TAG_NBG_MANAGER, " This type " + nimBleGumApply.applyType + " does not exist ");
            return false;
        }
        if (mImplServices.containsKey(mApplyType.get(nimBleGumApply.applyType))) {
            try {
                return mImplServices.get(mApplyType.get(nimBleGumApply.applyType)).isShowing(nimBleGumApply);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG_NBG_MANAGER, " This type " + nimBleGumApply.applyType + " services does not run ");
        }
        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        mContext.unregisterReceiver(mPackageMonitor);
        for (int i = 0; i < mConnectionSave.size(); i++) {
            mContext.unbindService(mConnectionSave.get(i));
        }
        super.finalize();
    }

    private class ApplyCallback extends INimbleGumCallBack.Stub {
        private NimBleGumApplyCallBack applyCallBack;

        public ApplyCallback(NimBleGumApplyCallBack applyCallBack) {
            this.applyCallBack = applyCallBack;
        }

        @Override
        public void onShow(int state) throws RemoteException {
            Log.d(TAG_NBG_MANAGER, " applyCallBack onShow ");
            if (applyCallBack != null) {
                applyCallBack.onShow(state);
            }
        }

        @Override
        public void onDismiss(int state) throws RemoteException {
            Log.d(TAG_NBG_MANAGER, " applyCallBack onDismiss ");
            if (applyCallBack != null) {
                applyCallBack.onDismiss(state);
            }
        }

        @Override
        public void onConvert(int state) throws RemoteException {
            Log.d(TAG_NBG_MANAGER, " applyCallBack onConvert ");
            if (applyCallBack != null) {
                applyCallBack.onConvert(state);
            }
        }

        @Override
        public void onSlideDown() throws RemoteException {
            Log.d(TAG_NBG_MANAGER, " applyCallBack onSlideDown ");
            if (applyCallBack != null) {
                applyCallBack.onSlideDown();
            }
        }

        @Override
        public void onUserLeave() throws RemoteException {
            Log.d(TAG_NBG_MANAGER, " applyCallBack onUserLeave ");
            if (applyCallBack != null) {
                applyCallBack.onUserLeave();
            }
        }

        @Override
        public void onUserOperate() throws RemoteException {
            Log.d(TAG_NBG_MANAGER, " applyCallBack onUserOperate ");
            if (applyCallBack != null) {
                applyCallBack.onUserOperate();
            }
        }

        @Override
        public void onOperateEvent(NimbleGumOperateEvent event) throws RemoteException {
            Log.d(TAG_NBG_MANAGER, " applyCallBack onOperateEvent ");
            if (applyCallBack != null) {
                applyCallBack.onOperateEvent(event);
            }
        }

        @Override
        public void onUiModeChanged(boolean night) throws RemoteException {
            Log.d(TAG_NBG_MANAGER, " applyCallBack onUiModeChange ");
            if (applyCallBack != null) {
                applyCallBack.onUiModeChanged(night);
            }
        }
    }

    private class SmartComponentConnection implements ServiceConnection {
        private String type;
        private Bundle data;
        private NimBleGumApply nimBleGumApply;
        private NimBleGumApplyCallBack applyCallBack;

        public SmartComponentConnection(String type, Bundle data) {
            this.type = type;
            this.data = data;
        }

        public SmartComponentConnection(NimBleGumApply nimBleGumApply, NimBleGumApplyCallBack applyCallBack) {
            this.nimBleGumApply = nimBleGumApply;
            this.applyCallBack = applyCallBack;
        }


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG_NBG_MANAGER, " onServiceConnected ");
            mConnectionSave.add(this);
            INimbleGumSmartComponentService implService = INimbleGumSmartComponentService.Stub.asInterface(iBinder);
            try {
                if (implService != null) {
                    mImplServices.put(componentName, implService);
                    if (applyCallBack != null) {
                        implService.onSmartComponentCallBack(nimBleGumApply, new ApplyCallback(applyCallBack));
                    } else {
                        implService.onSmartComponent(type, data);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG_NBG_MANAGER, " onServiceDisconnected ");
            mConnectionSave.remove(this);
            mImplServices.remove(componentName);
        }
    }

    private class PackageMonitor extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                if (packageName == null || packageName.length() == 0) {
                    // they sent us a bad intent
                    return;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG_NBG_MANAGER, " onReceive " + action);
                        getAllApplyType();
                    }
                });
            }
        }
    }
}