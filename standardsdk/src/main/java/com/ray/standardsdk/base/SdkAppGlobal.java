package com.ray.standardsdk.base;

import android.app.Application;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ray
 * @date 2023/8/4 13:19
 * 利用反射获取APP Context类，用于任意位置初始化Sdk，不必受context限制。可复用
 */
public class SdkAppGlobal {
    public static final String TAG_SDK = "TAG_SDK";
    private static final String TAG = TAG_SDK + SdkAppGlobal.class.getSimpleName();

    public static final String CLASS_FOR_NAME = "android.app.ActivityThread";
    public static final String CURRENT_APPLICATION = "currentApplication";
    public static final String GET_INITIAL_APPLICATION = "getInitialApplication";

    private SdkAppGlobal(){

    }
    //https://developer.aliyun.com/article/834303
    public static Application getApplication(){
        Application application = null;
        try{
            //ActivityThread中静态方法拿到application对象的引用
            Class activityThreadClass = Class.forName(CLASS_FOR_NAME);
            Method method = activityThreadClass.getDeclaredMethod(CURRENT_APPLICATION);
            method.setAccessible(true);
            application = (Application) method.invoke(null);
        }catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                |NoSuchMethodException | SecurityException | ClassNotFoundException e){
            Log.w(TAG, "getApplication: " + e);
        }

        if(application != null){
         return application;
        }

        try{
            Class atClass = Class.forName(CLASS_FOR_NAME);
            Method method = atClass.getDeclaredMethod(GET_INITIAL_APPLICATION);
            method.setAccessible(true);
            application = (Application) method.invoke(null);
        }catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                |NoSuchMethodException | SecurityException | ClassNotFoundException e){
            Log.w(TAG, "getApplication: " + e);
        }

        return application;

    }

}
