package com.example.myview.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * 左划删除控件
 * 能在控件实现左滑吗？如何传入自定义的布局？
 * 思路：
 * 1、一个容器，左右两部分，左边外部导入，右边删除框 x 增加层级
 * 2、在 View 右边追加一个删除款 x 需要在 View 内拦截事件
 * 3、在 ConstraintLayout 内部添加一个删除框，左边对其 parent 右边
 *
 */
class LeftDeleteItemLayout : ConstraintLayout {
    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

}