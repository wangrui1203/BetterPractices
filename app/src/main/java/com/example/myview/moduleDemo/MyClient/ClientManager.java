package com.example.myview.moduleDemo.MyClient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.RequiresPermission;
import com.example.myview.utils.AppUtils;
import com.ray.mysdk.ICalculator;
import com.ray.mysdk.bean.Sample;
import com.ray.mysdk.bean.Sample2;
import com.ray.mysdk.binderpool.IHvac;
import com.ray.mysdk.binderpool.IVehicle;
import com.ray.mysdk.listener.ICalculatorListener;

import java.io.File;
import java.io.InputStream;

/**
 * @author ray
 * @date 2023/8/15 09:29
 */
public class ClientManager {

    private static final String TAG = "CilentManager";

    //aidl接口类型的对象
    private ICalculator mCalculator;
    Context mContext;

    private ClientManager(){}

    private static class SingletonHolder{
        private static final ClientManager INSTANCE = new ClientManager();
    }

    public static ClientManager getInstance() {
        return ClientManager.SingletonHolder.INSTANCE;
    }


    //connection对象
    //onServiceConnected方法和onServiceDisconnected是在主线程中执行的
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"-- service connected --");
            //获取服务端对象的代理，并转换为aidl接口类型的对象，从而调用服务端提供的方法
            mCalculator = ICalculator.Stub.asInterface(service);

//            transferCustomType();

            //死亡代理
            try {
                service.linkToDeath(mDeathRecipient, 0);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"-- service disconnected --");
            mCalculator = null;
        }
    };

    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "--  service binderDied, service: " + mCalculator);
            if(mCalculator != null){
                //当前绑定由于异常断开时，将当前死亡代理进行解绑
                mCalculator.asBinder().unlinkToDeath(mDeathRecipient, 0);
                //重新绑定服务端的service
                mCalculator = null;
                bindToService(mContext);
            }
        }
    };

    public void bindToService(Context context) {
        mContext = context.getApplicationContext();
        Intent intent = new Intent();
        intent.setAction("com.example.aidl.ServerService");
        intent.setClassName("com.ray.myserver", "com.ray.myserver.ServerService");
        boolean suc = context.bindService(intent, serviceConnection, context.BIND_AUTO_CREATE);
        Log.i(TAG, "bindToService: " + suc);
    }

    //基础使用
    public void baseCalculate(){
        calculate('+' , 6, 3);
        calculate('-' , 6, 3);
        calculate('*' , 6, 3);
        calculate('/' , 6, 3);
    }


    // AIDL 基本使用方式
    private void calculate(final char operator, final int num1, final int num2) {
        if(mCalculator != null){
            try {
                int result = 0;
                switch (operator) {
                    case '+':
                        result = mCalculator.add(num1, num2);
                        break;
                    case '-':
                        result = mCalculator.subtract(num1, num2);
                        break;
                    case '*':
                        result = mCalculator.multiply(num1, num2);
                        break;
                    case '/':
                        result = mCalculator.divide(num1, num2);
                        break;
                }
                Log.i(TAG, "calculate result : " + result);
            } catch (RemoteException exception) {
                Log.i(TAG, "calculate: " + exception);
            }
        }else{
            bindToService(mContext);
        }

    }

    // 问题1、2 - 传递自定义类型、数据流向
    public void transferCustomType() {
        if(mCalculator != null){
            try {
                // 传递自定义类型
                Sample sample = new Sample();
                sample.setNum(12);
                mCalculator.optionParcel(sample);
            } catch (Exception exception) {
                Log.e(TAG, "transferCustomType: " + exception);
            }
        }else{
            bindToService(mContext);
        }
    }

    public void transferParcelSerial() {
        if(mCalculator != null){
            try {
                // 传递自定义类型ParcelSerial
                Sample sample = new Sample();
                sample.setNum(12);
                Sample2 sample2 = new Sample2();
                sample2.setNum(22);
                sample.setSample2(sample2);
                Log.d(TAG, "sample1+sampl2" + sample.toString());
                mCalculator.optionParcel(sample);
            } catch (Exception exception) {
                Log.e(TAG, "transferParcelSerial: " + exception);
            }
        }else{
            bindToService(mContext);
        }
    }

    // 自定义数据，读取文件流？总是超出bundle范围
    public void transferCustomTypeStr() {
        if(mCalculator != null){
            try {
//                Log.d(TAG, "Before Size - " + getObjectSize());
                // 传递自定义类型
                Sample sample = new Sample();
                sample.setNum(12);
                // 打开 assets 下的link.txt取出内容，保存为字符串
                //读取文件流？总是超出bundle范围
                InputStream inputStream = mContext.getAssets().open("link.txt");
                byte[] bytes = new byte[1024 * 1600];
                inputStream.read(bytes);
                sample.setStr(new String(bytes));
                Log.i(TAG, "transferCustomTypeStr size:  " + bytes.length);
                mCalculator.optionParcel(sample);
            } catch (Exception exception) {
                Log.e(TAG, "transferCustomTypeStr: " + exception);
            }
        }else{
            bindToService(mContext);
        }
    }


    // 计算一个对象在内存中的大小
    private long getObjectSize(){//Object obj) {
        long size = 0;
        try {
            // getNativeHeapAllocatedSize 获取当前进程的堆内存大小
            size = android.os.Debug.getNativeHeapAllocatedSize();
            Log.e(TAG, "getObjectSize: " + size);
            return size;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 问题3 - 传递Bundle
    public void transferBundle() {
        try {
            // 传递Bundle
            Bundle bundle = new Bundle();
            Sample sample = new Sample();
            sample.setNum(12);
            bundle.putParcelable("sample", sample);

            Sample2 sample2 = new Sample2();
            sample2.setNum(13);
            bundle.putSerializable("sample2", sample2);
            Log.d("","bundle " + bundle);
            mCalculator.optionBundle(bundle);

        } catch (RemoteException exception) {
            Log.i(TAG, "transferBundle: " + exception);
        }
    }

    // 问题4 - 传输大文件
    //使用ParcelFileDescriptor静态方法，调用一个本地文件，且是只读模式。
    //该open方法返回一个文件描述符，将该描述符进行传递
    private void transferData() {
        try {
            // file.iso 是要传输的文件，位于app的缓存目录下，约3.5GB
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(new File(mContext.getCacheDir(), "file.iso"), ParcelFileDescriptor.MODE_READ_ONLY);
            // 调用AIDL接口，将文件描述符的读端 传递给 接收方
            mCalculator.transactFileDescriptor(fileDescriptor);
            fileDescriptor.close();

            /******** 下面的方法也可以实现文件传输，「接收端」不需要任何修改，原理是一样的 ********/
//        createReliablePipe 创建一个管道，返回一个 ParcelFileDescriptor 数组，
//        数组中的第一个元素是管道的读端，
//        第二个元素是管道的写端
//            ParcelFileDescriptor[] pfds = ParcelFileDescriptor.createReliablePipe();
//            ParcelFileDescriptor pfdRead = pfds[0];
//
//            // 调用AIDL接口，将管道的读端传递给 接收端
//            options.transactFileDescriptor(pfdRead);
//
//            ParcelFileDescriptor pfdWrite = pfds[1];
//
//            // 将文件写入到管道中
//            byte[] buffer = new byte[1024];
//            int len;
//            try (
//                    // file.iso 是要传输的文件，位于app的缓存目录下
//                    FileInputStream inputStream = new FileInputStream(new File(getCacheDir(), "file.iso"));
//                    ParcelFileDescriptor.AutoCloseOutputStream autoCloseOutputStream = new ParcelFileDescriptor.AutoCloseOutputStream(pfdWrite);
//            ) {
//                while ((len = inputStream.read(buffer)) != -1) {
//                    autoCloseOutputStream.write(buffer, 0, len);
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        transferData();
    }

    // 问题5 - oneway 将远程调用改为异步调用，为非阻塞式的，客户端不需要等待处理结果，且在一个binder对象中调用，会按照顺序进行
    public void callOneway() {
        try {
            for(int i = 0; i< 10; i++){
                mCalculator.optionOneway(i);
                Log.i(TAG, "callOneway: " + i +  " " + Thread.currentThread().getName());
            }
        } catch (RemoteException exception) {
            Log.e(TAG, "callOneway exception : " + exception);
        }
    }

    // 问题6 - RemoteCallback
    private void callRemote() {
        try {
            ICalculatorListener.Stub listener = new ICalculatorListener.Stub() {
                @Override
                public void callback(final String result) throws RemoteException {
                    Log.i(TAG, "callback: " + result);
                }
            };
            mCalculator.registerListener(listener);

//            Thread.sleep(3000);
//            mCalculator.unregisterListener(listener);
//            Log.i(TAG, "callRemote: ");

        } catch (RemoteException exception) {
            Log.i(TAG, "callRemote: " + exception);
        }
    }

    public static final int TYPE_HAVC = 1;
    public static final int TYPE_VEHICLE = 2;

    // 问题7 - Binder连接池
    private void callBinderPool() {
        try {
            IBinder binder = mCalculator.queryBinder(TYPE_HAVC);
            IHvac hvac = IHvac.Stub.asInterface(binder);
            // Hvac 提供的aidl接口
            hvac.basicTypes(1, 2, true, 3.0f, 4.0, "5");

            binder = mCalculator.queryBinder(TYPE_VEHICLE);
            IVehicle vehicle = IVehicle.Stub.asInterface(binder);
            // Vehicle 提供的aidl接口
            vehicle.basicTypes(1, 2, true, 3.0f, 4.0, "5");

        } catch (RemoteException exception) {
            Log.i(TAG, "callBinderPool: " + exception);
        }
    }

    // 问题9 - 权限
    @RequiresPermission(PERMISSION)
    private void callPermission() {
        try {
            if (checkPermission()) {
                Log.i(TAG, "callPermission: 有权限");
                mCalculator.optionPermission(1);
            } else {
                Log.i(TAG, "callPermission: 没有权限");
            }
        } catch (RemoteException exception) {
            Log.i(TAG, "callPermission: " + exception);
        }
    }

    /**
     * 检查应用自身是否有权限
     *
     * @return true 有权限，false 没有权限
     */
    private boolean checkPermission() {
        return mContext.checkSelfPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static final String PERMISSION = "com.wj.permission";

}
