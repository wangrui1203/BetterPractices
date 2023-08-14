package com.ray.myserver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
import android.util.SparseArray;
import com.ray.mysdk.ICalculator;
import com.ray.mysdk.bean.Sample;
import com.ray.mysdk.bean.Sample2;
import com.ray.mysdk.binderpool.IHvac;
import com.ray.mysdk.binderpool.IVehicle;
import com.ray.mysdk.listener.ICalculatorListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ray
 * @date 2023/8/14 11:32
 */
public class CalculatorBinder extends ICalculator.Stub{

    public static final String TAG = "CalculatorBinder";
    private final Context mContext;
    public CalculatorBinder(final Context context) {
        mContext = context;
    }

    private final List<ICalculatorListener> mListeners = new CopyOnWriteArrayList<>();
    private final MyRemoteCallback<ICalculatorListener> mCallbackList = new MyRemoteCallback<>();

    private final SparseArray<IBinder> mCache = new SparseArray<>();

    @Override
    public int add(final int a, final int b) throws RemoteException {
        return a + b;
    }

    @Override
    public int subtract(final int a, final int b) throws RemoteException {
        return a - b;
    }

    @Override
    public int multiply(final int a, final int b) throws RemoteException {
        return a * b;
    }

    @Override
    public int divide(final int a, final int b) throws RemoteException {
        if(b == 0){
            throw new IllegalArgumentException("Divisor cannot be zero");
        }
        return a / b;
    }

    @Override
    public void optionParcel(final Sample sample) throws RemoteException {
        Log.i(TAG, "optionParcel: " + sample);
    }

    @Override
    public void optionBundle(final Bundle bundle) throws RemoteException {
        Log.i(TAG, "optionBundle: " + bundle.toString());
        bundle.setClassLoader(mContext.getClassLoader());
        Sample2 sample2 = (Sample2) bundle.getSerializable("sample2");
        Log.i(TAG, "optionBundle: " + sample2.toString());

        Sample sample = bundle.getParcelable("sample");
        Log.i(TAG, "optionBundle: " + sample.toString());
    }


    @Override
    public void transactFileDescriptor(ParcelFileDescriptor pfd) {
        Log.i(TAG, "transactFileDescriptor: " + Thread.currentThread().getName());
        Log.i(TAG, "transactFileDescriptor: calling pid:" + Binder.getCallingPid() + " calling uid:" + Binder.getCallingUid());
        File file = new File(mContext.getCacheDir(), "file.iso");
        try (
                ParcelFileDescriptor.AutoCloseInputStream inputStream = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
        ) {
            file.delete();
            file.createNewFile();
            FileOutputStream stream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            // 将inputStream中的数据写入到file中
            while ((len = inputStream.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
            stream.close();
            pfd.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void optionOneway(final int i) throws RemoteException {
        Log.i(TAG, "optionOneway: " + i);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "optionOneway: " + i);
    }

    @Override
    public void registerListener(final ICalculatorListener listener) throws RemoteException {
        Log.i(TAG, "registerListener: listener sign:" + listener.hashCode());
        Log.i(TAG, "registerListener: listener IBinder sign:" + listener.asBinder());

        // 错误做法，因为每次Listener都不是一个对象，所以会导致无法移除，应该保存listener.asBinder()来判断
//            if (!mListeners.contains(listener)) {
//                mListeners.add(listener);
//            }
        // 间隔 10秒钟后，使用listener向客户端发送消息
//            try {
//                Thread.sleep(10000);
//                Log.i(TAG, "registerListener: Call client");
//                for (final ICalculatorListener i : mListeners) {
//                    i.callback("11");
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

        // 正确做法
        mCallbackList.register(listener, "我是cookie");

//            for (int i = 0; i < 10; i++) {
//                new Thread(() -> {
//                    notifyToClient();
//                }).start();
//            }


    }

    @Override
    public void unregisterListener(final ICalculatorListener listener) {
        Log.i(TAG, "unregisterListener: listener sign:" + listener);
        Log.i(TAG, "unregisterListener: listener IBinder sign:" + listener.asBinder());
        // 错误做法，因为每次Listener都不是一个对象，所以会导致无法移除，应该保存listener.asBinder() 或使用 RemoteCallbackList
//            mListeners.remove(listener);

        mCallbackList.unregister(listener);

    }


    @Override
    public void optionPermission(final int i) throws RemoteException {
        // 在oneway 接口中Binder.getCallingPid() 始终为 0
        Log.i(TAG, "optionPermission: calling pid " + Binder.getCallingPid() + "; calling uid" + Binder.getCallingUid());

        // 方法一：检查权限,如果没有权限，抛出SecurityException
        mContext.enforceCallingPermission("com.ray.permission", "没有权限");

        // 方法二：检查权限，如果没有权限，返回false
        boolean checked = mContext.checkCallingPermission("com.ray.permission") == PackageManager.PERMISSION_GRANTED;
        Log.e(TAG, "optionPermission: " + checked);
    }

    // 向客户端发送消息
    private synchronized void notifyToClient() {
        Log.i(TAG, "notifyToClient");
        int n = mCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                mCallbackList.getBroadcastItem(i).callback(i + "--");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbackList.finishBroadcast();
    }

    @Override
    public IBinder queryBinder(final int type) throws RemoteException {
        IBinder binder = mCache.get(type);
        if (binder != null) {
            return binder;
        }

        switch (type) {
            case 1:
                binder = new MyHavc();
                break;
            case 2:
                binder = new MyVehicle();
                break;
        }
        mCache.put(type, binder);
        return binder;
    }

    public static class MyHavc extends IHvac.Stub {
        public static final String TAG = "MyHavc";

        @Override
        public void basicTypes(final int anInt, final long aLong, final boolean aBoolean, final float aFloat, final double aDouble, final String aString) throws RemoteException {
            Log.i(TAG, "basicTypes: ");
        }
    }

    public static class MyVehicle extends IVehicle.Stub {
        public static final String TAG = "MyVehicle";

        @Override
        public void basicTypes(final int anInt, final long aLong, final boolean aBoolean, final float aFloat, final double aDouble, final String aString) throws RemoteException {
            Log.i(TAG, "basicTypes: ");
        }
    }
}
