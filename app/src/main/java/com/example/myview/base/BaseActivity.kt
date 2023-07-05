package com.example.myview.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.myview.R


/**
 * 基类 Activity
 * 支持默认 Toolbar、沉浸状态栏、全屏、禁止旋转、Toast简化、页面跳转简化、权限申请等功能
 *
 */
abstract class BaseActivity : AppCompatActivity() {
    /**
     * 自定义ToolBar主标题
     */
    var mToolbarTitle: TextView? = null
    /**
     * 自定义 ToolBar 子标题
     */
    var mToolbarSubTitle: TextView? = null
    /**
     * 自定义 ToolBar
     */
    var mToolbar: Toolbar? = null
    /**
     * 是否使用默认自定义的 toolbar
     */
    var isDefaultToolbar = true
    /**
     * 是否显示 toolbar 返回键
     */
    var isShowBacking = true
    /**
     * 是否沉浸状态栏
     */
    var isSteepStatusBar = false
    /**
     * 是否全屏
     */
    var isFullScreen = false
    /**
     * 是否禁止旋转屏幕，默认竖屏显示
     */
    var isAllowScreenRotate = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("", "BaseActivity-->onCreate()")

        //绑定视图，而不是布局，适合配合 view binding 使用
        val mView = bindView()

        //当前 Activity 渲染的视图View
        val mContextView: View = mView ?:
        //推荐使用 view binding，但依然可以使用 布局 ID 绑定
        LayoutInflater.from(this).inflate(bindLayout(), null)
        //设置全屏
        if (isFullScreen) {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        //设置沉浸状态栏
        if (isSteepStatusBar) {
            steepStatusBar()
        }
        //设置活动布局
        setContentView(mContextView)
        //设置屏幕旋转
        if (!isAllowScreenRotate) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // 默认自定义 TOOLBAR 相关
        if (isDefaultToolbar) {

            //几个控件
            mToolbar = findViewById(R.id.toolbar)
            mToolbarTitle = findViewById(R.id.toolbar_title)
            mToolbarSubTitle = findViewById(R.id.toolbar_subtitle)

            if (mToolbar != null) {
                //将Toolbar显示到界面
                setSupportActionBar(mToolbar)

                //设置 Toolbar，顶部 padding 设置为状态栏高度
                mToolbar!!.setPadding(
                    mToolbar!!.paddingLeft,
                    statusBarHeight,
                    mToolbar!!.paddingRight,
                    mToolbar!!.paddingBottom
                )
            }

            if (mToolbarTitle != null) {
                //getTitle()的值是activity的android:label属性值
                mToolbarTitle!!.text = title
                //设置默认的标题不显示
                val actionBar = supportActionBar
                actionBar?.setDisplayShowTitleEnabled(false)
            }
        }
        //初始化传入参数
        initData(intent)

        //处理其他逻辑
        doBusiness(this)
    }
    /**
     * 绑定视图
     *
     * @return 绑定视图
     */
    open fun bindView(): View?{
        return null
    }
    /**
     * 绑定布局
     *
     * @return 布局 id
     */
    fun bindLayout(): Int {
        return 0
    }
    /**
     * 沉浸状态栏
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun steepStatusBar() {
        //沉浸式状态栏，方式一
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            // 沉浸式状态栏
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            // 透明状态栏
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//        }

        //沉浸式状态栏，方式二
        if (Build.VERSION.SDK_INT >= 21) {
            // 沉浸式状态栏
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

            // 透明状态栏
            window.statusBarColor = Color.TRANSPARENT
        }
    }
    /**
     * 利用反射获取状态栏高度
     * @return
     * 状态栏高度
     */
    val statusBarHeight: Int
        get() {
            var result = 0
            //获取状态栏高度的资源id
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }
    /**
     * 初始化参数，不写 onCreate 可以用这个(不强制)
     *
     * @param intent 传入的 intent
     */
    open fun initData(intent: Intent?) {}

    /**
     * 绑定视图，不写 onCreate 可以用这个(不强制)
     *
     * @return 绑定视图
     */
    open fun doBusiness(context: Context) {}

}