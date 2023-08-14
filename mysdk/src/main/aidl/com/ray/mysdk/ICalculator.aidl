// ICalculator.aidl
package com.ray.mysdk;

import com.ray.mysdk.listener.ICalculatorListener;
import com.ray.mysdk.bean.Sample;

interface ICalculator {
      //基本使用
      int add(int a, int b);
      int subtract(int a, int b);
      int multiply(int a, int b);
      int divide(int a, int b);
      //自定义数据实现Parcel
      void optionParcel(in Sample sample);
      //bundle方式
      void optionBundle(in Bundle bundle);
      //超大文件传输
      void transactFileDescriptor(in ParcelFileDescriptor pfd);
      //oneway方式，防止耗时anr
      oneway void optionOneway(int i);

      oneway void registerListener(ICalculatorListener listener);
      oneway void unregisterListener(ICalculatorListener listener);
      //binderpool
      IBinder queryBinder(int type);
      //权限验证
      oneway void optionPermission(int i);
}