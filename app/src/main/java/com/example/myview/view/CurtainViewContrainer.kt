package com.example.myview.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

/**
 * @author ray
 * @date 2023/9/1 17:44
 */
class CurtainViewContrainer: ViewGroup {
    constructor(context: Context):this(context, null)

    constructor(context: Context, attrs: AttributeSet):this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        init()
    }

    private fun init() {
        clipChildren = false
        clipToPadding = false
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        TODO("Not yet implemented")
    }
}