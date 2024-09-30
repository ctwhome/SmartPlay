package com.example.smartplay.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

class FlowLayout : ViewGroup {
    private var lineHeight = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        val count = childCount
        var lineHeight = 0

        var xPos = paddingLeft
        var yPos = paddingTop

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight

                if (xPos + childWidth > width) {
                    xPos = paddingLeft
                    yPos += lineHeight
                }

                xPos += childWidth
                lineHeight = max(lineHeight, childHeight)
            }
        }
        height = max(height, yPos + lineHeight)

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        var xPos = paddingLeft
        var yPos = paddingTop

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight

                if (xPos + childWidth > width) {
                    xPos = paddingLeft
                    yPos += lineHeight
                }

                child.layout(xPos, yPos, xPos + childWidth, yPos + childHeight)
                xPos += childWidth
                lineHeight = max(lineHeight, childHeight)
            }
        }
    }
}