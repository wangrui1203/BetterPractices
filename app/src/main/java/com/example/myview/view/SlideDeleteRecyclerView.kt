package com.example.myview.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.*
import android.widget.Scroller
import androidx.recyclerview.widget.RecyclerView
import com.example.loglib.KLog
import java.lang.Math.abs

/**
 * 左滑删除控件
 * 核心思想：
 * 1.在down事件中，判断在列表内位置，得到对应item
 * 2.拦截move事件，item跟随滑动，最大距离为删除按钮长度
 * 3.在up事件中，确定最终状态，固定item位置
 */
class SlideDeleteRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
    ): RecyclerView(context, attrs, defStyleAttr){

        //系统最小移动距离
        private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        //最小有效滑动速度
        private val mMinVelocity = 600

        //增加手势控制，双击快速完成侧滑，还是为了练习
        private var mGestureDetector: GestureDetector
                = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
            override fun onDoubleTap(e: MotionEvent): Boolean {
                getSelectItem(e)
                mItem?.let {
                    val deleteWidth = it.getChildAt(it.childCount - 1).width
                    //触发移动至完全展开deleteWidth
                    if (it.scrollX == 0) {
                        mScroller.startScroll(0, 0, deleteWidth, 0)
                    }else {
                        mScroller.startScroll(it.scrollX, 0, -it.scrollX, 0)
                    }
                    invalidate()
                    return true
                }
                //不进行拦截，只是作为工具判断下双击
                return false
            }
        })
        // 使用速度控制器，增加侧滑速度判定滑动成功。注意VelocityTracker 由 native 实现，需要及时释放内存
        // addMovement(event)=添加触摸点MotionEvent;
        // computerCurrentVelocity(1000)计算1s内x轴方向移动速度；
        // getXVelocity()获取x方向移动速度，负值表示向左滑动
        private var mVelocityTracker: VelocityTracker? = null


        //流畅滑动
        private var mScroller = Scroller(context)
        //当前选中的item
        private var mItem: ViewGroup? = null
        private var deleteWidth = -1

        //上次按下横坐标
        private var mLastX = 0f
        private var mLastY = 0f
        //上次的view index
        private var index = -1
        //是否滑动子View
        private var mIsSlide = false
        //是否展开
        private var isTouchOpened = false
//        // 滑动模式，左滑模式(-1)，初始化(0)，上下模式(1)
//        private var mScrollMode = 0

        private val TAG = "SlideDeleteRecycleView"

        //当前RecyclerView被上层viewGroup分发到事件，所有事件都会通过dispatchTouchEvent给到
//        override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
            //
//            ev?.let { mGestureDetector.onTouchEvent(it) }
//            return super.dispatchTouchEvent(ev)
//        }

        // viewGroup对子控件的事件拦截，一旦拦截，
        // 后续事件序列不会再调用onInterceptTouchEvent
        // 内部的事件拦截，有两种情况：
        // 1）ACTION_DOWN 时，如果已经有ItemView处于展开状态，
        // 并且这次点击的对象不是已打开的那个ItemView，则拦截事件，并将已展开的ItemView关闭。
        // 2）ACTION_MOVE 时，有俩判断，满足其一则认为是侧滑：
        // 1. x方向速度大于y方向速度，且大于最小速度限制；
        // 2. x方向的侧滑距离大于y方向滑动距离，且x方向达到最小滑动距离；
        override fun onInterceptTouchEvent(e: MotionEvent):Boolean{
            //触发速度计算
            obtainVelocity(e)
            e.let {
                when(e.action){
                    //情况1
                    MotionEvent.ACTION_DOWN -> {
                        KLog.e("", "onInterceptTouchEvent() -> ACTION_DOWN")
                        //如果动画没停止，立即停止动画
                        if(!mScroller.isFinished){
                            mScroller.abortAnimation()
                        }
//                        var view: ViewGroup? = getChildAt(index) as ViewGroup?
                        //获取点击位置，并更新mItem
                        getSelectItem(e)
                        if(mItem != null){
                            KLog.d(TAG, "dragEnable : false")
//                            mStateCallback?.dragEnable(false)
                            KLog.i(TAG,"touch item ${mItem.toString()} scrollX ${mItem!!.scrollX}")
//                            if(view != null){
//                                KLog.i(TAG,"touch view ${view.toString()} scrollX ${view!!.scrollX}")
//                            }
                            //该处无效
                            // 如果之前触碰的item已经打开，但是当前触碰到的view不是那个view则立即关闭之前的view
                            // 此处并不需要担动画没完成冲突，因为之前已经abortAnimation
//                            if(view!=null && mItem != view && view.scrollX != 0){
//                                view.scrollTo(0,0)
//                                KLog.e(TAG, "onInterceptTouchEvent() -> ACTION_DOWN 拦截事件-关闭已打开的menu")
//                                return true
//                            }
                            //判断点击的是否是一个打开的item
//                            isTouchOpened = view == mItem && mItem!!.scrollX != 0
                            //设置点击的横坐标
                            mLastX = e.x
                            // 对滑动冲突处理
                            mLastY = e.y
//                            mScrollMode = 0

                            // 这里进行了强制的要求，RecyclerView的子ViewGroup必须要有2个子view,这样菜单按钮才会有值，
                            // 需要注意的是:如果不定制RecyclerView的子View，则要求子View必须要有固定的width。
                            // 比如使用LinearLayout作为根布局，而content部分width已经是match_parent，此时如果菜单view用的是wrap_content，menu的宽度就会为0。
                            if (mItem?.childCount!! >= 2) {
                                KLog.d(TAG, "ItemChild = ${mItem?.childCount}")
                                deleteWidth = mItem?.getChildAt(mItem?.childCount!! - 1 )!!.getWidth()
                            } else {
                                KLog.d(TAG, "ItemChild = ${mItem?.childCount}")
                                //子ItemView不含有两个子View
                                deleteWidth = -1
                            }
                        }
                    }
                    //情况2
                    MotionEvent.ACTION_MOVE -> {
                        KLog.e(TAG, "onInterceptTouchEvent() -> ACTION_MOVE")
                        mVelocityTracker!!.computeCurrentVelocity(1000)
                        if((kotlin.math.abs(mVelocityTracker!!.xVelocity) > mMinVelocity
                                    && kotlin.math.abs(mVelocityTracker!!.xVelocity) > kotlin.math.abs(mVelocityTracker!!.yVelocity))
                            ||(kotlin.math.abs(e.x - mLastX) >= mTouchSlop)
                                    && kotlin.math.abs(e.x - mLastX) > kotlin.math.abs(e.y - mLastY)){
                            KLog.i(TAG, "onInterceptTouchEvent() -> ACTION_MOVE true")
                            mIsSlide = true

                            KLog.d("", "dragEnable : false")
//                            stateCallback.dragEnable(false)
                        }else{
                            //判定非侧向滑动场景
                            if(!isTouchOpened){
                                KLog.e("","dragEnable : true")
//                            stateCallback.dragEnable(true)
                            }
                        }
                        //-> 如果拦截了ACTION_MOVE，后续事件就不触发onInterceptTouchEvent了
                        // 不能拦截事件，会造成列表项中onclick事件失效
                    }
                    MotionEvent.ACTION_UP -> {
                        KLog.e(TAG, "onInterceptTouchEvent() -> ACTION_UP")
                        releaseVelocity()
                    }
                }
            }
            return super.onInterceptTouchEvent(e)
        }


    // 拦截事件后，处理事件
    // 或者子控件不处理，返回到父控件处理，在onTouch之后，在onClick之前
    // 如果不消耗，则在同一事件序列中，当前View无法再次接受事件
    // performClick会被onTouchEvent拦截，我们这不需要点击，全都交给super实现去了
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        //触发速度计算
        if (e != null) {
            obtainVelocity(e)
        }
        if(mIsSlide && mItem != null){
            e?.let {
                when(e.action){
                    MotionEvent.ACTION_DOWN -> {
                        //没有拦截，不会触发到
                        KLog.e(TAG, "onTouchEvent: ACTION_DOWN")
                    }
                    //拦截了ACTION_MOVE之后，后面一系列event都会交到本view处理
                    MotionEvent.ACTION_MOVE -> {
                        KLog.e(TAG, "onTouchEvent: ACTION_MOVE")
                        //滑动
                        if(deleteWidth != -1) moveItem(e)
                    }
                    MotionEvent.ACTION_UP -> {
                        KLog.e(TAG, "onTouchEvent: ACTION_UP")
                        //滑动结束，判断结果
                        if(deleteWidth != -1) stopMove()
                    }
                }
            }
        }else{
            //此处防止RecyclerView正常滑动时，还有菜单未关闭
            closeMenu()
            // Velocity，这里的释放是防止RecyclerView正常拦截了，但是在onTouchEvent中却没有被释放；
            // 有三种情况：1.onInterceptTouchEvent并未拦截，在onInterceptTouchEvent方法中，DOWN和UP一对获取和释放；
            // 2.onInterceptTouchEvent拦截，DOWN获取，但事件不是被侧滑处理，需要在这里进行释放；
            // 3.onInterceptTouchEvent拦截，DOWN获取，事件被侧滑处理，则在onTouchEvent的UP中释放。
            releaseVelocity()
        }
        return super.onTouchEvent(e)
    }

    //滑动结束 判断位置
    private fun stopMove() {
        mItem?.let{
            //如果移动过半了，应该判定左滑成功
            //如果整个移动过程速度大于1000，也判定滑动成功
            // 删除按钮的宽度
            deleteWidth = it.getChildAt(it.childCount - 1).width
            //注意如果没有拦截ACTION_MOVE，mVelocityTracker是没有初始化的???
            var velocity = 0f
            mVelocityTracker?.let { tracker ->
                tracker.computeCurrentVelocity(1000)
                velocity = tracker.xVelocity
            }
            //判断结束情况:
            if(velocity < -mMinVelocity){
                // 1) 向左速度很快，大于最小有效速度，向左滑动为负值
                //计算剩余要移动的距离
                var delt = kotlin.math.abs(deleteWidth - it.scrollX)
                //根据松手时的速度计算要移动的时间
                var time = (delt / mVelocityTracker?.xVelocity!! * 1000).toInt()
                mScroller.startScroll(it.scrollX, 0, deleteWidth - it.scrollX, 0, kotlin.math.abs(time))
            }else if(velocity >= -mMinVelocity){
                // 2) 向右侧滑达到侧滑最低速度，则关闭
                mScroller.startScroll(it.scrollX, 0, -it.scrollX, 0, Math.abs(it.scrollX));
            }
            else if ( abs(it.scrollX) >= deleteWidth / 2f ) {
                // 3) 移动过半 触发移动至完全展开
                mScroller.startScroll(it.scrollX, 0, deleteWidth - it.scrollX, 0, kotlin.math.abs(deleteWidth - it.scrollX))
            }else {
                // 4)（其他情况）如果移动没过半应该恢复状态，或者向右移动很快则恢复到原来状态，即关闭状态
                mScroller.startScroll(it.scrollX, 0, -it.scrollX, 0, kotlin.math.abs(it.scrollX))
            }
            invalidate()
        }
        //清除状态
        mLastX = 0f
        mIsSlide = false
        mItem = null
        deleteWidth = -1
        //mVelocityTracker由native实现，需要及时释放该追踪
        releaseVelocity()
    }

    //移动item
    private fun moveItem(e: MotionEvent) {
        mItem?.let{
            val disX = mLastX - e.x
            deleteWidth = it.getChildAt(it.childCount - 1).width
            //检查mItem移动后应该在[-deleteLength, 0]内
            if((it.scrollX + disX) <= deleteWidth && (it.scrollX + disX)>0){
                //触发移动
                it.scrollBy(disX.toInt(), 0)
            }
            mLastX = e.x
        }
    }

    //获取点击位置
    //版本一：通过点击y的坐标除以item的高度得出
    //问题：没考虑列表项的可见性、列表滑动的情况，并且x和屏幕有关不仅仅是列表
    //版本二：通过遍历子view检查事件在哪个view内，得到点击的item
    private fun getSelectItem(e: MotionEvent) {
        //获得第一个可见的item的position
        val frame = Rect()
        //遍历ChildView
        for(i in 0..childCount - 1){
            val child = getChildAt(i)
//            KLog.i(TAG,"foreach item $i")
            if(child.visibility == VISIBLE){
//                KLog.i(TAG,"touch child $child")
                //获取子view的bound
                child.getHitRect(frame)
                //判断触摸点是否在子view中
                if(frame.contains(e.x.toInt(), e.y.toInt())){
                    KLog.i(TAG,"touch item idx $i")
                    index = i
                    mItem = getChildAt(i) as ViewGroup
                    KLog.i(TAG,"touch item $mItem")
                }
            }
        }
//        //方式二
//        forEach {
//            if (it.visibility == VISIBLE) {
//                it.getHitRect(frame)
//                if (frame.contains(e.x.toInt(), e.y.toInt())) {
//                    mItem = it as ViewGroup
//                }
//            }
//        }
    }

    private fun obtainVelocity(event: MotionEvent){
        mVelocityTracker =mVelocityTracker?:VelocityTracker.obtain()
        mVelocityTracker!!.addMovement(event)
    }

    private fun releaseVelocity(){
        mVelocityTracker?.let {
            it.clear()
            it.recycle()
        }
        mVelocityTracker = null
    }

    /**
     * 将显示子菜单 的子View关闭
     * 不定制item，不好监听点击事件，需要调用者手动关闭
     */
    private fun closeMenu(){
        if(mItem != null && mItem!!.scrollX != 0){
            mItem!!.scrollTo(0, 0)
        }
    }

    //流畅地滑动
    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mItem?.scrollBy(mScroller.currX, mScroller.currY)
            invalidate()
        }
    }


    private var mStateCallback: StateCallback? = null
    fun setStateCallback(stateCallback: StateCallback) {
        mStateCallback = stateCallback
    }

    interface StateCallback {
        fun dragEnable(enable: Boolean)
    }

}