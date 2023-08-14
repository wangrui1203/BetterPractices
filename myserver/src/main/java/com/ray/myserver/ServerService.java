package com.ray.myserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;

/**
 * @author ray
 * @date 2023/8/14 10:29
 * 在onBinder方法中返回IBinder对象（实现了AIDL接口）
 * xml注册service
 */
public class ServerService extends Service {

    private static final String TAG = "ServerService";

    private static final String CHANNEL_ID_STRING = "com.example.server.service";
    private static final int CHANNEL_ID = 0x11;

    private CalculatorBinder mCalculatorBinder;

    public ServerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //在Android 8.0之后的系统中，Service启动后需要添加Notification，将Service设定为前台Service，否则会抛出异常。
        startServiceForeground();
    }

    /**
     * 在Android 8.0之后的系统中，Service启动后需要添加Notification，将Service设定为前台Service，否则会抛出异常。
     * 8.0之后，后台服务会被回收。所以要添加notification
     */
    private void startServiceForeground() {
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(getApplicationContext(),
                    CHANNEL_ID_STRING).build();
            startForeground(CHANNEL_ID, notification);
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mCalculatorBinder == null) {
            mCalculatorBinder = new CalculatorBinder(this);
        }
        return mCalculatorBinder;
    }


}
