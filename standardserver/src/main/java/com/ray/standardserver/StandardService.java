package com.ray.standardserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.loglib.KLog;
import com.ray.standardserver.binder.MainBinder;

/**
 * @author ray
 * @date 2023/8/16 17:00
 */
public class StandardService extends Service {

    private MainBinder mainBinder;

    private static final String CHANNEL_ID_STRING = "com.ray.standardserver.service";
    private static final int CHANNEL_ID = 0x11;

    @Override
    public void onCreate() {
        super.onCreate();
        KLog.init(true,"My StandardServer");
        KLog.d("","Service onCreate()");
        mainBinder = new MainBinder(getApplicationContext());
        //在Android 8.0之后的系统中，Service启动后需要添加Notification，将Service设定为前台Service，否则会抛出异常。
//        startServiceForeground();
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
        return mainBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        KLog.d("","onUnbind");
        return super.onUnbind(intent);
    }
}
