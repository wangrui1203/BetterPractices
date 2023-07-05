package com.example.myview.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Scroller
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import java.lang.Math.abs

class SlideDeleteRecycleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    ): RecyclerView(context, attrs, defStyleAttr){
        //流畅滑动
        private var mScroller = Scroller(context)
        //当前选中的item
        private var mItem: ViewGroup? = null
        //上次按下横坐标
        private var mLastX = 0f
        private val TAG = "SlideDeleteRecycleView"

        override fun onInterceptTouchEvent(e: MotionEvent):Boolean{
            e?.let {
                when(e.action){
                    MotionEvent.ACTION_DOWN -> {
                        //获取点击位置
                        getSelectItem(e)
                        //设置点击的横坐标
                        mLastX = e.x
                    }
                    MotionEvent.ACTION_MOVE -> {
                        //移动控件
                        moveItem(e)
                        //更新点击的横坐标
                        mLastX = e.x
                        //拦截事件
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        //判断结果
                        stopMove(e)
                    }
                }
            }
            return super.onInterceptTouchEvent(e)
        }

    //滑动结束
    //版本一：判断一下结束的位置，补充或者恢复位置
    private fun stopMove(e: MotionEvent) {
        mItem?.let{
            val disX = e.x - mLastX
            //如果移动过半了，应该判定左滑成功
            val deleteWidth = it.getChildAt(it.childCount - 1).width
            if(abs(it.scrollX) >= deleteWidth / 2){
                //触发移动至完全展开
                mScroller.startScroll(it.scrollX, 0, -deleteWidth, 0)
                invalidate()
            }else{
                //如果移动没过半应该恢复状态
                mScroller.startScroll(it.scrollX,0,0,0)
                invalidate()
            }
            //清楚状态
            mLastX = 0f
            mItem = null
        }
    }

    //移动item
    //版本一：绝对值小于删除按钮长度随便移动，大于则不移动
    private fun moveItem(e: MotionEvent) {
        mItem?.let{
            val disX = mLastX - e.x
            //检查mItem移动后应该在[-deleteLength, 0]内
            val deleteWidth = it.getChildAt(it.childCount - 1).width
            if((it.scrollX + disX) <= deleteWidth && (it.scrollX + disX)>=0){
                //触发移动
                it.scrollBy(disX.toInt(), 0)
            }
        }
    }

    //获取点击位置
    //版本一：通过点击y的坐标除以item的高度得出
    private fun getSelectItem(e: MotionEvent) {
        val frame = Rect()
        forEach {
            if (it.visibility != GONE) {
                it.getHitRect(frame)
                if (frame.contains(e.x.toInt(), e.y.toInt())) {
                    mItem = it as ViewGroup
                }
            }
        }
    }
}