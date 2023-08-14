package com.ray.myserver;

import android.os.IInterface;
import android.os.RemoteCallbackList;
import android.util.Log;

/**
 * @author ray
 * @date 2023/8/14 11:33
 */
public class MyRemoteCallback <E extends IInterface> extends RemoteCallbackList<E> {

    public static final String TAG = "MyRemoteCallback";

    // q: onCallbackDied() 什么时候会被调用？
    // a: 当客户端进程被杀死时，会调用onCallbackDied()方法

    // q: cookie 为什么是空的
    // a: 从客户端传过来的cookie是空的，因为客户端没有传cookie

    // q: 客户端如何传cookie
    // a: 通过Bundle传递
    @Override
    public void onCallbackDied(final E callback, final Object cookie) {
        super.onCallbackDied(callback, cookie);
        Log.i(TAG, "onCallbackDied: 线程名：" + Thread.currentThread().getName() + "，Callback：" + callback + "，cookie: " + cookie);
    }

}
