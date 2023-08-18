package com.example.myview.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs

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
    private val mDeleteView: View?

    var mDeleteClickListener: OnClickListener? = null
        set(value) {
            field = value
            mDeleteView?.setOnClickListener(value)
        }

    //流畅滑动
    private var mScroller = Scroller(context)

    //上次事件的横坐标
    private var mLastX = -1f
    private var mLastY = 0f
    // 滑动模式，左滑模式(-1)，初始化(0)，上下模式(1)
    private var mScrollMode = 0


    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes)

    init {
        //kotlin的初始化函数
        mDeleteView = makeDeleteView(context)
        addView(mDeleteView)
    }

    //创建删除框，设置好位置对齐自身最右边
    private fun makeDeleteView(context: Context): View {
        val deleteView = TextView(context)

        //给当前控件一个id，用于删除控件约束
        this.id = generateViewId()

        //设置布局参数
        deleteView.layoutParams = LayoutParams(
            dp2px(context, 100f), 0
        ).apply {
            //设置约束条件
            leftToRight = id
            topToTop = id
            bottomToBottom = id
        }

        //设置其他参数
        deleteView.text = "删除"
        deleteView.gravity = Gravity.CENTER
        deleteView.setTextColor(Color.WHITE)
        deleteView.textSize = sp2px(context,13f).toFloat()
        deleteView.setBackgroundColor(Color.RED)

        //设置点击回调
        deleteView.setOnClickListener(mDeleteClickListener)

        return deleteView
    }

    //拦截事件
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            //down事件记录x，不拦截，当move的时候才会用到
            MotionEvent.ACTION_DOWN -> {
                mLastX = event.x
                // 对滑动冲突处理
                mLastY = event.y
                mScrollMode = 0
            }
            //拦截本控件内的移动事件
            // 不能拦截，拦截会导致子控件onClick无法生效，onClick需要在ACTION_UP时触发
//            MotionEvent.ACTION_MOVE -> return true
        }
        return super.onInterceptTouchEvent(event)
    }

    //处理事件
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 防止滑出view范围收不到ACTION_MOVE事件
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (mScrollMode == 0) {
                    val deltaX = abs(event.x - mLastX)
                    val deltaY = abs(event.y - mLastY)
                    // 异常情况忽略了
                    if (deltaX == deltaY && deltaX == 0f) return super.onTouchEvent(event)
                    // 判断模式，进入左滑状态(-1)，上下滑动(1)
                    mScrollMode = if (deltaX > deltaY) -1 else 1
                }

                // 左滑模式下交给当前控件处理
                if (mScrollMode < 0) {
                    moveItem(event)
                    return true
                }else {
                    // 这里不处理滑动事件，交个父控件(即RecyclerView)去处理
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                }
            }
            MotionEvent.ACTION_UP -> stopMove()
        }
        return super.onTouchEvent(event)
    }

    private fun moveItem(e: MotionEvent) {
        Log.e("TAG", "moveItem: mLastX=$mLastX")
        //如果没有收到down事件，不应该移动
        if (mLastX == -1f) return

        val dx = mLastX - e.x
        //更新点击的横坐标
        mLastX = e.x
        //检查mItem移动后应该在[-deleteLength, 0]内
        val deleteWidth = mDeleteView!!.width
        if ((scrollX + dx) <= deleteWidth && (scrollX + dx) >= 0) {
            //触发移动
            scrollBy(dx.toInt(), 0)
        }

        // 菜的扣脚，ACTION_DOWN会接管整个事件序列，需要配合requestDisallowInterceptTouchEvent
        //如果一段时间没有移动时间，mLastX还没被stopMove重置为-1，那就是移动到其他地方了
        //设置200毫秒没有新事件就触发stopMove
//        removeCallbacks(stopMoveRunnable)
//        postDelayed(stopMoveRunnable, 200)
    }

    private fun stopMove() {
        //如果移动过半了，应该判定左滑成功
        val deleteWidth = mDeleteView!!.width
        if (abs(scrollX) >= deleteWidth / 2f) {
            //触发移动至完全展开
            mScroller.startScroll(scrollX, 0, deleteWidth - scrollX, 0)
        }else {
            //如果移动没过半应该恢复状态，则恢复到原来状态
            mScroller.startScroll(scrollX, 0, - scrollX, 0)
        }

        invalidate()
        //清除状态
        mLastX = -1f
    }

    //流畅地滑动
    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            invalidate()
        }
    }

    //单位转换
    @Suppress("SameParameterValue")
    private fun dp2px(context: Context, dpVal: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dpVal, context.resources
                .displayMetrics
        ).toInt()
    }

    @Suppress("SameParameterValue")
    private fun sp2px(context: Context, spVal: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spVal * fontScale + 0.5f).toInt()
    }

}