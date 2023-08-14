aidl-module
在实际工作中，强烈建议将 AIDL 的接口封装到一个独立的工程（Module）中，使用时将该工程编译成一个jar包，再交给其它模块使用。这样做可以避免需要同时在APP工程以及Service工程中定义AIDL接口的情况，也方便我们后期的维护。

# 几大问题

## 1. 数据类型
AIDL 数据类型
上述示例中，我们使用AIDL传递的是最简单的int型数据，AIDL不仅支持int型数据，AIDL支持的数据类型有：


Java编程语言中的所有原始类型（如int、long、char、boolean等）


String和CharSequence


List，只支持ArrayList,里面每个元素都必须能够被AIDL支持


Map，只支持HashMap,里面的每个元素都必须被AIDL支持,包括key和value


Parcelable，所有实现了Parcelable接口的对象


Serializable，所有实现了Serializable接口的对象（不能独立传输）


AIDL，所有的AIDL接口本身也可以在AIDL文件中使用

### Parcelable
在安卓中非基本数据类型的对象，除了String和CharSequence都是不可以直接通过AIDL进行传输的，需要先进行序列化操作。序列化就是将对象转换为可存储或可传输的状态，序列化后的对象可以在网络上进行传输，也可以存储到本地。
Parcelable 是安卓实现的可序列化接口。它假定一种特定的结构和处理方式，这样一个实现了 Parcelable接口的对象可以相对快速地进行序列化和反序列化。
在接下来的例子中，我们定义一个Sample对象，并实现Parcelable接口将其序列化，在Android Studio上通过插件Android Parcelable Code Generator，我们可以很快速的将一个对象序列化，而不用自行编写代码。

紧接着我们只需要在需要序列化的类中，右键->generate->parcelable 选中需要序列化的成员变量，即可完成对象的序列化。
然后在aidl目录下同样的包名里创建Sample.aidl文件，这样Android SDK就能识别出Sample对象。
Sample.aidl文件内容如下：
```java
// Sample.aidl
package com.wj.sdk.bean;

parcelable Sample;
```
在将需要传输的对象序列化后，我们在ICalculator.aidl中定义一个新的方法，并将Sample通过AIDL接口传递给「服务端」。

```java
// ICalculator.aidl
package com.wj.sdk;

import com.wj.sdk.bean.Sample;

interface ICalculator {
  void optionParcel(in Sample sample);
  }

```

### Serializable
Serializable 是 Java 提供的一个序列化接口，它是一个空接口，为对象提供标准的序列化和反序列化操作。使用 Serializable 来实现序列化相当简单，只需对象实现了Serializable 接口即可实现默认的序列化过程。Serializable 的序列化和反序列化过程由系统自动完成。
AIDL虽然支持Serializable序列化的对象，但是并不能直接在AIDL接口中传递Serializable的对象，必须放在一个Parcelable对象中传递。

### Parcelable & Serializable 对比
Serializable 虽然使用简单，但是在AIDL中并不推荐使用，因为Serializable 使用了反射机制，效率较低，而且会产生大量的临时变量，增加内存开销。而Parcelable直接在内存中进行读写，效率较高，而且没有额外的开销。
一般来说，如果需要将数据通过网络传输或者持久化到本地，建议使用Serializable，如果只是在应用内部进行数据传递，则建议使用Parcelable。


## 2. 数据流向
在上面的ICalculator.aidl中，addOptParcelable()方法中出现了in、out、inout这些关键字，是因为在传递序列化参数时，必须定义这些参数的数据流方向，in、out、inout关键字的影响主要体现在参数对象在传输过程中是否被复制和修改。具体来说：


in：表示数据从客户端流向服务端，客户端会将参数对象复制一份并发送给服务端，服务端收到后可以对该对象进行修改，但不会影响客户端的原始对象 。


out：表示数据从服务端流向客户端，客户端会将参数对象的空引用发送给服务端，服务端收到后可以创建一个新的对象并赋值给该引用，然后返回给客户端，客户端会将原始对象替换成服务端返回的对象 。


inout：表示数据双向流动，客户端会将参数对象复制一份并发送给服务端，服务端收到后可以对该对象进行修改，并将修改后的对象返回给客户端，客户端会将原始对象替换成服务端返回的对象 。


使用这些关键字时，需要注意以下几点：


如果参数对象是不可变的（如String），则不需要使用out或inout关键字，因为服务端无法修改其内容 。


如果参数对象是可变的（如List或Map），则需要根据实际需求选择合适的关键字，以避免不必要的数据拷贝和传输 。


如果参数对象是自定义的Parcelable类型，则需要在其writeToParcel()和readFromParcel()方法中根据flags参数判断是否需要写入或读取数据，以适应不同的关键字 。



## 3. 传递复杂数据或大量数据-Bundle
AIDL支持传递一些基本类型和 Parcelable 类型的数据。如果需要传递一些复杂的对象或者多个对象以及数量不定的对象时，可以使用 Bundle 类来封装这些数据，然后通过 AIDL 接口传递Bundle对象。Bundle类是一个键值对的容器，它可以存储不同类型的数据，并且实现了Parcelable接口，所以可以在进程间传输。
如果AIDL接口包含接收Bundle作为参数（预计包含 Parcelable 类型）的方法，则在尝试从Bundle读取之前，请务必通过调用 Bundle.setClassLoader(ClassLoader) 设置Bundle的类加载器。否则，即使在应用中正确定义 Parcelable 类型，也会遇到 ClassNotFoundException。例如，
```java
// ICalculator.aidl
package com.wj.sdk;

interface ICalculator {
    void optionBundle(in Bundle bundle);
}
```

如下方实现所示，在读取Bundle的中数据之前，ClassLoader 已在Bundle中完成显式设置。
```java
@Override
public void optionBundle(final Bundle bundle) throws RemoteException {
    Log.i(TAG, "optionBundle: " + bundle.toString());
    bundle.setClassLoader(getClassLoader());
    Sample2 sample2 = (Sample2) bundle.getSerializable("sample2");
    Log.i(TAG, "optionBundle: " + sample2.toString());
    Sample sample = bundle.getParcelable("sample");
    Log.i(TAG, "optionBundle: " + sample.toString());
}

```

为什么需要设置类加载器？因为Bundle对象可能包含其他的Parcelable对象，而这些对象的类定义可能不在默认的类加载器中。设置类加载器可以让Bundle对象正确地找到和创建Parcelable对象。
例如，如果你想传递一个Android系统的NetworkInfo对象，你需要在AIDL文件中声明它是一个Parcelable对象：

然后，在客户端和服务端的代码中，你需要在获取Bundle对象之前，设置类加载器为NetworkInfo的类加载器：
```java
Bundle bundle = data.readBundle();
bundle.setClassLoader(NetworkInfo.class.getClassLoader());
NetworkInfo networkInfo = bundle.getParcelable("network_info");
```
这样，Bundle对象就可以正确地反序列化NetworkInfo对象了。

## 4. 传递大文件

众所周知，AIDL是一种基于Binder实现的跨进程调用方案，Binder 对传输数据大小有限制，传输超过 1M 的文件就会报 android.os.TransactionTooLargeException 异常。不过我们依然有大文件传输的解决方案，其中一种解决办法是，使用AIDL传递文件描述符ParcelFileDescriptor，来实现超大型文件的跨进程传输。
该部分内容较多，可以查看我之前写的文章：Android 使用AIDL传输超大型文件 - 掘金
https://juejin.cn/post/7218615271384088633


## 5. AIDL引起的ANR
Android AIDL 通信本身是一个耗时操作，因为它涉及到进程间的数据传输和序列化/反序列化的过程。如果在「客户端」的主线程中调用 AIDL 接口，而且「服务端」的方法执行比较耗时，就会导致「客户端」主线程被阻塞，从而引发ANR。
为了避免 AIDL 引起的 ANR，可以采取以下这些措施：

不要在主线程中调用 AIDL 接口，而是使用子线程或者异步任务来进行 IPC。
不要在 onServiceConnected () 或者 onServiceDisconnected () 中直接操作服务端方法，因为这些方法是在主线程中执行的。
使用oneway键字来修饰 AIDL 接口，使得 IPC 调用变成非阻塞的。

oneway 简介
不要在主线程中直接调用「服务端」的方法，这个很好理解，我们主要来看oneway。oneway 是AIDL定义接口时可选的一个关键字，它可以修饰 AIDL 接口中的方法，修改远程调用的行为。
oneway主要有以下两个特性：

将远程调用改为「异步调用」，使得远程调用变成非阻塞式的，客户端不需要等待服务端的处理，只是发送数据并立即返回。
oneway 修饰方法，在同一个IBinder对象调用中，会按照调用顺序依次执行。

使用场景
使用oneway的场景一般是当你不需要等待服务端的返回值或者回调时，可以提高 IPC 的效率。
oneway可以用来修饰在interface之前，这样会让interface内所有的方法都隐式地带上oneway，也可以修饰在interface里的各个方法之前。
例如：例如，你可能需要向服务端发送一些控制命令或者通知，而不关心服务端是否处理成功。
```java
// ICalculator.aidl
package com.wj.sdk;

interface ICalculator {
    oneway void optionOneway(int i);
}

```
或直接将oneway添加在interface前。
```java
// ICalculator.aidl
package com.wj.sdk;

oneway interface ICalculator {
    void optionOneway(int i);
}

```

### 注意事项
给AIDL接口添加oneway关键词有以下的事项需要注意：

oneway 修饰本地调用没有效果，仍然是同步的，「客户端」需要等待「服务端」的处理。

本地调用是指「客户端」和「服务端」在同一个进程中，不需要进行 IPC 通信，而是直接调用 AIDL 接口的方法。这种情况下，oneway就失效了，因为没有进程间的数据传输和序列化/反序列化的过程，也就没有阻塞的问题。

oneway 不能用于修饰有返回值的方法，或者抛出异常，因为「客户端」无法接收到这些信息。
同一个IBinder对象进行oneway调用，这些调用会按照原始调用的顺序依次执行。不同的IBinder对象可能导致调用顺序和执行顺序不一致。

同一个IBinder对象的oneway调用，会按照调用的顺序依次执行，这是因为内核中每个IBinder对象都有一个oneway事务的队列，只有当上一个事务完成后才会从队列中取出下一个事务。也是因为这个队列的存在，所以不同IBinder对象oneway调用的执行顺序，不一定和调用顺序一致。

oneway 要谨慎用于修饰调用极其频繁的IPC接口

当「服务端」的处理较慢，但是「客户端」的oneway调用非常频繁时，来不及处理的调用会占满binder驱动的缓存，导致transaction failed












