package com.example.myview.base

import android.util.SparseArray
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

/**
 * ViewHolder主要是做一些与控件布局有关的操作 ，我们可以在这边简化一下获取控件对象的代码
 */
class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val mConvertView: View = itemView
    /**
     * SparseArray
     * 以键值对形式进行存储，基于分查找,因此查找的时间复杂度为0(LogN);
     * 由于SparseArray中Key存储的是数组形式,因此可以直接以int作为Key。避免了HashMap的装箱拆箱操作,性能更高且int的存储开销远远小于Integer;
     * 采用了延迟删除的机制(针对数组的删除扩容开销大的问题的优化， 具体稍后分析) ;
     */
    private val mViews: SparseArray<View?> = SparseArray()

    /**
     * 根据资源获取View对象
     * @param res
     * 控件ID
     * @param <T>
     * 类型
     * @return
     * 控件
    </T> */
    fun <T : View?> getView(@IdRes res: Int): T? {
        var view = mViews[res]
        if (view == null) {
            view = mConvertView.findViewById(res)
            mViews.put(res, view)
        }
        return view as T?
    }

    /**
     * 提供TextView和Button设置文本简化操作
     * @param idRes
     * 控件ID
     * @param charSequence
     * 字符串
     * @return
     * holder
     */
    fun setText(@IdRes idRes: Int, charSequence: CharSequence?): ViewHolder {
        val view = getView<View>(idRes)!!
        if (view is TextView) {
            view.text = charSequence
        } else if (view is Button) {
            view.text = charSequence
        }
        return this
    }

    /**
     * 提供TextView和Button设置文本颜色简化操作
     * @param idRes
     * 控件ID
     * @param color
     * 颜色
     * @return
     * holder
     */
    fun setTextColor(@IdRes idRes: Int, color: Int): ViewHolder {
        val view = getView<View>(idRes)!!
        if (view is TextView) {
            view.setTextColor(color)
        } else if (view is Button) {
            view.setTextColor(color)
        }
        return this
    }

    /**
     * 设置指定ViewId的背景颜色
     * @param idRes
     * 控件ID
     * @param color
     * 颜色
     * @return
     * holder
     */
    fun setBackgroundColor(@IdRes idRes: Int, color: Int): ViewHolder {
        val view = getView<View>(idRes)!!
        view.setBackgroundColor(color)
        return this
    }

    /**
     * 设置指定ViewId的可见度
     * @param idRes
     * 控件ID
     * @param visibility
     * 可见度
     * @return
     * holder
     */
    fun setVisibility(@IdRes idRes: Int, @DrawableRes visibility: Int): ViewHolder {
        val view = getView<View>(idRes)!!
        view.visibility = visibility
        return this
    }

    /**
     * 设置ImageView显示图片
     * @param idRes
     * 控件ID
     * @param res
     * 图片路径
     * @return
     * holder
     */
    fun setImageResource(@IdRes idRes: Int, @DrawableRes res: Int): ViewHolder {
        val view = getView<View>(idRes)!!
        if (view is ImageView) {
            view.setImageResource(res)
        }
        return this
    }

    /**
     * 设置指定控件ID的点击事件
     * @param idRes
     * 控件ID
     * @param listener
     * 监听接口
     * @return
     * holder
     */
    fun setOnClickListener(@IdRes idRes: Int, listener: View.OnClickListener?): ViewHolder {
        val view = getView<View>(idRes)!!
        view.setOnClickListener(listener)
        return this
    }

    /**
     * 设置指定控件ID的长按事件
     * @param idRes
     * 控件ID
     * @param listener
     * 监听接口
     * @return
     * holder
     */
    fun setOnLongClickListener(@IdRes idRes: Int, listener: OnLongClickListener?): ViewHolder {
        val view = getView<View>(idRes)!!
        view.setOnLongClickListener(listener)
        return this
    }

    /**
     * 设置指定控件的TAG
     * @param idRes
     * 控件ID
     * @param tag
     * tag
     * @return
     * holder
     */
    fun setTag(@IdRes idRes: Int, tag: Any?): ViewHolder {
        val view = getView<View>(idRes)!!
        view.tag = tag
        return this
    }

    /**
     * 获取指定控件的TAG
     * @param idRes
     * 控件ID
     * @return
     * holder
     */
    fun getTag(@IdRes idRes: Int): Any {
        val view = getView<View>(idRes)!!
        return view.tag
    }

    companion object {
        /**
         *
         * @param view
         * view
         * @return
         * holder
         */
        fun create(view: View): ViewHolder {
            return ViewHolder(view)
        }
    }


}