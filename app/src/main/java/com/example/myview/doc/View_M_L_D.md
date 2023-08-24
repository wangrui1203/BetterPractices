https://juejin.cn/post/6939540905581887502

# 一 Measure

`MeasureSpec`int三十二位整数来表示，封装了View的size（后三十位）和mode属性，即为宽高属性，与使用相应测量模式作为依据，调整最终尺寸。

* UNSPECIFIED ViewGroup对于View没有任何限制，View可以拿到任意想要的SIZE
* EXACTLY View本身设定了确切的大小
* AT_MOST size是View能够占据的最大空间，对于wrap_content,View可以在父容器可以容纳的范围内申请空间

`LayoutParams`封装了View的width和height属性，默认解析在布局文件中设置的layout_width,layout_height。也可以外部动态设置

> View的MeasureSpec由其父容器ViewGroup的MeasureSpec和View自身的LayoutParams共同决定
>   * View能够占据的尺寸，肯定收到父容器ViewGroup的影响，一般不会超过ViewGroup容纳范围，除非viewGroup本身支持滑动（如ScrollView）



**DecorView** 是整个视图树的根布局，而 DecorView 是 FrameLayout 的子类，所以说平时我们在 Activity 中 setContentView 其实就是在向 DecorView 执行 addView 操作。很自然地，整个视图树的测量过程就是要从 DecorView 开始，从上到下从外到内进行，DecorView 的尺寸大小就是整个视图树所能占据的最大空间，而 DecorView 的宽高默认都是 match_parent，即占据整个屏幕空间
V

## ViewRootImpl && View
View绘制的整个流程的启动入口，从ViewRootImpl的performTraversals开始看
用于为DecorView生成MeasureSpec

**performTraversals**
* 获取Spec
  * 屏幕的宽高：mWidth,mHeight 
  * DecorView的宽高：lp.width,lp.height
* 测量整个视图树 
  * performMeasure，mView代表DecorView，启动测量流程，measure

**measure-final**
measure final方法，View子类和ViewGroup都无法重写，在其中完成一些通用都调用逻辑之后

=》
**onMeasure**

默认情况下，onMeasure方法会考虑View自身是否设置了minWidth和background,取两者最小作为View最终尺寸的参考依据
然后区分【UNSPECIFIED】和【EXACTLY或AT_MOST】两种情况
将结果，SetMeasuredDimension
> onMeasure方法是自定义View需要重写的方法
onMeasure方法中，自定义View完成自身尺寸的测量逻辑，如ViewGroup除了测量自身外，需要测量所有的childView
> 且在确定最终尺寸时，注意区分【UNSPECIFIED】和【EXACTLY】和【AT_MOST】三种情况

**setMeasuredDimension-final**
setMeasuredDimension也是final方法，不可被重写，把上一步测量获得的
measuredWidth与measuredHeight，进行传递。用于后续Layout阶段，确定最终View的宽高

## ViewGroup流程
不同ViewGroup有不同的布局效果，因此宽高属性的计算规则各有差异，
体现在**onMeasure**方法中
例如FrameLayout
* 考虑FrameLayout自身的padding，minimumSize等 
* 考虑各个childView的measureWidth和measureHeight（其中会考虑到childView的margin，然后考虑到父容器的MeasureSpec测量标准和自身childView的LayoutParams）
* 且FrameLayout需要考虑一种特殊情况：假设 FrameLayout 的 layout_width 设置为 wrap_content，而某个 childView 的 layout_width 设置为 match_parent。此时对于 FrameLayout 来说其宽度并没有确定值，需要依靠所有 childView 来决定。对于该 childView 来说，其希望的是宽度占满整个 FrameLayout。所以此时该 childView 的widthSpecSize就应该是 FrameLayout 当前的 widthMeasureSize，widthSpecMode 应该是 EXACTLY才对。而 FrameLayout 也只有在完成所有 childView 的 measure 操作后才能得到自己的widthMeasureSize，所以第二步逻辑就是来进行补救措施，判断是否需要让 childView 进行第二次 measure

对于 DecorView 来说，其 MeasureSpec 是通过测量屏幕宽高来生成的，这从 ViewRootImpl 的 performTraversals() 方法就可以体现出来
对于 View（包括ViewGroup）来说，其 MeasureSpec 是由其父容器 ViewGroup 的 MeasureSpec 和 View 自身的 LayoutParams 来共同决定的。

重复：父容器向子视图-下发测量要求-接受测量结果

# 二、Layout
Layout 代表的是确定位置的过程，在这个过程中 View 需要计算得出自己在父容器中的显示位置

**performLayout**
View 的 layout 起始点也是从 ViewRootImpl 开始的，ViewRootImpl 的 performLayout 方法会调用

**layout** 启动layout流程

对于`View`，传入的四个参数即我们熟知的 left、top、right、bottom，这四个值都是 View 相对父容器 ViewGroup 的坐标值

对于`DecorView`， 来说这四个值就分别是 0、0、screenWidth、screenHeight

**setFrame** 将四个值保存到View的相应全局变量，用于确定width,height。
回调onSizeChanged接受宽高的变化通知
> 自定义View通过layout？方法，得到View的准确宽高大小，并接受宽高大小的变化通知

**onLayout**
对于ViewGroup来说，
layout方法调用onLayout方法，onLayout方法在View类中是空实现，大部分情况下View无需重写。
ViewGroup中，是抽象方法，每个ViewGroup的子类， 需要实现onLayout来管理所有childView的摆放位置


# 三、Draw
绘制视图，View通过操作Canvas来实现自己的UI效果

**performDraw**
起始点从ViewRootImpl的performDraw方法开始,
用 drawSoftware 方法，再通过调用 DecorView 的 draw 方法来启动 draw 流程

**onDraw**
对于View来说，重写onDraw来绘制内容，实现自己的特定UI，无需关心dispatchDraw

**dispatchDraw**
对于viewGroup来说，除了需要绘制背景色，前景色，无需绘制自身，
所以ViewGroup无需重写onDraw，应使用dispatchDraw，向所有childView下发Draw请求


# 四、问题

## 1.ViewGroup 和 View 的绘制顺序
measure 阶段是先 View 后 ViewGroup
layout 阶段是先 ViewGroup 后 View
draw 阶段是先 ViewGroup 后 View

以 FrameLayout 为例，其 onMeasure 方法就需要先去完成所有 childView 的 measure 操作，得到 maxWidth 和 maxHeight 后才能确定自己的尺寸值

在 layout 阶段，FrameLayout 的 setFrame 方法已经将外部传入的 left、top、right、bottom 等四个值保存起来了，至此 ViewGroup 自身的位置信息就已经确定下来了，之后才会调用 layoutChildren 方法去执行 childView 的 layout 操作

在 draw 阶段，FrameLayout 也是先执行自己的 onDraw 方法后，再去执行 dispatchDraw 方法，这也说明 ViewGroup 是先完成自身的绘制需求后才去绘制 childView，毕竟 ViewGroup 的视图显示层次要比 View 低


## 2.View 多个回调函数的先后顺序
（常用的）
Activity 从开始展示到退出页面这个过程，View 的这五个方法的先后顺序是：

父容器的 onAttachedToWindow 和 onVisibilityChanged 会先后调用，之后才会调用 childView 的这两个方法
childView 的 onDetachedFromWindow 会先被调用，所有 childView 都调用后才会调用父容器的该方法
View 的绘制流程就按照 onMeasure、onLayout、onLayout 的顺序进行，onAttachedToWindow 和 onVisibilityChanged 都会早于这三个方法

## 3.getWidth 和 getMeasuredWidth 的区别
getMeasuredWidth() 和 getMeasuredHeight()返回的是 View 在 measure 阶段的测量结果，用于在 onMeasure 方法后调用。getWidth() 和 getHeight()返回的是 View 的实际宽高值，用于在 onLayout 方法后调用。这两者可以说是 View 在不同阶段下的一个尺寸值，大多数情况下这两个值都是相等的，但 measureWidth 只是相当于一个预估值，View 的最终宽度并不一定遵循该值，View 的实际宽高需要在 layout 阶段才能最终确定下来
例如，我们完全可以通过重写 layout 方法来使得 View 的位置发生偏移，这就可以使得 View 的 width 和 measureWidth 两者不相等
```java
@Override
public void layout(int l, int t, int r, int b) {
super.layout(l, t, r + 10, b + 10);
}
```







