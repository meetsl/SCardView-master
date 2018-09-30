package com.meetsl.scardview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.opengl.Visibility

internal class SCardViewApi17Impl : SCardViewBaseImpl() {


    override fun initStatic() {
        SRoundRectDrawableWithShadow.sRoundRectHelper = object : SRoundRectDrawableWithShadow.RoundRectHelper {
            override fun drawRoundRect(canvas: Canvas, bounds: RectF, cornerRadius: Float, cornerVisibility: Int, paint: Paint) {
                canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)
            }
        }
    }
}