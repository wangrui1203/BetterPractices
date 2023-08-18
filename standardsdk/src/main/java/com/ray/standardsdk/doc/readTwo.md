
## aidl封装
「服务端」在对外提供业务能力时，不可能要求每个调用方自己编写AIDL并实现Service的绑定逻辑，所以我们必须将AIDL封装成SDK提供给外部使用。在封装SDK时一般需要遵守以下原则：

* 简化「客户端」的调用成本
* 隐藏Service重连机制，使调用方无需关心Service重连的具体实现
* 减少「客户端」与「服务端」的不必要的通信次数，提高性能
* 根据需要进行权限验证


## 结构
audio下是具体的业务

base下是可以复用的类：

/SdkAppGlobal 适用于反射获取外部app的context

/SdkManagerBase 适用于具体的业务逻辑继承

/SdkBase 根据以上四个原则进行封装
* 为了让子类能够更方便地实现与服务端的连接。
* 内部实现Service重连机制，并对外暴露connect()、disconnect()、isConnected()等方法

其中SdkManager是一个管理类，是SdkBase的子类，对客户端提供统一对入口

### 1.SdkBase
把客户端对服务端的绑定、重连、线程切换等细节封装到SDK中，使用时，
继承SdkBase并传入Service包名、类名、和期望的断线重连时间即可。
例如SdkManager，是一个使用例子，再次封装，作为一个入口。







