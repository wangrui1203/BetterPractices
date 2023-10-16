// INimbleGumService.aidl
package com.ray.mysdk;
import com.ray.mysdk.nimblegums.NimBleGum;
import com.ray.mysdk.INimbleGumCallBack;
// Declare any non-default types here with import statements

interface INimbleGumService {
    void enqueueGum( in String pkg, in NimBleGum nbg, in long id, in int userId, in INimbleGumCallBack cb);

    void cancelGum( in String pkg, in long id, in int userId);

    void updateGum( in String pkg, in NimBleGum nbg, in int userId);

    void sendOperatorAction(in String pkg, in long id, in int userId);

    boolean isShowing(in NimBleGum nbg);
}