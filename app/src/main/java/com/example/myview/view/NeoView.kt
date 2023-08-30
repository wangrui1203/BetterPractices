package com.example.myview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.myview.R
import java.util.*

/**
 * @author ray
 * @date 2023/8/29 16:13
 */
class NeoView : View {


    var random = Random()
    //普通画笔
    var mPaint: Paint? = null
    //高亮画笔
    var mPaintLight: Paint? = null

    //文字大小
    val TEXT_SIZE = 24
    var x = 0
    var y = 0
    var mWidth = 0
    var mHeight = 0
    var frameCount = 0
    //改变文字的间隔时间
    var interval = intArrayOf(9, 11, 17, 23, 29)
    //数字下降的速度
    var fadeInterval = 1.6f
    //文字颜色值
    val DEFAULT_TEXT_COLOR = Color.argb(255, 0, 255, 70)
    var textColor = 0
    var a = 0
    var r = 0
    var g = 0
    var b = 0

    var streams: Array<Any?>? = null
    var katakana: Array<String?>? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.NeoView, defStyleAttr, 0)
        textColor = a.getColor(R.styleable.NeoView_textColor, DEFAULT_TEXT_COLOR)
        a.recycle()
        init()
    }

    fun init(){
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        //透明度，0红色，255绿色，70蓝色
        mPaint!!.setARGB(255, 0, 255, 70)
        mPaint!!.textSize = TEXT_SIZE.toFloat()
        a = textColor shr 24 and 0xff
        r = textColor shr 16 and 0xff
        g = textColor shr 8 and 0xff
        b = textColor and 0xff
        mPaint!!.setARGB(a, r, g, b)

        mPaintLight = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintLight!!.setARGB(255, 140, 255, 170)
        mPaintLight!!.textSize = TEXT_SIZE.toFloat()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode == MeasureSpec.EXACTLY) {
            mWidth = widthSize
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mHeight = heightSize
        }
        setMeasuredDimension(mWidth, mHeight)
        setUp()
    }


    private fun setUp() {
        if (streams == null && mWidth > 0) {
            streams = arrayOfNulls<Any>(mWidth / TEXT_SIZE)
        } else {
            // 防止重复初始化数组，避免框架层对onMeasure多次调用，造成重复创建对象。
            return
        }

        // 初始化katakana字符数组
        if (katakana == null) {
            katakana = arrayOfNulls<String>(96)
        }
        for (i in 0..95) {
            katakana!![i] = (0x30a0 + i).toChar().toString()
        }
        //带索引遍历
        for (j in streams!!.indices) {
            //255完全不透明
            var opacity = 255
            //5+[0,30)=[5,35)的随机数，作为每个数字流的长度
            val length = random.nextInt(35 - 5) + 5
            //[5,22)的随机数
            val speed = random.nextInt(22 - 5) + 5
            val symbols: Array<Symbol?> =
                arrayOfNulls<Symbol>(length)
            //第j个数字流的X轴点
            val sx = x + j * TEXT_SIZE
            var first = random.nextInt(100) < 45
            //每次y的起始值随机，不要每次都从顶部开始
            y = -random.nextInt(500)
            //设置每个数字流中的每个字符
            for (i in 0 until length) {
                val symbol: Symbol = Symbol(
                    sx,
                    y - i * TEXT_SIZE, speed, first, opacity
                )
                //symbol.value = getChar();
                symbol.setToRandomSymbol()
                symbols[i] = symbol
                opacity -= (255 / symbols.size / fadeInterval).toInt()
                first = false
            }
            streams!![j] = symbols
        }
    }

    private fun render(canvas: Canvas) {
        //画布
        canvas.drawColor(Color.BLACK)
        for (j in streams?.indices!!) {
            val symbols: Array<Symbol> =
                streams?.get(j) as Array<Symbol>
            for (i in symbols.indices) {
                val symbol: Symbol = symbols[i]
                //KLog.i("www", symbol.toString());
                if (symbol.first) {
                    mPaintLight!!.setARGB(symbol.opacity, 140, 255, 170)
                    canvas.drawText(symbol.value?:"", symbol.x.toFloat(), symbol.y.toFloat(), mPaintLight!!)
                } else {
                    mPaint!!.setARGB(symbol.opacity, r, g, b)
                    canvas.drawText(symbol.value?:"", symbol.x.toFloat(), symbol.y.toFloat(), mPaint!!)
                }
            }
            //渲染流中，渲染流的每个字符
            setSymbols(symbols)
        }
        invalidate()
        frameCount++
    }

    //由上至下，增加y，到底部重置，从顶开始
    private fun rain() {
        for (j in streams?.indices!!) {
            val symbols: Array<Symbol> =
                streams?.get(j) as Array<Symbol>
            for (i in symbols.indices) {
                val symbol: Symbol = symbols[i]
                //根据速度，增加每个字符的y。到达底部后，重置为0（页面顶部）
                symbol.y = if (symbol.y >= mHeight) 0 else symbol.y + symbol.speed
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //渲染流
        render(canvas)
        rain()
    }


    private fun setSymbols(symbols: Array<Symbol>) {
        for (i in symbols.indices) {
            val symbol: Symbol = symbols[i]
            symbol.setToRandomSymbol()
        }
        /*if (Math.abs(frameCount) % REFRESH_RATE == 0){
        }*/
    }


    inner class Symbol(var x: Int, var y: Int, var speed: Int, var first: Boolean, var opacity: Int) {
        //符号
        var value: String? = null
        //每个符号选择一个随机的变化间隔
        var switchInternal: Int = (this@NeoView.interval).get(this@NeoView.random.nextInt(this@NeoView.interval.size)) //random.nextInt(25-2)+2;
        //设置随机符号
        fun setToRandomSymbol() {
            if (this@NeoView.frameCount % switchInternal == 0) {
                //不是每帧渲染该字符都变化，按照间隔再变化
                value = char
            }
        }
        private val char: String?
            private get() = (this@NeoView.katakana)?.get(this@NeoView.random.nextInt(96))

        /*
        char charItem = (char) (0x30a0 + (random.nextInt(96)));
        return String.valueOf(charItem);
        */
        /*
        char[] array = new char[62];
        for (int i=0x30;i<=0x39;i++){
            array[i&0x0f]=(char) i;
        }
        int data = 0x41;
        for (int i=10;i<=35;i++){
            array[i] = (char)data;
            data++;
        }
        data = 0x61;
        for (int i=36;i<=61;i++){
            array[i] = (char)data;
            data++;
        }
        //System.out.println(Arrays.toString(array));
        //[0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        // A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
        // a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z]
        return String.valueOf(array[random.nextInt(array.length)]);
        */

        override fun toString(): String {
            return "Symbol{" +
                    "x=" + x +
                    ", y=" + y +
                    ", value='" + value + '\'' +
                    ", speed=" + speed +
                    '}'
        }
    }


}