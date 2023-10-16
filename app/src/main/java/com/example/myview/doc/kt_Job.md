
协程使用
https://juejin.cn/post/6844904098899181582

阻塞/非阻塞/并发
https://juejin.cn/post/6844903972755472391


## 协程取消
在一个长时间运行的应用程序中，我们可能需要对协程进行细粒度控制。例如，用户可能关闭了启动了协程的页面，现在不再需要其运行结果，此时就应该主动取消协程。launch 函数的返回值 Job 对象就可用于取消正在运行的协程

```kotlin

fun main() = runBlocking {
//sampleStart
    val job = launch {
        repeat(1000) { i ->
            println("job: I'm sleeping $i ...")
            delay(500L)
        }
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancel() // cancels the job
    job.join() // waits for job's completion 
    println("main: Now I can quit.")
    //sampleEnd    
}
```


运行结果
```kotlin
job: I'm sleeping 0 ...
job: I'm sleeping 1 ...
job: I'm sleeping 2 ...
main: I'm tired of waiting!
main: Now I can quit.
```

只要 main 函数调用了 job.cancel，我们就看不到 job 协程的任何输出了，因为它已被取消。还有一个 Job 的扩展函数 cancelAndJoin ，它结合了 cancel 和 join 的调用。

cancel() 函数用于取消协程，join() 函数用于阻塞等待协程执行结束。之所以连续调用这两个方法，是因为 cancel() 函数调用后会马上返回而不是等待协程结束后再返回，所以此时协程不一定是马上就停止了，为了确保协程执行结束后再执行后续代码，此时就需要调用 join() 方法来阻塞等待。可以通过调用 Job 的扩展函数 cancelAndJoin() 来完成相同操作
```kotlin
public suspend fun Job.cancelAndJoin() {
    cancel()
    return join()
}
```

## 使包含计算代码的协程取消
有两种方法可以使计算类型的代码可以被取消。第一种方法是定期调用一个挂起函数来检查取消操作，yieid() 函数是一个很好的选择。另一个方法是显示检查取消操作。让我们来试试后一种方法

## finally关闭资源
可取消的挂起函数在取消时会抛出 CancellationException，可以用常用的方式来处理这种情况。例如，try {...} finally {...} 表达式和 kotlin 的 use 函数都可用于在取消协程时执行回收操作

## 运行不可取消的代码
如果在上一个示例中的 finally 块中使用挂起函数，将会导致抛出 CancellationException，因为此时协程已经被取消了（例如，在 finally 中先调用 delay(1000L) 函数，将导致之后的输出语句不执行）。通常这并不是什么问题，因为所有性能良好的关闭操作（关闭文件、取消作业、关闭任何类型的通信通道等）通常都是非阻塞的，且不涉及任何挂起函数。但是，在极少数情况下，当需要在取消的协程中调用挂起函数时，可以使用 withContext 函数和 NonCancellable 上下文将相应的代码包装在 withContext(NonCancellable) {...} 代码块中

## 超时
大多数情况下，我们会主动取消协程的原因是由于其执行时间已超出预估的最长时间。虽然我们可以手动跟踪对相应 Job 的引用，并在超时后取消 Job，但官方也提供了 withTimeout 函数来完成此类操作。

通过使用 try{...}catch（e:TimeoutCancellationException）{...} 代码块来对任何情况下的超时操作执行某些特定的附加操作，或者通过使用 withTimeoutOrNull 函数以便在超时时返回 null 而不是抛出异常

```kotlin
import kotlinx.coroutines.*

fun main() = runBlocking {
    //sampleStart
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "Done" // will get cancelled before it produces this result
    }
    println("Result is $result")
    //sampleEnd
}
```
此时将不会打印出异常信息
I'm sleeping 0 ...
I'm sleeping 1 ...
I'm sleeping 2 ...
Result is null

## async并发
1）直接并发
2）惰性并发
    ` val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }`
    `one.start()`
3)结构化并发

## 调试
https://juejin.cn/post/6844904100103094280
线程间切换

## flow异步


    


