package com.example.myview.view;

import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.util.SizeF;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillManager;
import android.widget.RemoteViews;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import com.example.loglib.KLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;

/**
 * @author ray
 * @date 2023/9/22 16:30
 * RemoteViews with remote control support which can receive event message from host process and
 * send control message to host process
 */
public class NRemoteViews extends android.widget.RemoteViews implements Parcelable {
    public static final String TAG = "NioRemoteViews";

    public static final int MSG_HOST_MESSENGER_READY = 10001;
    public static final int MSG_HOST_EXCEPTION_OCCUR = 10002;
    public static final int MSG_HOST_RELEASE_VIEW = 10003;

    public static final String KEY_HOST_MESSENGER = "host_messenger";
    public static final String KEY_HOST_EXCEPTION = "host_exception";

    public static final Parcelable.Creator<NRemoteViews> CREATOR =
        new Parcelable.Creator<NRemoteViews>(){
            public NRemoteViews createFromParcel(Parcel parcel){
                return new NRemoteViews(parcel);
            }

            @Override
            public NRemoteViews[] newArray(int i) {
                return new NRemoteViews[i];
            }
        };
    private NRemoteViews.Controller mController;
    private Bundle mData;
    private Throwable mDataLocation;

    public NRemoteViews(String packageName, int layoutId) {
        super(packageName, layoutId);
    }

    public NRemoteViews(Parcel source) {
        super(source);
        mController = Controller.CREATOR.createFromParcel(source);
        if(source.readInt() != 0){
            mData =Bundle.CREATOR.createFromParcel(source);
        }
    }
    @Override
    public boolean onLoadClass(Class clazz) {
        KLog.i(TAG, "verify class: " + clazz);
        return true;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        mController.writeToParcel(dest, flags);
        if (mData != null) {
            dest.writeInt(1);
            mData.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
    }

    @Override
    public View apply(Context context, ViewGroup parent) {
        Context ctx;
        try {
            ctx = RestrictedContext.getRestrictedContextForPackage(context, getPackage(),
                    UserHandle.of(UserHandle.getUserId(mApplication.uid)));
        } catch (Throwable e) {
            throw new IllegalStateException("Can not create create for " + getPackage());
        }
        View v = super.apply(ctx, parent);
        mController.initView(v, mData);
        return v;
    }

        /**
         * called from the host when this view is useless
         */
        public void release() {
            if (mController != null) {
                mController.release();
                mController = null;
            }
        }
        /**
         * call from host, set a listener for provider die; the callback will be triggered when the
         * provider process crashed
         * @param listener
         */
        public void setOnProviderDieListener(ProviderDieListener listener) {
            mController.setOnProviderDieListener(listener);
        }
    public static class WeakRefHandler extends Handler{
        private WeakReference<Callback> mWeakReference;
        public WeakRefHandler(Callback callback) {
            mWeakReference = new WeakReference<Handler.Callback>(callback);
        }
        public WeakRefHandler(Looper looper, Callback callback) {
            super(looper);
            mWeakReference = new WeakReference<Handler.Callback>(callback);
        }

        @Override
        public void handleMessage(Message msg){
            if(mWeakReference != null && mWeakReference.get() != null){
                Callback callback = mWeakReference.get();
                callback.handleMessage(msg);
            }
        }
    }

    /**
     * run in host process
     */
    public static abstract class HostControl{
        private WeakRefHandler mHandler;
        private Handler.Callback mCallback;
        Messenger mMessenger;

        private Bundle mData;

        public HostControl(){}
        /**
         * subclass must have a public constructor with parameter Messenger
         *
         * @param messenger
         */
        public HostControl(Messenger messenger) {
            mCallback = new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    HostControl.this.handleMessageFromProvider(msg);
                    return true;
                }
            };
            mHandler = new WeakRefHandler(Looper.getMainLooper(), mCallback);
            mMessenger = messenger;

            Messenger outMessenger = new Messenger(mHandler);
            Message msg = Message.obtain();
            msg.what = MSG_HOST_MESSENGER_READY;
            Bundle data = new Bundle();
            data.putParcelable(KEY_HOST_MESSENGER, outMessenger);
            msg.setData(data);
            try {
                KLog.i(TAG, "send host messenger to provider");
                sendMessageToProvider(msg);
            } catch (RemoteException e) {
                KLog.e(TAG, "err to send host messenger to provider", e);
            }
        }

        /**
         * send message to provider Control, if the provider process has been died, a
         * RemoteException will be throws
         *
         * @param msg
         * @throws RemoteException
         */
        public final void sendMessageToProvider(Message msg) throws RemoteException {
            mMessenger.send(msg);
        }
        /**
         * run in host process when the host apply this NioRemoteViews
         * @param rootView the root view inflated from NioRemoteViews
         */
        public abstract void initView(View rootView);

        /**
         * handle messages from provider control
         * @param msg
         */
        public abstract void handleMessageFromProvider(Message msg);

        /**
         * the host release this view
         * it is a good chance to tell the provider to clear some resource
         */
        public void release() {
            if (mHandler == null) {
                throw new IllegalStateException("this NioRemoteViews already released");
            }
            Message msg = Message.obtain();
            msg.what = MSG_HOST_RELEASE_VIEW;
            try {
                sendMessageToProvider(msg);
            } catch (RemoteException e) {
                // the provider process already die, ignore
                KLog.e(TAG, "call NioRemoteViews.release, but the provider already die");
            } finally {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
                mCallback = null;
                mMessenger = null;
            }
        }
        private void setData(Bundle data) {
            mData = data;
        }

        protected final Bundle getData() {
            return mData;
        }
    }


    /**
     * run in provider process
     */
    private static abstract class ProviderControl implements IBinder.DeathRecipient {
        private Messenger mMessenger;

        private Messenger mOutMessenger;
        private WeakRefHandler mHandler;
        private Handler.Callback mCallback;

        public ProviderControl(){
            mCallback = new Handler.Callback(){
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    if(msg.what == MSG_HOST_MESSENGER_READY){
                        KLog.i(TAG, "receive host messenger");
                        mMessenger = msg.getData().getParcelable(KEY_HOST_MESSENGER);
                        onHostInit();
                        try {
                            mMessenger.getBinder().linkToDeath(ProviderControl.this, 0);
                        } catch (RemoteException e) {
                           KLog.e(TAG, "host crash ... ", e);
                           // host process crash, don't bother the provider process
                           onHostDie();
                        }
                    }else if(msg.what == MSG_HOST_EXCEPTION_OCCUR){
                        String error = msg.getData().getString(KEY_HOST_EXCEPTION);
                        KLog.e(TAG, "host exception occur: " + error);
                        handleExceptionFromHost(new RuntimeException(error));
                    }else if(msg.what == MSG_HOST_RELEASE_VIEW){
                        onRelease();
                        KLog.i(TAG, "the host released this NioRemoteViews");
                        onViewReleased();
                    }
                    ProviderControl.this.handleMessageFromHost(msg);
                    return true;
                }
            };
            mHandler = new WeakRefHandler(Looper.getMainLooper(),mCallback);
            mOutMessenger = new Messenger(mHandler);
        }

        private void onRelease() {
            try {
                if (mMessenger != null) {
                    mMessenger.getBinder().unlinkToDeath(this, 0);
                }
            } finally {
                mMessenger = null;
            }
            mOutMessenger = null;
            mHandler = null;
            mCallback = null;
        }
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (mMessenger != null) {
                try {
                    mMessenger.getBinder().unlinkToDeath(this, 0);
                } catch (Throwable e) {
                    // ignore any exception
                }
            }
        }
        /**
         * called when the host release this NioRemoteViews
         */
        protected void onViewReleased() {}
        /**
         * called when the host init this NioRemoteViews
         */
        protected void onHostInit() {}
        /**
         364           * The exception generated by HostControl will be transferred here
         365           */
        @CallSuper
        public void handleExceptionFromHost(RuntimeException exception) {
            throw exception;
        }

        public final void sendMessageToHost(Message msg) throws RemoteException {
            if (mMessenger != null) {
                mMessenger.send(msg);
            } else {
                throw new IllegalStateException("Cannot send a message to the host before the host init");
            }
        }
        /**
         * * receive  message from host control*
         * @param msg
         */
        public abstract void handleMessageFromHost(Message msg);
        @Override
        public void binderDied() {
            this.onHostDie();
        }

        /**
         * called when host process crash
         */
        protected void onHostDie(){}
    }
    private static class Controller implements Parcelable, IBinder.DeathRecipient{
        private NRemoteViews.ProviderControl mProviderControl;
        private Messenger mProviderMessenger;
        private String mHostControlClassName;

        private HostControl mHostControl;
        private ProviderDieListener mProviderDieListener;

        public static final Creator<NRemoteViews.Controller> CREATOR =
                new Creator<NRemoteViews.Controller>() {
            public NRemoteViews.Controller createFromParcel(Parcel source) {
                return new NRemoteViews.Controller(source);
            }

            public NRemoteViews.Controller[] newArray(int size) {
                return new NRemoteViews.Controller[size];
            }
        };
        public Controller(NRemoteViews.ProviderControl control, Class<?
                extends NRemoteViews.HostControl> clazz) {
            mProviderControl = control;
            mHostControlClassName = clazz.getName();
        }
        public Controller(Parcel source) {
            mHostControlClassName = source.readString();
            mProviderMessenger = Messenger.readMessengerOrNullFromParcel(source);
            if (mProviderMessenger == null) {
                binderDied();
            } else {
                try {
                    mProviderMessenger.getBinder().linkToDeath(this, 0);
                } catch (RemoteException e) {
                    // provider process crash, don't bother the host process
                    binderDied();
                }
            }
        }
        void initView(View root, Bundle data) {
            Message msg = null;

            try {
                ClassLoader loader = root.getContext().getClassLoader();
                Class clazz = loader.loadClass(mHostControlClassName);
                Constructor constructor = clazz.getConstructor(Messenger.class);
                mHostControl = (HostControl) constructor.newInstance(mProviderMessenger);
                if (data != null) {
                    mHostControl.setData(data);
                } else {
                    mHostControl.setData(Bundle.EMPTY);
                }
                mHostControl.initView(root);
            } catch (Throwable e) {
                KLog.e(TAG, "err to init HostControl", e);
                // transfer exception to provider
                msg = Message.obtain();
                msg.what = MSG_HOST_EXCEPTION_OCCUR;
                Bundle exceptionMsg = new Bundle();
                exceptionMsg.putString(KEY_HOST_EXCEPTION, Arrays.toString(e.getStackTrace()));
            }

            if (msg != null) {
                try {
                    mProviderMessenger.send(msg);
                } catch (RemoteException e) {
                    throw new IllegalStateException("Provider messenger die", e);
                }
            }
        }
        @Override
        public void binderDied() {
                if(mProviderDieListener != null){
                    mProviderDieListener.onRemoteDie();
                }

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
                dest.writeString(mHostControlClassName);
                if (mProviderControl.mOutMessenger == null) {
                    throw new IllegalStateException("This NioRemoteViews and controller have been " +
                                                "released!!!");
                }
                mProviderControl.mOutMessenger.writeToParcel(dest, flags);
        }
        protected void setOnProviderDieListener(ProviderDieListener listener) {
            if (mProviderDieListener != null) {
                KLog.w(TAG, "replacing ProviderDieListener");
            }
            mProviderDieListener = listener;
        }
        protected void release(){
            if (mHostControl != null) {
                mHostControl.release();
            } else if (mProviderMessenger != null) {
                Message msg = Message.obtain();
                msg.what = MSG_HOST_RELEASE_VIEW;
                try {
                    mProviderMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    KLog.e(TAG, "call NioRemoteViews.release, but the provider already die");
                }
            }
            mProviderMessenger.getBinder().unlinkToDeath(this, 0);
            mProviderMessenger = null;
            mProviderDieListener = null;
            mHostControl = null;
            mProviderControl = null;
        }
    }

        /**
         * set controller for this NioRemoteViews
         * @param controller the ProviderControl instance by provider, run in provider process
         * @param clazz      the HostControl class, a instance of it will be constructed when the host
         *                   this NioRemoteViews
         */
        public void setController(NRemoteViews.ProviderControl controller, Class<?
                extends NRemoteViews.HostControl> clazz) {
            mController = new NRemoteViews.Controller(controller, clazz);
        }


        /**
         * Provide preset data for NioRemoteViews, can only be set once and can be accessed in
         * {@link HostControl#initView(View)} with {@link  HostControl#getData()}
         * @param data a bundle of data
         */
        public void setBundle(Bundle data) {
            if (mDataLocation != null) {
                KLog.e(TAG, "ERROR data can only set once, Previous stacktrace is ", mDataLocation);
                throw new IllegalStateException("setBundle can only be called once");
            } else {
                mDataLocation = new Throwable();
            }
            if (data == null) {
                throw new IllegalArgumentException("data is null");
            }
            mData = data;
        }


    public static interface ProviderDieListener{
        void onRemoteDie();
    }

    private static class RestrictedContext extends ContextWrapper{
        static final Class[] sServiceClassWhiteList;
        static final String[] sServiceNameWhiteList;

        static {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sServiceClassWhiteList = new Class[]{
//                        AccessibilityManager.class,
//                        AudioManager.class,
//                        AutofillManager.class,
//                        ClipboardManager.class,
//                        DisplayManager.class,
//                        InputManager.class,
//                        LayoutInflater.class,
//                        TextClassificationManager.class,
//                        TextServicesManager.class,
//                        SearchManager.class,
                };

                sServiceNameWhiteList = new String[]{
                        Context.ACCESSIBILITY_SERVICE,
                        Context.AUDIO_SERVICE,
//                        Context.AUTOFILL_MANAGER_SERVICE,
                        Context.CLIPBOARD_SERVICE,
                        Context.DISPLAY_SERVICE,
                        Context.INPUT_SERVICE,
                        Context.LAYOUT_INFLATER_SERVICE,
                        Context.TEXT_CLASSIFICATION_SERVICE,
                        Context.TEXT_SERVICES_MANAGER_SERVICE,
                        Context.SEARCH_SERVICE,
                };
            } else {
                sServiceClassWhiteList = new Class[]{
//                        AccessibilityManager.class,
//                        AudioManager.class,
//                        ClipboardManager.class,
//                        DisplayManager.class,
//                        InputManager.class,
//                        LayoutInflater.class,
//                        TextServicesManager.class,
//                        SearchManager.class,
                };
                sServiceNameWhiteList = new String[]{
                        Context.ACCESSIBILITY_SERVICE,
                        Context.AUDIO_SERVICE,
                        Context.CLIPBOARD_SERVICE,
                        Context.DISPLAY_SERVICE,
                        Context.INPUT_SERVICE,
                        Context.LAYOUT_INFLATER_SERVICE,
                        Context.TEXT_SERVICES_MANAGER_SERVICE,
                        Context.SEARCH_SERVICE,
                };
            }
        }
        static final String UNSUPPORTED_MSG = " Not supported in NRemoteViews";


        public RestrictedContext(Context base) {
            super(base);
        }

        private void filterSystemService(Class<?> serviceClazz) {
            for (Class s : sServiceClassWhiteList) {
                if (s.isAssignableFrom(serviceClazz)) {
                    return;
                }
            }
            throw new UnsupportedOperationException(serviceClazz.getName() + UNSUPPORTED_MSG);
        }

        private void filterSystemService(String serviceName) {
            for (String s : sServiceNameWhiteList) {
                if (s.equals(serviceName)) {
                    return;
                }
            }
            throw new UnsupportedOperationException(serviceName + UNSUPPORTED_MSG);
        }
        static RestrictedContext getRestrictedContextForPackage(Context context, String pkg,
                                                                UserHandle userHandle) throws PackageManager.NameNotFoundException {
            Context ctx = context.createPackageContextAsUser(pkg,
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY, userHandle);
            return new RestrictedContext(ctx);
        }

        @Override
        public Object getSystemService(String name) {
            filterSystemService(name);
            return super.getSystemService(name);
        }

        @Override
        public String getSystemServiceName(Class<?> serviceClass) {
            filterSystemService(serviceClass);
            return super.getSystemServiceName(serviceClass);
        }
//        @Override
//        public SharedPreferences getSharedPreferences(File file, int mode) {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
        @Override
        public File getFileStreamPath(String name) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
//        @Override
//        public Context createPackageContextAsUser(String packageName, int flags, UserHandle user) throws PackageManager.NameNotFoundException {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                                   SQLiteDatabase.CursorFactory factory) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                                   SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Context getApplicationContext() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public PackageManager getPackageManager() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public ContentResolver getContentResolver() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
//        @Override
//        public void reloadSharedPreferences() {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
        @Override
        public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
         }
         @Override
         public boolean deleteSharedPreferences(String name) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public FileInputStream openFileInput(String name) throws FileNotFoundException {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public boolean deleteFile(String name) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
//        @Override
//        public File getSharedPreferencesPath(String name) {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
        @Override
        public String[] fileList() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getDataDir() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getFilesDir() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getNoBackupFilesDir() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getExternalFilesDir(String type) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File[] getExternalFilesDirs(String type) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getObbDir() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File[] getObbDirs() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getCacheDir() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getCodeCacheDir() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getExternalCacheDir() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File[] getExternalCacheDirs() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File[] getExternalMediaDirs() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getDir(String name, int mode) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
//        @Override
//        public File getPreloadsFileCache() {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
        @Override
        public boolean moveDatabaseFrom(Context sourceContext, String name) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public boolean deleteDatabase(String name) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public File getDatabasePath(String name) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public String[] databaseList() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, int flags) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
                                       String broadcastPermission, Handler scheduler) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
                                       String broadcastPermission, Handler scheduler, int flags) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
//        @Override
//        public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user,
//                                             IntentFilter filter, String broadcastPermission, Handler scheduler) {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
        @Override
        public boolean bindService(Intent service, ServiceConnection conn, int flags) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags,
                                         UserHandle user) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
//        @Override
//        public boolean bindServiceAsUser(Intent service, ServiceConnection conn, int flags,
//                                         Handler handler, UserHandle user) {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
        @Override
        public boolean startInstrumentation(ComponentName className, String profileFile,
                                            Bundle arguments) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
//        @Override
//        public Context createApplicationContext(ApplicationInfo application, int flags) throws PackageManager.NameNotFoundException {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
        @Override
        public Context createContextForSplit(String splitName) throws PackageManager.NameNotFoundException {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Context createConfigurationContext(Configuration overrideConfiguration) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Context createDisplayContext(Display display) {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
        @Override
        public Context createDeviceProtectedStorageContext() {
            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
        }
//        @Override
//        public Context createCredentialProtectedStorageContext() {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
//        @Override
//        public IBinder getActivityToken() {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
//        @Override
//        public IServiceConnection getServiceDispatcher(ServiceConnection conn, Handler handler,
//                                                       int flags) {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
//        @Override
//        public IApplicationThread getIApplicationThread() {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
//        @Override
//        public Handler getMainThreadHandler() {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
//        @Override
//        public AutofillManager.AutofillClient getAutofillClient() {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
//        @Override
//        public void setAutofillClient(AutofillManager.AutofillClient client) {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
//        @Override
//        public void setAutofillCompatibilityEnabled(boolean autofillCompatEnabled) {
//            throw new UnsupportedOperationException(UNSUPPORTED_MSG);
//        }
    }
}
