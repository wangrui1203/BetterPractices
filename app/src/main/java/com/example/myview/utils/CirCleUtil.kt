package com.example.myview.utils

import android.content.Context
import java.text.DecimalFormat

class CircleUtil {

    companion object {
        /**
         * 将double格式化为指定小数位的String，不足小数位用0补全
         *
         * @param v     需要格式化的数字
         * @param scale 小数点后保留几位
         * @return
         */
        fun roundByScale(v: Double, scale: Int): String {
            if (scale < 0) {
                throw IllegalArgumentException("参数错误，必须设置大于0的数字")
            }
            if (scale == 0) {
                return DecimalFormat("0").format(v)
            }
            var formatStr = "0."

            for (i in 0 until scale) {
                formatStr += "0"
            }
            return DecimalFormat(formatStr).format(v);
        }

        fun dip2px(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }

        fun dp2px(context: Context, dpValue: Float): Int {
            return dip2px(context, dpValue)
        }

        fun px2dip(context: Context, pxValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (pxValue / scale + 0.5f).toInt()
        }

        fun px2sp(context: Context, pxValue: Float): Int {
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (pxValue / fontScale + 0.5f).toInt()
        }

        fun sp2px(context: Context, spValue: Float): Int {
            val fontScale = context.resources.displayMetrics.scaledDensity
            return (spValue * fontScale + 0.5f).toInt()
        }

    }
}