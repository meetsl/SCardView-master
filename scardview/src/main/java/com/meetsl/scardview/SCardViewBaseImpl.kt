package com.meetsl.scardview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.Nullable

open class SCardViewBaseImpl : SCardViewImpl {

    private val mCornerRect = RectF()

    override fun initStatic() {
        // Draws a round rect using 7 draw operations. This is faster than using
        // canvas.drawRoundRect before JBMR1 because API 11-16 used alpha mask textures to draw
        // shapes.
        SRoundRectDrawableWithShadow.sRoundRectHelper = object : SRoundRectDrawableWithShadow.RoundRectHelper {
            override fun drawRoundRect(canvas: Canvas, bounds: RectF, cornerRadius: Float, cornerVisibility: Int, paint: Paint) {
                val twoRadius = cornerRadius * 2
                val innerWidth = bounds.width() - twoRadius - 1f
                val innerHeight = bounds.height() - twoRadius - 1f
                if (cornerRadius >= 1f) {
                    // increment corner radius to account for half pixels.
                    val roundedCornerRadius = cornerRadius + .5f
                    mCornerRect.set(-roundedCornerRadius, -roundedCornerRadius, roundedCornerRadius, roundedCornerRadius)
                    val saved = canvas.save()
                    canvas.translate(bounds.left + roundedCornerRadius, bounds.top + roundedCornerRadius)
                    if (cornerVisibility == NOLEFTCORNER || cornerVisibility == NOTOPCORNER || cornerVisibility == NOLT_RBCORNER)
                        canvas.drawRect(-roundedCornerRadius, -roundedCornerRadius, 0f, 0f, paint)
                    else
                        canvas.drawArc(mCornerRect, 180f, 90f, true, paint)
                    canvas.translate(innerWidth, 0f)
                    canvas.rotate(90f)
                    if (cornerVisibility == NORIGHTCORNER || cornerVisibility == NOTOPCORNER || cornerVisibility == NORT_LBCORNER)
                        canvas.drawRect(-roundedCornerRadius, -roundedCornerRadius, 0f, 0f, paint)
                    else
                        canvas.drawArc(mCornerRect, 180f, 90f, true, paint)
                    canvas.translate(innerHeight, 0f)
                    canvas.rotate(90f)
                    if (cornerVisibility == NORIGHTCORNER || cornerVisibility == NOBOTTOMCORNER || cornerVisibility == NOLT_RBCORNER)
                        canvas.drawRect(-roundedCornerRadius, -roundedCornerRadius, 0f, 0f, paint)
                    else
                        canvas.drawArc(mCornerRect, 180f, 90f, true, paint)
                    canvas.translate(innerWidth, 0f)
                    canvas.rotate(90f)
                    if (cornerVisibility == NOLEFTCORNER || cornerVisibility == NOBOTTOMCORNER || cornerVisibility == NORT_LBCORNER)
                        canvas.drawRect(-roundedCornerRadius, -roundedCornerRadius, 0f, 0f, paint)
                    else
                        canvas.drawArc(mCornerRect, 180f, 90f, true, paint)
                    canvas.restoreToCount(saved)

                    //draw top and bottom pieces
                    canvas.drawRect(bounds.left + roundedCornerRadius - 1f, bounds.top,
                            bounds.right - roundedCornerRadius + 1f,
                            bounds.top + roundedCornerRadius, paint)

                    canvas.drawRect(bounds.left + roundedCornerRadius - 1f,
                            bounds.bottom - roundedCornerRadius,
                            bounds.right - roundedCornerRadius + 1f, bounds.bottom, paint)
                }
                // center
                canvas.drawRect(bounds.left, bounds.top + cornerRadius,
                        bounds.right, bounds.bottom - cornerRadius, paint)
            }

        }
    }

    override fun initialize(cardView: SCardViewDelegate, context: Context,
                            backgroundColor: ColorStateList, radius: Float, elevation: Float,
                            maxElevation: Float, direction: Int, cornerVisibility: Int, startColor: Int, endColor: Int) {
        val background = createBackground(cardView, context, backgroundColor, radius,
                elevation, maxElevation, direction, cornerVisibility, startColor, endColor)
        background.setAddPaddingForCorners(cardView.preventCornerOverlap)
        cardView.cardBackground = background
        updatePadding(cardView)
    }

    private fun createBackground(cardViewDelegate: SCardViewDelegate, context: Context,
                                 backgroundColor: ColorStateList, radius: Float, elevation: Float,
                                 maxElevation: Float, direction: Int, cornerVisibility: Int, startColor: Int,
                                 endColor: Int): SRoundRectDrawableWithShadow {
        return SRoundRectDrawableWithShadow(cardViewDelegate, context.resources, backgroundColor, radius,
                elevation, maxElevation, direction, cornerVisibility, startColor, endColor)
    }

    override fun updatePadding(cardView: SCardViewDelegate) {
        val shadowPadding = Rect()
        getShadowBackground(cardView).getMaxShadowAndCornerPadding(shadowPadding)
        cardView.setMinWidthHeightInternal(Math.ceil(getMinWidth(cardView).toDouble()).toInt(),
                Math.ceil(getMinHeight(cardView).toDouble()).toInt())
        cardView.setShadowPadding(shadowPadding.left, shadowPadding.top,
                shadowPadding.right, shadowPadding.bottom)
    }

    override fun onCompatPaddingChanged(cardView: SCardViewDelegate) {
        // NO OP
    }

    override fun onPreventCornerOverlapChanged(cardView: SCardViewDelegate) {
        getShadowBackground(cardView).setAddPaddingForCorners(cardView.preventCornerOverlap)
        updatePadding(cardView)
    }

    override fun setBackgroundColor(cardView: SCardViewDelegate, @Nullable color: ColorStateList?) {
        getShadowBackground(cardView).setColor(color)
    }

    override fun setShadowColor(cardView: SCardViewDelegate, startColor: Int, endColor: Int) {
        getShadowBackground(cardView).setShadowColor(startColor, endColor)
    }

    override fun setColors(cardView: SCardViewDelegate, backgroundColor: Int, shadowStartColor: Int, shadowEndColor: Int) {
        getShadowBackground(cardView).setColors(backgroundColor, shadowStartColor, shadowEndColor)
    }

    override fun getBackgroundColor(cardView: SCardViewDelegate): ColorStateList {
        return getShadowBackground(cardView).getColor()!!
    }

    override fun setRadius(cardView: SCardViewDelegate, radius: Float) {
        getShadowBackground(cardView).setCornerRadius(radius)
        updatePadding(cardView)
    }

    override fun getRadius(cardView: SCardViewDelegate): Float {
        return getShadowBackground(cardView).getCornerRadius()
    }

    override fun setElevation(cardView: SCardViewDelegate, elevation: Float) {
        getShadowBackground(cardView).setShadowSize(elevation)
    }

    override fun getElevation(cardView: SCardViewDelegate): Float {
        return getShadowBackground(cardView).getShadowSize()
    }

    override fun setMaxElevation(cardView: SCardViewDelegate, maxElevation: Float) {
        getShadowBackground(cardView).setMaxShadowSize(maxElevation)
        updatePadding(cardView)
    }

    override fun getMaxElevation(cardView: SCardViewDelegate): Float {
        return getShadowBackground(cardView).getMaxShadowSize()
    }

    override fun getMinWidth(cardView: SCardViewDelegate): Float {
        return getShadowBackground(cardView).getMinWidth()
    }

    override fun getMinHeight(cardView: SCardViewDelegate): Float {
        return getShadowBackground(cardView).getMinHeight()
    }

    override fun getShadowBackground(cardView: SCardViewDelegate): SRoundRectDrawableWithShadow {
        return cardView.cardBackground as SRoundRectDrawableWithShadow
    }
}