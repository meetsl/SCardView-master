package com.meetsl.scardview

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import kotlin.math.ceil
import kotlin.math.cos

/**
 * Created by shilong
 *  2018/9/11.
 */
class SRoundRectDrawableWithShadow(cardViewDelegate: SCardViewDelegate, resources: Resources, backgroundColor: ColorStateList, radius: Float, shadowSize: Float, maxShadowSize: Float, direction: Int, cornerVisibility: Int, startColor: Int, endColor: Int) : Drawable() {
    private var mInsetShadow: Int = 0 // extra shadow to avoid gaps between card and shadow
    /*
    * This helper is set by CardView implementations.
    * <p>
    * Prior to API 17, canvas.drawRoundRect is expensive; which is why we need this interface
    * to draw efficient rounded rectangles before 17.
    * */
    private var mPaint: Paint
    private var mCornerShadowPaint: Paint
    private var mEdgeShadowPaint: Paint
    private var mCardBounds: RectF
    private var mCornerRadius: Float = 0.toFloat()
    private var mCornerShadowPath: Path? = null

    // actual value set by developer
    private var mRawMaxShadowSize: Float = 0.toFloat()

    // multiplied value to account for shadow offset
    private var mShadowSize: Float = 0.toFloat()

    // actual value set by developer
    private var mRawShadowSize: Float = 0.toFloat()

    private var mBackground: ColorStateList? = null

    private var mDirty = true

    private var mShadowStartColor: Int = 0

    private var mShadowEndColor: Int = 0

    private var mAddPaddingForCorners = true

    /**
     * If shadow size is set to a value above max shadow, we print a warning
     */
    private var mPrintedShadowClipWarning = false
    private var mLightDirection: Int = DIRECTION_TOP
    private var mCornerVisibility: Int = NONE
    private var mCardDelegate: SCardViewDelegate
    private var mTranslatePos: Pair<Pair<Float, Float>, Pair<Float, Float>>? = null

    init {
        mShadowStartColor = if (startColor == -1) resources.getColor(R.color.sl_cardview_shadow_start_color) else startColor
        mShadowEndColor = if (endColor == -1) resources.getColor(R.color.sl_cardview_shadow_end_color) else endColor
        mInsetShadow = resources.getDimensionPixelSize(R.dimen.cardview_compat_inset_shadow)
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        setBackground(backgroundColor)
        mCornerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        mCornerShadowPaint.style = Paint.Style.FILL
        mCornerRadius = (radius + .5f).toInt().toFloat()
        mCardBounds = RectF()
        mEdgeShadowPaint = Paint(mCornerShadowPaint)
        mEdgeShadowPaint.isAntiAlias = false
        mLightDirection = direction
        mCornerVisibility = cornerVisibility
        mCardDelegate = cardViewDelegate
        setShadowSize(shadowSize, maxShadowSize)
    }

    private fun setBackground(color: ColorStateList?) {
        mBackground = color ?: ColorStateList.valueOf(Color.TRANSPARENT)
        mPaint.color = mBackground!!.getColorForState(state, mBackground!!.defaultColor)
    }

    /**
     * Casts the value to an even integer.
     */
    private fun toEven(value: Float): Int {
        val i = (value + .5f).toInt()
        return if (i % 2 == 1) {
            i - 1
        } else i
    }

    fun setAddPaddingForCorners(addPaddingForCorners: Boolean) {
        mAddPaddingForCorners = addPaddingForCorners
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
        mCornerShadowPaint.alpha = alpha
        mEdgeShadowPaint.alpha = alpha
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        mDirty = true
    }

    private fun setShadowSize(shadowSize: Float, maxShadowSize: Float) {
        var updateShadowSize = shadowSize
        var updateMaxShadowSize = maxShadowSize
        if (updateShadowSize < 0f) {
            throw IllegalArgumentException("Invalid shadow size " + updateShadowSize
                    + ". Must be >= 0")
        }
        if (updateMaxShadowSize < 0f) {
            throw IllegalArgumentException("Invalid max shadow size " + updateMaxShadowSize
                    + ". Must be >= 0")
        }
        updateShadowSize = toEven(updateShadowSize).toFloat()
        updateMaxShadowSize = toEven(updateMaxShadowSize).toFloat()
        if (updateShadowSize > updateMaxShadowSize) {
            updateShadowSize = updateMaxShadowSize
            if (!mPrintedShadowClipWarning) {
                mPrintedShadowClipWarning = true
            }
        }
        if (mRawShadowSize == updateShadowSize && mRawMaxShadowSize == updateMaxShadowSize) {
            return
        }
        mRawShadowSize = updateShadowSize
        mRawMaxShadowSize = updateMaxShadowSize
        mTranslatePos = calculateShadowDirection()
        mShadowSize = (updateShadowSize * SHADOW_MULTIPLIER + mInsetShadow.toFloat() + .5f).toInt().toFloat()
        mDirty = true
        invalidateSelf()
    }

    override fun getPadding(padding: Rect): Boolean {
        val vOffset = ceil(calculateVerticalPadding(mRawMaxShadowSize, mCornerRadius,
                mAddPaddingForCorners).toDouble()).toInt()
        val hOffset = ceil(calculateHorizontalPadding(mRawMaxShadowSize, mCornerRadius,
                mAddPaddingForCorners).toDouble()).toInt()
        padding.set(hOffset, vOffset, hOffset, vOffset)
        return true
    }

    companion object {
        // used to calculate content padding
        private val COS_45 = cos(Math.toRadians(45.0))
        var sRoundRectHelper: RoundRectHelper? = null
        const val SHADOW_MULTIPLIER = 1.5f

        fun calculateVerticalPadding(maxShadowSize: Float, cornerRadius: Float,
                                     addPaddingForCorners: Boolean): Float {
            return if (addPaddingForCorners) {
                (maxShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * cornerRadius).toFloat()
            } else {
                maxShadowSize * SHADOW_MULTIPLIER
            }
        }

        fun calculateHorizontalPadding(maxShadowSize: Float, cornerRadius: Float,
                                       addPaddingForCorners: Boolean): Float {
            return if (addPaddingForCorners) {
                (maxShadowSize + (1 - COS_45) * cornerRadius).toFloat()
            } else {
                maxShadowSize
            }
        }
    }


    override fun onStateChange(stateSet: IntArray): Boolean {
        val newColor = mBackground!!.getColorForState(stateSet, mBackground!!.defaultColor)
        if (mPaint.color == newColor) {
            return false
        }
        mPaint.color = newColor
        mDirty = true
        invalidateSelf()
        return true
    }

    override fun isStateful(): Boolean {
        return mBackground != null && mBackground!!.isStateful || super.isStateful()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    fun setCornerRadius(radius: Float) {
        var updateRadius = radius
        if (updateRadius < 0f) {
            throw IllegalArgumentException("Invalid radius $updateRadius. Must be >= 0")
        }
        updateRadius = (updateRadius + .5f).toInt().toFloat()
        if (mCornerRadius == updateRadius) {
            return
        }
        mCornerRadius = updateRadius
        mDirty = true
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if (mDirty) {
            buildComponents(bounds)
            mDirty = false
        }
        mTranslatePos?.let {
            canvas.translate(it.first.first, it.first.second)
            drawShadow(canvas)
            canvas.translate(it.second.first, it.second.second)
            sRoundRectHelper?.drawRoundRect(canvas, mCardBounds, mCornerRadius, mCornerVisibility, mPaint)
        }
    }

    /**
     * According to the position of light,calculate shadow's position of the card
     */
    private fun calculateShadowDirection(): Pair<Pair<Float, Float>, Pair<Float, Float>> {
        val moveDistance = mRawShadowSize / 2
        return when (mLightDirection) {
            DIRECTION_NONE -> {
                val shadowPos = Pair(0f, 0f)
                val rectPos = Pair(0f, 0f)
                Pair(shadowPos, rectPos)
            }
            DIRECTION_LEFT -> {
                val shadowPos = Pair(0f, 0f)
                val rectPos = Pair(-moveDistance, 0f)
                Pair(shadowPos, rectPos)
            }
            DIRECTION_RIGHT -> {
                val shadowPos = Pair(0f, 0f)
                val rectPos = Pair(moveDistance, 0f)
                Pair(shadowPos, rectPos)
            }
            DIRECTION_TOP -> {
                val shadowPos = Pair(0f, 0f)
                val rectPos = Pair(0f, -moveDistance)
                Pair(shadowPos, rectPos)
            }
            DIRECTION_BOTTOM -> {
                val shadowPos = Pair(0f, 0f)
                val rectPos = Pair(0f, moveDistance)
                Pair(shadowPos, rectPos)
            }
            DIRECTION_LT -> {
                val shadowPos = Pair(0f, moveDistance)
                val rectPos = Pair(-moveDistance, -moveDistance)
                Pair(shadowPos, rectPos)
            }
            DIRECTION_RT -> {
                val shadowPos = Pair(0f, moveDistance)
                val rectPos = Pair(moveDistance, -moveDistance)
                Pair(shadowPos, rectPos)
            }
            DIRECTION_LB -> {
                val shadowPos = Pair(0f, -moveDistance)
                val rectPos = Pair(-moveDistance, moveDistance)
                Pair(shadowPos, rectPos)
            }
            DIRECTION_RB -> {
                val shadowPos = Pair(0f, -moveDistance)
                val rectPos = Pair(moveDistance, moveDistance)
                Pair(shadowPos, rectPos)
            }
            else -> {
                throw IllegalArgumentException("invalid light direction exception")
            }
        }
    }

    private fun drawShadow(canvas: Canvas) {
        val visibility = calculateCornerVisibility() //顺时针 - 可见性
        // LT
        var saved = canvas.save()
        drawLTCorner(canvas, visibility.left)
        canvas.restoreToCount(saved)
        // RB
        saved = canvas.save()
        drawRBCorner(canvas, visibility.right)
        canvas.restoreToCount(saved)
        // LB
        saved = canvas.save()
        drawLBCorner(canvas, visibility.bottom)
        canvas.restoreToCount(saved)
        // RT
        saved = canvas.save()
        drawRTCorner(canvas, visibility.top)
        canvas.restoreToCount(saved)
    }

    private fun calculateCornerVisibility(): RectF {
        return when (mCornerVisibility) {
            NOLEFTCORNER -> RectF(0f, mCornerRadius, mCornerRadius, 0f)
            NORIGHTCORNER -> RectF(mCornerRadius, 0f, 0f, mCornerRadius)
            NOTOPCORNER -> RectF(0f, 0f, mCornerRadius, mCornerRadius)
            NOBOTTOMCORNER -> RectF(mCornerRadius, mCornerRadius, 0f, 0f)
            NOLT_RBCORNER -> RectF(0f, mCornerRadius, 0f, mCornerRadius)
            NORT_LBCORNER -> RectF(mCornerRadius, 0f, mCornerRadius, 0f)
            else -> RectF(mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius)
        }
    }

    private fun drawRTCorner(canvas: Canvas, cornerRadius: Float) {
        val edgeShadowTop = -cornerRadius - mShadowSize
        val inset = cornerRadius + mInsetShadow.toFloat() + mRawShadowSize / 2
        var right = mCardBounds.height() - 2 * inset
        val drawVerticalEdges = right > 0
        buildShadowCorners(cornerRadius)
        canvas.translate(mCardBounds.right - inset, mCardBounds.top + inset)
        canvas.rotate(90f)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawVerticalEdges) {
            if (mCornerVisibility == NOTOPCORNER || mCornerVisibility == NORT_LBCORNER) {
                right -= mCornerRadius
            }
            if (mCornerVisibility == NOBOTTOMCORNER || mCornerVisibility == NOLT_RBCORNER) {
                right += mCornerRadius
            }
            canvas.drawRect(0f, edgeShadowTop, right, -cornerRadius, mEdgeShadowPaint)
        }
    }

    private fun drawRBCorner(canvas: Canvas, cornerRadius: Float) {
        val edgeShadowTop = -cornerRadius - mShadowSize
        val inset = cornerRadius + mInsetShadow.toFloat() + mRawShadowSize / 2
        val drawHorizontalEdges = mCardBounds.width() - 2 * inset > 0

        buildShadowCorners(cornerRadius)
        canvas.translate(mCardBounds.right - inset, mCardBounds.bottom - inset)
        canvas.rotate(180f)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawHorizontalEdges) {
            var right = mCardBounds.width() - 2 * inset
            if (mCornerVisibility == NOLEFTCORNER || mCornerVisibility == NORT_LBCORNER)
                right += mCornerRadius
            if (mCornerVisibility == NORIGHTCORNER || mCornerVisibility == NOLT_RBCORNER)
                right -= mCornerRadius
            canvas.drawRect(0f, edgeShadowTop, right, -cornerRadius, mEdgeShadowPaint)
        }
    }

    private fun drawLBCorner(canvas: Canvas, cornerRadius: Float) {
        buildShadowCorners(cornerRadius)
        val edgeShadowTop = -cornerRadius - mShadowSize
        val inset = cornerRadius + mInsetShadow.toFloat() + mRawShadowSize / 2
        var right = mCardBounds.height() - 2 * inset
        val drawVerticalEdges = right > 0
        canvas.translate(mCardBounds.left + inset, mCardBounds.bottom - inset)
        canvas.rotate(270f)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawVerticalEdges) {
            if (mCornerVisibility == NOTOPCORNER || mCornerVisibility == NOLT_RBCORNER) {
                right += mCornerRadius
            }
            if (mCornerVisibility == NOBOTTOMCORNER || mCornerVisibility == NORT_LBCORNER) {
                right -= mCornerRadius
            }
            canvas.drawRect(0f, edgeShadowTop, right, -cornerRadius, mEdgeShadowPaint)
        }
    }

    private fun drawLTCorner(canvas: Canvas, cornerRadius: Float) {
        val edgeShadowTop = -cornerRadius - mShadowSize
        val inset = cornerRadius + mInsetShadow.toFloat() + mRawShadowSize / 2
        val drawHorizontalEdges = mCardBounds.width() - 2 * inset > 0
        buildShadowCorners(cornerRadius)

        canvas.translate(mCardBounds.left + inset, mCardBounds.top + inset)
        canvas.drawPath(mCornerShadowPath!!, mCornerShadowPaint)
        if (drawHorizontalEdges) {
            var right = mCardBounds.width() - 2 * inset
            if (mCornerVisibility == NORIGHTCORNER || mCornerVisibility == NORT_LBCORNER)
                right += mCornerRadius
            if (mCornerVisibility == NOLEFTCORNER || mCornerVisibility == NOLT_RBCORNER)
                right -= mCornerRadius
            canvas.drawRect(0f, edgeShadowTop, right, -cornerRadius, mEdgeShadowPaint)
        }
    }

    private fun buildShadowCorners(cornerRadius: Float) {
        val innerBounds = RectF(-cornerRadius, -cornerRadius, cornerRadius, cornerRadius)
        val outerBounds = RectF(innerBounds)
        outerBounds.inset(-mShadowSize, -mShadowSize)

        if (mCornerShadowPath == null) {
            mCornerShadowPath = Path()
        } else {
            mCornerShadowPath!!.reset()
        }
        mCornerShadowPath!!.fillType = Path.FillType.EVEN_ODD
        mCornerShadowPath!!.moveTo(-cornerRadius, 0f)
        mCornerShadowPath!!.rLineTo(-mShadowSize, 0f)
        // outer arc
        mCornerShadowPath!!.arcTo(outerBounds, 180f, 90f, false)
        // inner arc
        mCornerShadowPath!!.arcTo(innerBounds, 270f, -90f, false)
        mCornerShadowPath!!.close()
        val startRatio = cornerRadius / (cornerRadius + mShadowSize)
        mCornerShadowPaint.shader = RadialGradient(0f, 0f, cornerRadius + mShadowSize,
                intArrayOf(mShadowStartColor, mShadowStartColor, mShadowEndColor),
                floatArrayOf(0f, startRatio, 1f),
                Shader.TileMode.CLAMP)

        // we offset the content shadowSize/2 pixels up to make it more realistic.
        // this is why edge shadow shader has some extra space
        // When drawing bottom edge shadow, we use that extra space.
        mEdgeShadowPaint.shader = LinearGradient(0f, -cornerRadius + mShadowSize, 0f,
                -cornerRadius - mShadowSize,
                intArrayOf(mShadowStartColor, mShadowStartColor, mShadowEndColor),
                floatArrayOf(0f, .5f, 1f), Shader.TileMode.CLAMP)
        mEdgeShadowPaint.isAntiAlias = false
    }

    private fun buildComponents(bounds: Rect) {
        // Card is offset SHADOW_MULTIPLIER * maxShadowSize to account for the shadow shift.
        // We could have different top-bottom offsets to avoid extra gap above but in that case
        // center aligning Views inside the CardView would be problematic.
        val verticalOffset = mRawMaxShadowSize * SHADOW_MULTIPLIER
        mCardBounds.set(bounds.left + mRawMaxShadowSize, bounds.top + verticalOffset,
                bounds.right - mRawMaxShadowSize, bounds.bottom - verticalOffset)
        buildShadowCorners(mCornerRadius)
    }

    fun getCornerRadius(): Float {
        return mCornerRadius
    }

    fun getMaxShadowAndCornerPadding(into: Rect) {
        getPadding(into)
    }

    fun setShadowSize(size: Float) {
        setShadowSize(size, mRawMaxShadowSize)
    }

    fun setMaxShadowSize(size: Float) {
        setShadowSize(mRawShadowSize, size)
    }

    fun getShadowSize(): Float {
        return mRawShadowSize
    }

    fun getMaxShadowSize(): Float {
        return mRawMaxShadowSize
    }

    fun getMinWidth(): Float {
        val content = 2 * Math.max(mRawMaxShadowSize, mCornerRadius + mInsetShadow.toFloat() + mRawMaxShadowSize / 2)
        return content + (mRawMaxShadowSize + mInsetShadow) * 2
    }

    fun getMinHeight(): Float {
        val content = 2 * Math.max(mRawMaxShadowSize, mCornerRadius + mInsetShadow.toFloat()
                + mRawMaxShadowSize * SHADOW_MULTIPLIER / 2)
        return content + (mRawMaxShadowSize * SHADOW_MULTIPLIER + mInsetShadow) * 2
    }

    fun setColor(color: ColorStateList?) {
        setBackground(color)
        invalidateSelf()
    }

    fun getColor(): ColorStateList? {
        return mBackground
    }

    fun getCardRectSize(): RectF {
        return mCardBounds
    }

    fun getMoveDistance(): Pair<Float, Float>? {
        mTranslatePos?.let {
            val x = it.first.first + it.second.first
            val y = it.first.second + it.second.second
            return Pair(x, y)
        }
        return null
    }

    fun setShadowColor(startColor: Int, endColor: Int) {
        mShadowStartColor = startColor
        mShadowEndColor = endColor
        invalidateSelf()
    }

    fun setColors(backgroundColor: Int, shadowStartColor: Int, shadowEndColor: Int) {
        mBackground = ColorStateList.valueOf(backgroundColor)
        mPaint.color = mBackground!!.getColorForState(state, mBackground!!.defaultColor)
        mShadowStartColor = shadowStartColor
        mShadowEndColor = shadowEndColor
        invalidateSelf()
    }

    interface RoundRectHelper {
        fun drawRoundRect(canvas: Canvas, bounds: RectF, cornerRadius: Float, cornerVisibility: Int, paint: Paint)
    }
}