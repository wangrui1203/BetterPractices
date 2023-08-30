通过 Jetpack Compose 实现一个自定义控件所需要的基本步骤有：

* 通过 BoxWithConstraints 拿到父项的约束条件，即以此拿到控件允许占有的最大空间和最小空间，包括：minWidth、maxWidth、minHeight、maxHeight 等
* 通过 Canvas() 函数来调用 drawLine、drawPath 等 API，绘制自定义图形
* 对于一些 Jetpack Compose 目前还不支持的绘制功能，可以通过 drawIntoCanvas 方法拿到原生 Android 环境的 Canvas 和 Paint 对象，利用原生 Android 环境的 API 方法来实现部分绘制需求
* 将上述操作封装为可组合函数，以函数入参参数的形式向外暴露必要的绘制参数，该可组合函数即我们最终实现的自定义 View 了

在 Jetpack Compose 体系架构下，实现自定义控件的步骤和原生方式相比有着挺大的差别。最终实现的控件对应的也是一个可组合函数，而非一个类。而且我们不用再多在意控件本身的可见性和生命周期了，因为 Jetpack  Compose 会负责以高效的方式创建和释放对象，即使我们使用到了 Animator，Jetpack Compose 也会在可组合函数的生命周期结束的时候就自动停止动画




