package com.olamachia.dailygramapp.utils.customviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ProgressBar

class VerticalProgressBar : ProgressBar {
    private var x = 0
    private var y = 0
    private var z = 0
    private var w = 0
    override fun drawableStateChanged() {
        super.drawableStateChanged()
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(h, w, oldh, oldw)
        x = w
        y = h
        z = oldw
        this.w = oldh
    }

    @Synchronized
    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(c: Canvas) {
        c.rotate((-90).toFloat())
        c.translate((-height).toFloat(), 0F)
        super.onDraw(c)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isSelected = true
                isPressed = true
            }
            MotionEvent.ACTION_MOVE -> {
                progress = (
                    max -
                        (max * event.y / height).toInt()
                    )
                onSizeChanged(width, height, 0, 0)
            }
            MotionEvent.ACTION_UP -> {
                isSelected = false
                isPressed = false
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return true
    }

    @Synchronized
    override fun setProgress(progress: Int) {
        if (progress >= 0) super.setProgress(progress) else super.setProgress(0)
        onSizeChanged(x, y, z, w)
    }
}
