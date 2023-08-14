开发手机APP时我们一般都是写一个独立的应用，很少会涉及到除了系统服务以外的多个进程间交互的情况，但开发车载应用则不同，随着车载系统需求复杂程度的逐渐提升，现代的车载应用或多或少都会涉及多进程间的交互。
#https://juejin.cn/post/7221328463692120119
#https://juejin.cn/post/7236009756530933819

# 1.AIDL简介

## 1.1简介
AIDL 全称Android 接口定义语言（Android Interface Definition Language），是一种用于定义客户端和服务端之间的通信接口的语言，它可以让不同进程之间通过IPC（进程间通信）进行数据交互。
在 Android 系统中一个进程通常无法直接访问另一个进程的内存空间，这被称为Application Sandbox。因此，为了实现进程间通信，Android系统提供了用于实现跨进程通信的协议，但是实现通信协议往往比较复杂，需要将通信数据进行编组和解组，使用AIDL可以让上述操作变得简单。
AIDL的架构可以看作是一种CS（Client-Server）架构，即客户端-服务端架构。简单介绍如下：
1）「客户端」是指需要调用「服务端」提供的数据或功能的应用，它通过绑定「服务端」的Service来获取一个IBinder对象，然后通过该对象调用「服务端」暴露出来的接口方法 。
2）「服务端」是指提供数据或功能给「客户端」的应用，它通过创建一个Service并在onBind()方法中返回一个IBinder对象来实现通信接口，该对象需要重写.aidl文件中定义的接口方法 。
3）「客户端」和「服务端」需要共享一个.aidl文件，用来声明通信接口和方法，该文件会被Android SDK工具转换成一个Java接口，该接口包含一个Stub类和一个Proxy类 。

## 1.2使用场景
Android 系统中的 IPC不只是有AIDL，Android系统还提供了以下几种常用的 IPC 的方式：

Messenger

一种基于AIDL的IPC通信的方式，它对AIDL进行了封装，简化了使用过程，只需要创建一个Handler对象来处理消息。Messenger只支持单线程串行请求，只能传输Message对象，不能传输自定义的Parcelable对象。

ContentProvider

一种用于提供数据访问接口的IPC通信的方式，它可以让不同进程之间通过URI和Cursor进行数据交互。ContentProvider可以处理多线程并发请求，可以传输任意类型的数据，但使用过程比较繁琐，需要实现多个方法。

Socket

一种基于TCP/IP协议的IPC通信的方式，它可以让不同进程之间通过网络套接字进行数据交互。Socket可以处理多线程并发请求，可以传输任意类型的数据，但使用过程比较底层，需要处理网络异常和安全问题。
我们可以根据不同的场景和需求，选择合适的IPC的方式。一般来说：


如果需要实现跨应用的数据共享，可以使用ContentProvider。


如果需要实现跨应用的功能调用，可以使用AIDL。


如果需要实现跨应用的消息传递，可以使用Messenger。


如果需要实现跨网络的数据交换，可以使用Socket。


接下来，我们通过代码来实践一个 AIDL 通信的示例。


## 1.3 实践
在实际工作中，强烈建议将 AIDL 的接口封装到一个独立的工程（Module）中，使用时将该工程编译成一个jar包，再交给其它模块使用。这样做可以避免需要同时在APP工程以及Service工程中定义AIDL接口的情况，也方便我们后期的维护。


# 2.实例

## 2.1 简单计算器

在编写示例之前，先做出需求定义。
假设我们有一个「服务端」，提供一个计算器的功能，可以进行加减乘除等多种运算。我们想让其他「客户端」应用也能调用这个「服务端」，进行计算，我们可以按照以下步骤来实现：
第 1 步，创建SDK工程，定义 AIDL 接口
在实际工作中，强烈建议将 AIDL 的接口封装到一个独立的工程（Module）中，使用时将该工程编译成一个jar包，再交给其它模块使用。这样做可以避免需要同时在APP工程以及Service工程中定义AIDL接口的情况，也方便我们后期的维护。
在SDK工程中，定义一个AIDL接口，声明我们想要提供的方法和参数。例如，我们可以创建一个ICalculator.aidl文件，内容如下：
```java
interface ICalculator {
int add(int a, int b);
int subtract(int a, int b);
int multiply(int a, int b);
int divide(int a, int b);
}
```

第 2 步，创建 Service 工程，实现AIDL接口
在「服务端」应用中，创建一个Service类，实现AIDL接口，并在onBind方法中返回一个IBinder对象。例如，我们可以创建一个CalculatorService类，内容如下：
```java
public class CalculatorService extends Service {

private final Calculator.Stub mBinder = new Calculator.Stub() {
    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }

    @Override
    public int subtract(int a, int b) throws RemoteException {
      return a - b;
    }

    @Override
    public int multiply(int a, int b) throws RemoteException {
      return a * b;
    }

    @Override
    public int divide(int a, int b) throws RemoteException {
      if (b == 0) {
        throw new IllegalArgumentException("Divisor cannot be zero");
      }
      return a / b;
    }
};

@Override
public IBinder onBind(Intent intent) {
return mBinder;
}
}
```
在「服务端」应用中，注册Service，并设置android:enabled和android:exported属性为true，以便其他应用可以访问它。
如果需要还可以添加一个intent-filter，指定一个action，让其他应用可以通过intent启动服务，同时服务端也可以通过读取intent中的action来过滤绑定请求。
例如，在AndroidManifest.xml文件中，我们可以添加以下代码：
```xml
<service
android:name=".CalculatorService"
android:enabled="true"
android:exported="true">
<intent-filter>
<action android:name="com.example.calculator.CALCULATOR_SERVICE" />
</intent-filter>
</service>
```
在Android 8.0之后的系统中，Service启动后需要添加Notification，将Service设定为前台Service，否则会抛出异常。
```java

@Override
public void onCreate() {
super.onCreate();
Log.e(TAG, "onCreate: ");
startServiceForeground();
}

private static final String CHANNEL_ID_STRING = "com.wj.service";
private static final int CHANNEL_ID = 0x11;

private void startServiceForeground() {
NotificationManager notificationManager = (NotificationManager)
getSystemService(Context.NOTIFICATION_SERVICE);
NotificationChannel channel;
channel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name),
NotificationManager.IMPORTANCE_LOW);
notificationManager.createNotificationChannel(channel);
Notification notification = new Notification.Builder(getApplicationContext(),
CHANNEL_ID_STRING).build();
startForeground(CHANNEL_ID, notification);
}
```

第 3 步，创建客户端工程，调用AIDL接口
在「客户端」应用中，创建一个ServiceConnection对象，实现onServiceConnected和onServiceDisconnected方法，在onServiceConnected方法中获取IBinder对象的代理，并转换为AIDL接口类型。 
```java
private ICalculator mCalculator;

private ServiceConnection mConnection = new ServiceConnection() {
@Override
public void onServiceConnected(ComponentName name, IBinder service) {
mCalculator = ICalculator.Stub.asInterface(service);

        // 计算 3*6
        calculate('*',3,6);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mCalculator = null;
    }
};
```
在使用计算器功能的应用中，绑定提供计算器功能的应用的Service，并传递一个Intent对象，指定提供计算器功能的应用的包名和Service类名。如果提供计算器功能的应用设置了intent-filter，还需要指定相应的action。
```java
private void bindToServer() {
Intent intent = new Intent();
intent.setAction("com.wj.CALCULATOR_SERVICE");
intent.setComponent(new ComponentName("com.wj.service", "com.wj.service.CalculatorService"));
boolean connected = bindService(intent, mConnection, BIND_AUTO_CREATE);
Log.e(TAG, "onCreate: " + connected);
}
```
获取到IBinder对象的代理后就可以通过该对象调用「服务端」提供的方法了。
```java
private void calculate(final char operator, final int num1, final int num2) {
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
}
```
注意，从Android 11 开始，系统对应用的可见性进行了保护，如果 build.gradle 中的Target API > = 30，那么还需要在 AndroidManifest.xml 配置queries标签指定「服务端」应用的包名，才可以绑定远程服务。
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <queries>
        <package android:name="com.wj.service"/>
    </queries>

</manifest>
```






