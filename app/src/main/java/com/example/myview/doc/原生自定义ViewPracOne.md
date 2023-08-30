在原生的 Android View 体系下，我们要实现一个自定义 View 所需要的基本步骤有：

* 继承 android.view.View，在子类的构造函数中通过 AttributeSet 拿到在 XML 文件中声明的各个属性值，完成一些初始化的操作
* 重写 onMeasure 和 onSizeChanged 两个方法，拿到 View 的宽高信息
* 重写 onLayout 方法，确定子 View 的位置信息（如果是自定义 ViewGroup 的话）
* 重写 onDraw 方法，通过 Paint、Path 等向 Canvas 绘制图形，从而实现各种自定义效果
* 重写 onVisibilityChanged、onAttachedToWindow、onDetachedFromWindow 等方法，在适当的时候开启动画或者停止动画，避免资源浪费和内存泄漏（如果有使用到 Animator 的话）
* 如果自定义view支持触摸事件，那么需要实现触摸事件实现方法，如`onTouchEvent`等

整个流程的重点就是 onDraw 方法了，开发者在这里拿到 Canvas 对象，也即画布，然后通过各种 API 在画布上绘制图形。例如，canvas.drawLine就用于绘制直线，canvas.drawPath 就用于绘制路径

## 分类
自定义 View 大体可以分为三种：改装、组合和自定义
* 改装指的是继承自某个控件，在原有功能的基础上进行增删改，比如：基于 ViewPager 打造一个无限循环的轮播图控件
* 组合指的是将2个以上的控件组合成一个控件，比如：基于 RelativeLayout + 多个 EditText 组合成一个密码输入控件
* 自定义指的是当 Android 官方控件不足以满足业务需求（比如统计图表中的饼状图/折线图）时，继承 View / ViewGroup 类，重写 onMeasure()、onLayout()、onDraw() 三大方法，从 0 到 1 创造一个新的控件



## 注意
### onDraw中技巧
* 使用局部变量：在onDraw方法中创建对象和变量会增加内存分配和垃圾回收的负担。因此，在onDraw方法中使用局部变量可以提高性能。
* 使用缓存：如果我们需要频繁重绘的自定义View，可以使用缓存来提高性能。可以使用Bitmap或者Canvas来进行缓存。
* 使用线程：如果我们需要进行一些耗时的操作，比如网络请求、图片加载等，可以使用线程来避免阻塞UI线程。可以使用AsyncTask或者Handler来开启线程。

例如
```java
public class MyView extends View {

    private Paint mPaint; // 画笔
    private Bitmap mBitmap; // 缓存的Bitmap
    private Canvas mCanvas; // 缓存的Canvas

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(5);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap == null) {
            // 创建缓存的Bitmap和Canvas
            mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
        // 绘制图形到缓存的Canvas上
        mCanvas.drawCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2 - 5, mPaint);
        // 将缓存的Bitmap绘制到View的Canvas上
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }
}

```

### 处理触摸事件技巧
【需要注意资源的释放】
使用GestureDetector：GestureDetector可以帮助我们检测手势，比如单击、双击、长按、滑动等。
使用VelocityTracker：VelocityTracker可以帮助我们计算触摸事件的速度和方向，比如滑动的速度和方向。
使用Scroller：Scroller可以帮助我们实现平滑的滚动效果，比如ListView和ScrollView中的滚动效果。

## 实践

### 1 数字雨
思路：
1）定义显示文本的内容，构建字符串并存储。
2）随机显示，使用随机数进行内容的获取。
3）获取画笔，并设置参数：背景颜色值，文字大小
4）高亮画笔，设置颜色，文字大小
5）onMeasure中测量决定view大小
6）onDraw中绘制操作，按照速度和间隔等参数，进行绘制

内存问题：
运行的时候内存会一点一点的上涨，每次上升大约100kb左右，cpu消耗大约13%左右。
解决方案：
上涨原因主要是将char类型字符通过调用String.value(char)方法转换为字符串造成的，查看String的源码发现每次调用value(char)方法都会调用new String()方法创建新的字符串，于是将字符串提前生成好放入一个String数组里面，问题解决。
现在内存非常稳定，几乎不增加。
