// ISdk.aidl
package com.ray.standardsdk;

// Declare any non-default types here with import statements

interface ISdk {
    IBinder getService(int servicetype) = 1;
}