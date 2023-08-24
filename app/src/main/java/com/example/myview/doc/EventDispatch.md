
事件分发

https://juejin.cn/post/6931914294980411406?searchId=20230821092534B2A0CD77B0D7597CD39D

在整个事件分发过程中，我们主要接触的是 ViewGroup 和 View 这两种视图类型。一次完整的事件分发过程会包括三个阶段，即事件的发布、拦截和消费，这三个过程分别对应声明在 View 和 ViewGroup 中的三个方法


## 发布
```java
public boolean dispatchTouchEvent(MotionEvent ev)
```
Android 中的视图（View、ViewGroup、Activity 等）接收到的触摸事件都是通过这个方法来进行分发的，如果事件能够传递给当前视图，则此方法一定会被调用，即视图接收到的触摸事件都需要通过该方法来进行分发。该方法的返回值用于表明该视图或者内嵌视图是否消费了该事件。如果当前视图类型是 ViewGroup，该方法内部会调用 onInterceptTouchEvent(MotionEvent)方法来判断是否拦截该事件

## 拦截
事件的拦截对应着如下方法
```java
public boolean onInterceptTouchEvent(MotionEvent ev)
```
ViewGroup 包含该方法，View 中不存在。该方法通过返回值来标明是否需要拦截对应的事件。返回 true 则表示拦截这个事件，不继续发布给子视图，并将事件交由自身的 onTouchEvent(MotionEvent event) 方法来进行处理；返回 false 则表示不拦截事件，继续传递给子视图。如果 ViewGroup 拦截了某个事件，那么在同一个事件序列当中，此方法不会被再次调用

## 消费
```java
public boolean onTouchEvent(MotionEvent event)
```
该方法返回 true 表示当前视图已经处理了对应的事件，事件将在这里完成消费，终止传递；返回 false 表示当前视图不处理这个事件，事件会被传递给其它视图

## 三者联系
ViewGroup 完整包含以上三个过程，而 View 只包含分发和消费两个，既 View 类不包含 onInterceptTouchEvent(MotionEvent) 方法。三个方法之间的联系可以用如下伪代码来表示：
```kotlin
    fun dispatchTouchEvent(event: MotionEvent): Boolean {
        var consume = false
        consume = if (onInterceptTouchEvent(event)) {
            onTouchEvent(event)
        } else {
            child.dispatchTouchEvent(event)
        }
        return consume
    }

```
当触摸事件发生时，事件分发流程会按照如下执行：

根 ViewGroup 最先接收到 MotionEvent，其 dispatchTouchEvent 方法会被调用到，该方法内部会调用 onInterceptTouchEvent 方法来判断是否要拦截事件
ViewGroup 的 onInterceptTouchEvent 方法如果返回 true，则表示当前 ViewGroup 要拦截事件，否则就会去调用 child（内嵌的 ViewGroup 或者是 View）重复分发过程
View 和 ViewGroup 的 onTouchEvent 方法用来判断是否要消费该事件，如果返回了 true 则表示事件已被消费，终止传递

当然，View 的事件分发过程不是上述介绍的那么简单，实际上事件的流转过程很复杂，根据每个方法返回值的不同，事件序列的流转方向会有很大差异。


## viewGroup拦截事件
* Activity 会早于各个 ViewGroup 和 View 接收到触摸事件，ViewGroup 和 View 没有消费掉的 ACTION_DOWN 事件最终还是会交由 Activity 来消化掉

* 由于 ViewGroup 和 View 均没有消费掉 ACTION_DOWN 事件，所以后续的 ACTION_UP 事件不会再继续向它们下发，而是会直接调用 Activity 的 onTouchEvent 方法，由 Activity 来消化掉
* 如果 ViewGroup 自身拦截且消费了 ACTION_DOWN 事件，即 onInterceptTouchEvent 和 onTouchEvent  两个方法均返回了 true，那么本次事件序列的后续事件就都会交由其进行处理（如果能接收得到的话），不会再调用其 onInterceptTouchEvent 方法来判断是否进行拦截，dispatchTouchEvent 方法会直接调用 onTouchEvent 方法
* 而如果 ViewGroup 拦截了 ACTION_DOWN 事件，但是 onTouchEvent 方法中又没有消费掉该事件的话，那么本次事件序列的后续事件都不会再被其接收到，而是直接交由父视图进行处理。View  对 ACTION_DOWN 事件的处理逻辑也是如此
  
* 如果所有的 ViewGroup 和 View 都没有消耗 ACTION_DOWN 事件的话，则后续事件（ACTION_MOVE 和 ACTION_UP 等）都会直接交由 Activity 进行处理， ViewGroup 和 View 没有机会再接触到后续事件


## view消费事件
* View 没有拦截事件这个过程，但如果有消费掉 ACTION_DOWN 事件的话，后续事件就都可以接收到


## 注意
* View 是否能接收到整个事件序列的消息主要就取决于其是否消费了 ACTION_DOWN 事件，ACTION_DOWN 事件是整个事件序列的起始点，View 必须消耗了起始事件才有机会完整处理整个事件序列
* 在正常情况下，一个事件序列只应该由单独一个 View 或者 ViewGroup 进行处理，既然 MyLinearLayout 已经消费了 ACTION_DOWN 事件，那么后续的事件应该也都交由其进行处理

## 总结
1. Activity 会早于各个 ViewGroup 和 View 接收到触摸事件，Activity 可以通过主动拦截掉各个事件的下发使得 ViewGroup 和 View 接收不到任何事件。而如果 ViewGroup 和 View 接收到了 ACTION_DOWN 事件但没有消费掉，那么事件最终还是会交由 Activity 来消费
2. 当触摸事件被触发时，系统会根据触摸点的坐标系找到根 ViewGroup，然后向底层 View 下发事件，即事件分发流程先是从根 ViewGroup 从上往下（从外向内）向内嵌的底层 View 传递的，如果在这个过程中事件没有被消费的话，最终又会反向传递从下往上（从内向外）进行传递
3. ViewGroup 在接收到 ACTION_DOWN 事件时，其 dispatchTouchEvent 方法内部会先调用 onInterceptTouchEvent 判断是否要进行拦截，如果 onInterceptTouchEvent 方法返回了 false，则意味着其不打算拦截该事件，那么就会继续调用 child 的 dispatchTouchEvent 方法，继续重复以上步骤。如果拦截了，那么就会调用 onTouchEvent 进行消费
4. 如果 ViewGroup 自身拦截且消费了 ACTION_DOWN 事件，那么本次事件序列的后续事件就会都交由其进行处理（如果能接收得到的话），不会再调用其 onInterceptTouchEvent 方法来判断是否进行拦截，也不会再次遍历 child，dispatchTouchEvent 方法会直接调用 onTouchEvent 方法。这是为了尽量避免无效操作，提高系统的绘制效率
5. 如果根 ViewGroup 和内嵌的所有 ViewGroup 均没有拦截 ACTION_DOWN 事件的话，那么事件通过循环传递就会分发给最底层的 View。对于 View 来说，其不包含 onInterceptTouchEvent 方法，dispatchTouchEvent 方法会调用其 onTouchEvent 方法来决定是否消费该事件。如果返回 false，则意味着其不打算消费该事件，事件将依次调用父容器的 onTouchEvent 方法；返回 true 的话则意味着事件被其消费了，事件终止传递
6. 而不管 ViewGroup 有没有拦截 ACTION_DOWN 事件，只要其本身和所有 child 均没有消费掉 ACTION_DOWN 事件，即 dispatchTouchEvent 方法返回了 false，那么此 ViewGroup 就不会再接收到后续事件，后续事件会被 Activity 直接消化掉
7. 而不管是 ViewGroup 还是 View，只要其消费了 ACTION_DOWN 事件，即使 onTouchEvent 方法在处理每个后续事件时均返回了 false，都还是可以完整接收到整个事件序列的消息。后续事件会根据在在处理 ACTION_DOWN 事件保留的引用链，从上往下依次下发
8. View 是否能接收到整个事件序列的消息主要就取决于其是否消费了 ACTION_DOWN 事件，ACTION_DOWN 事件是整个事件序列的起始点，View 必须消耗了起始事件才有机会完整处理整个事件序列
9. 处于上游的 ViewGroup 不关心到底是下游的哪个 ViewGroup 或者 View 消费了触摸事件，只要下游的 dispatchTouchEvent 方法返回了 true，上游就会继续向下游下发后续事件
10. ViewGroup 和 View 对于每次事件序列的消费过程是独立的，即上一次事件序列的消费结果不影响新一次的事件序列

# 滑动冲突
如果父容器和子 View 都可以响应滑动事件的话，那么就有可能发生滑动冲突的情况。解决滑动冲突的方法大致上可以分为两种：外部拦截法 和 内部拦截法
## 外部拦截法
父容器根据实际情况在 onInterceptTouchEvent 方法中对触摸事件进行选择性拦截，如果判断到当前滑动事件自己需要，那么就拦截事件并消费，否则就交由子 View 进行处理。该方式有几个注意点：

ACTION_DOWN 事件父容器不能进行拦截，否则根据 View 的事件分发机制，后续的 ACTION_MOVE 与 ACTION_UP 事件都将默认交由父容器进行处理
根据实际的业务需求，父容器判断是否需要处理 ACTION_MOVE 事件，如果需要处理则进行拦截消费，否则交由子 View 去处理
原则上 ACTION_UP 事件父容器不应该进行拦截，否则子 View 的 onClick 事件将无法被触发

伪代码：
```kotlin
override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
var intercepted = false
when (event.action) {
MotionEvent.ACTION_DOWN -> {
intercepted = false
}
MotionEvent.ACTION_MOVE -> {
intercepted = if (满足拦截要求) {
true
} else {
false
}
}
MotionEvent.ACTION_UP -> {
intercepted = false
}
}
return intercepted
}
```

## 内部拦截法
内部拦截法则是要求父容器不拦截任何事件，所有事件都传递给子 View，子 View 根据实际情况判断是自己来消费还是传回给父容器进行处理。该方式有几个注意点：

父容器不能拦截 ACTION_DOWN 事件，否则后续的触摸事件子 View 都无法接收到
滑动事件的舍取逻辑放在子 View 的 dispatchTouchEvent 方法中，如果父容器需要处理事件则调用 parent.requestDisallowInterceptTouchEvent(false) 方法让父容器去拦截事件

伪代码：
子 View 修改其 dispatchTouchEvent 方法，根据实际需求来控制是否允许父容器拦截事件
```kotlin
override fun dispatchTouchEvent(event: MotionEvent): Boolean {
when (event.action) {
MotionEvent.ACTION_DOWN -> {
//让父容器不拦截 ACTION_DOWN 的后续事件
parent.requestDisallowInterceptTouchEvent(true)
}
MotionEvent.ACTION_MOVE -> {
if (父容器需要此事件) {
//让父容器拦截后续事件
parent.requestDisallowInterceptTouchEvent(false)
}
}
MotionEvent.ACTION_UP -> {
}
}
return super.dispatchTouchEvent(event)
}
```

由于 ViewGroup 的 dispatchTouchEvent 方法会预先判断子 View 是否有要求其不拦截事件，如果没有的话才会调用自身的 onInterceptTouchEvent 方法，所以除了 ACTION_DOWN 外，如果子 View 不拦截的话那么 ViewGroup 都进行拦截
```kotlin
override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
return event.action != MotionEvent.ACTION_DOWN
}

```
分析： 事件序列都是从ACTION_DOWN开始的，
然后1）子view如果拦截并消费了down，后续事件都是直接流向该view，且跳过onInterceptTouchEvent,从dispatchTouchEvent直接走onTouchEvent逻辑，去消费或者不消费。不消费则依次调用父容器的onTouchEvent,消费则事件终止传递
2）子view如果拦截，并未消费down，后续事件都不会再被其接受到，直接交由父视图处理。父视图若也不消费，则继续向上传递。若直到顶层视图，该down事件被消费，则事件序列后续事件都交由该层（顶层视图）进行处理


## 解决滑动冲突
例如ScrollView嵌套ScrollView,内部ScrollView无法单独滑动，跟随外部的滑动

使用内部拦截法
* 首先需要让外部 ScrollView 拦截 ACTION_DOWN 之外的任何事件
* 内部 ScrollView 判断自身是否还处于可滑动状态，如果滑动到了最顶部还想再往下滑动，或者是滑动到了最底部还想再往上滑动，那么就将事件都交由外部 ScrollView 处理，其它情况都直接拦截并消费掉事件，这样内部 ScrollView 就可以实现内部滑动了


1）
```kotlin
class ExternalScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    override fun onInterceptTouchEvent(motionEvent: MotionEvent): Boolean {
        val intercepted: Boolean
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                intercepted = false
                super.onInterceptTouchEvent(motionEvent)
            }
            else -> {
                intercepted = true
            }
        }
        return intercepted
    }

}

```

2）
```kotlin
class InsideScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private var lastX = 0f

    private var lastY = 0f

    override fun dispatchTouchEvent(motionEvent: MotionEvent): Boolean {
        val x = motionEvent.x
        val y = motionEvent.y
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - lastX
                val deltaY = y - lastY
                if (abs(deltaX) < abs(deltaY)) { //上下滑动的操作
                    if (deltaY > 0) { //向下滑动
                        if (scrollY == 0) { //滑动到顶部了
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    } else { //向上滑动
                        if (height + scrollY >= computeVerticalScrollRange()) { //滑动到底部了
                            parent.requestDisallowInterceptTouchEvent(false)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        lastX = x
        lastY = y
        return super.dispatchTouchEvent(motionEvent)
    }

}

```







