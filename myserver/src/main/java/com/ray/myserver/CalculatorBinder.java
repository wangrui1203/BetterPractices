package com.ray.myserver;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
import android.util.SparseArray;
import com.example.loglib.KLog;
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

    // 一个服务端可能会同时连接多个客户端，所以对于客户端注册过来的binder对象，我们应该是使用一个List集合来保存
    // 如果使用ArrayList或CopyOnWriteArrayList， 来保存binder实例，需要在断开连接时，清楚保存的binder
    // 否则调用已经断开连接的binder，会报出DeadObjectException问题
    // =》可以直接使用MyRemoteCallback，可以在进程销毁时自动清理，主要用于执行服务端到客户端的回调
    private final List<ICalculatorListener> mListeners = new CopyOnWriteArrayList<>();

    // 使用MyRemoteCallback
    // 注册 register-unregister
    // 处理回调 beginBroadcast(),getBroadcastItem(int),finishBroadcast()
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
        KLog.i(TAG, "optionParcel: " + sample);
    }

    @Override
    public void optionBundle(final Bundle bundle) throws RemoteException {
//        Log.i(TAG, "optionBundle1: " + bundle);
        bundle.setClassLoader(mContext.getClassLoader());
        Sample2 sample2 = (Sample2) bundle.getSerializable("sample2");
        KLog.i(TAG, "optionBundle2: " + sample2);

        Sample sample = bundle.getParcelable("sample");
        KLog.i(TAG, "optionBundle3: " + sample);
    }


    //https://juejin.cn/post/7218615271384088633
    //ParcelFileDescriptor构造一个input数据流，写入本地文件
    @Override
    public void transactFileDescriptor(ParcelFileDescriptor pfd) {
        KLog.i(TAG, "transactFileDescriptor: " + Thread.currentThread().getName());
        KLog.i(TAG, "transactFileDescriptor: calling pid:" + Binder.getCallingPid() + " calling uid:" + Binder.getCallingUid());
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
        KLog.i(TAG, "optionOneway: start " + i + " " + Thread.currentThread().getName());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "optionOneway: " + i);
    }

    //在服务端保存一个binder实例，在需要时服务端可以通过该实例向客户端发送请求
    //ICalculatorListener相当于客户端发给服务端的binder
    @Override
    public void registerListener(final ICalculatorListener listener) throws RemoteException {
        KLog.i(TAG, "registerListener: listener sign:" + listener.hashCode());
        KLog.i(TAG, "registerListener: listener IBinder sign:" + listener.asBinder());

        // 错误做法，因为aidl传递，存在两次序列化，因此客户端传递给服务端的对象并不是同一个，因此无法标识
        // 因为每次Listener都不是一个对象，所以会导致无法移除，应该保存listener.asBinder()来判断
        // asBinder返回的时ibinder对象，每次都是同一个
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

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                notifyToClient();
            }).start();
        }


    }

    @Override
    public void unregisterListener(final ICalculatorListener listener) {
        KLog.d(TAG, "unregisterListener: listener sign:" + listener);
        KLog.d(TAG, "unregisterListener: listener IBinder sign:" + listener.asBinder());
        // 错误做法，因为每次Listener都不是一个对象，所以会导致无法移除，应该保存listener.asBinder() 或使用 RemoteCallbackList
//            mListeners.remove(listener);

        mCallbackList.unregister(listener);
        KLog.d(TAG, "unregisterListener - remain: " + mCallbackList.getRegisteredCallbackCount());
    }


    //对客户端进行权限检查
    @Override
    public void optionPermission(final int i) throws RemoteException {
        // 在oneway 接口中Binder.getCallingPid() 始终为 0
        KLog.i(TAG, "optionPermission: calling pid " + Binder.getCallingPid() + "; calling uid" + Binder.getCallingUid());

//        // 方法一：检查权限,如果没有权限，抛出SecurityException
//        mContext.enforceCallingPermission("com.ray.permission", "没有权限");

        // 方法二：检查权限，如果没有权限，返回false
        boolean checked = mContext.checkCallingPermission("com.ray.permission") == PackageManager.PERMISSION_GRANTED;
        KLog.d(TAG, "optionPermission: " + checked);
    }

    // 向客户端发送消息
    // 方法上加锁，因为beginBroadcast和finishBroadcast是成对出现对，可能会存在多线程问题，出现调用两次beginBroadcast
    private synchronized void notifyToClient() {
        KLog.i(TAG, "notifyToClient");
        int n = mCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                mCallbackList.getBroadcastItem(i).callback(i + " --");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallbackList.finishBroadcast();
    }

    // 管理和分发binder的机制，使不同模块之间使用统一的一个service进行binder通信，客户端通过
    // 一个binder连接到服务端，根据不同的业务需求，获取到对象的binder实例，从而实现跨进程通信。
    // 减少客户端和服务端之间的连接数，提高性能和稳定性
    // 车载项目carService使用的就是这样一个机制，其他应用使用的不多
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
            KLog.i(TAG, "basicTypes: ");
        }
    }

    public static class MyVehicle extends IVehicle.Stub {
        public static final String TAG = "MyVehicle";

        @Override
        public void basicTypes(final int anInt, final long aLong, final boolean aBoolean, final float aFloat, final double aDouble, final String aString) throws RemoteException {
            KLog.i(TAG, "basicTypes: ");
        }
    }
}
