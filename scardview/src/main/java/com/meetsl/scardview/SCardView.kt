package com.meetsl.scardview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout


/**
 * Created by shilong
 *  2018/9/11.
 */
class SCardView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    private val COLOR_BACKGROUND_ATTR = intArrayOf(android.R.attr.colorBackground)
    private val DEFAULT_CHILD_GRAVITY = Gravity.TOP or Gravity.START
    private var IMPL: SCardViewImpl

    private var mCompatPadding: Boolean = false

    private var mPreventCornerOverlap: Boolean = false
    /**
     *  是否使用边角区域放置内容
     */
    private var mUseCornerArea: Boolean = false

    /**
     * CardView requires to have a particular minimum size to draw shadows before API 21. If
     * developer also sets min width/height, they might be overridden.
     *
     * CardView works around this issue by recording user given parameters and using an internal
     * method to set them.
     */
    internal var mUserSetMinWidth: Int = 0

    internal var mUserSetMinHeight: Int = 0
    internal val mContentPadding = Rect()

    internal val mShadowBounds = Rect()

    private val mCardViewDelegate = object : SCardViewDelegate {
        private var mCardBackground: Drawable? = null

        override fun setCardBackground(drawable: Drawable) {
            mCardBackground = drawable
            setBackgroundDrawable(drawable)
        }

        override fun getUseCompatPadding(): Boolean {
            return this@SCardView.getUseCompatPadding()
        }

        override fun getPreventCornerOverlap(): Boolean {
            return this@SCardView.getPreventCornerOverlap()
        }

        override fun setShadowPadding(left: Int, top: Int, right: Int, bottom: Int) {
            mShadowBounds.set(left, top, right, bottom)
            super@SCardView.setPadding(left + mContentPadding.left, top + mContentPadding.top,
                    right + mContentPadding.right, bottom + mContentPadding.bottom)
        }

        override fun setMinWidthHeightInternal(width: Int, height: Int) {
            if (width > mUserSetMinWidth) {
                super@SCardView.setMinimumWidth(width)
            }
            if (height > mUserSetMinHeight) {
                super@SCardView.setMinimumHeight(height)
            }
        }

        override fun getCardBackground(): Drawable? {
            return mCardBackground
        }

        override fun getCardView(): View {
            return this@SCardView
        }
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.cardViewStyle)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SCardView, defStyleAttr,
                R.style.CardView)
        val backgroundColor: ColorStateList?
        if (a.hasValue(R.styleable.SCardView_cardBackgroundColor)) {
            backgroundColor = a.getColorStateList(R.styleable.SCardView_cardBackgroundColor)
        } else {
            // There isn't one set, so we'll compute one based on the theme
            val aa = getContext().obtainStyledAttributes(COLOR_BACKGROUND_ATTR)
            val themeColorBackground = aa.getColor(0, 0)
            aa.recycle()

            // If the theme colorBackground is light, use our own light color, otherwise dark
            val hsv = FloatArray(3)
            Color.colorToHSV(themeColorBackground, hsv)
            backgroundColor = ColorStateList.valueOf(if (hsv[2] > 0.5f)
                resources.getColor(R.color.sl_cardview_light_background)
            else
                resources.getColor(R.color.sl_cardview_dark_background))
        }
        val radius = a.getDimension(R.styleable.SCardView_cardCornerRadius, 0f)
        val elevation = a.getDimension(R.styleable.SCardView_cardElevation, 0f)
        var maxElevation = a.getDimension(R.styleable.SCardView_cardMaxElevation, 0f)
        mCompatPadding = a.getBoolean(R.styleable.SCardView_cardUseCompatPadding, false)
        mPreventCornerOverlap = a.getBoolean(R.styleable.SCardView_cardPreventCornerOverlap, true)
        mUseCornerArea = a.getBoolean(R.styleable.SCardView_cardUseCornerArea, false)
        val defaultPadding = a.getDimensionPixelSize(R.styleable.SCardView_contentPadding, 0)
        mContentPadding.left = a.getDimensionPixelSize(R.styleable.SCardView_contentPaddingLeft,
                defaultPadding)
        mContentPadding.top = a.getDimensionPixelSize(R.styleable.SCardView_contentPaddingTop,
                defaultPadding)
        mContentPadding.right = a.getDimensionPixelSize(R.styleable.SCardView_contentPaddingRight,
                defaultPadding)
        mContentPadding.bottom = a.getDimensionPixelSize(R.styleable.SCardView_contentPaddingBottom,
                defaultPadding)
        if (elevation > maxElevation) {
            maxElevation = elevation
        }
        val direction = a.getInt(R.styleable.SCardView_cardLightDirection, DIRECTION_TOP)
        val cardCornerVisibility = a.getInt(R.styleable.SCardView_cardCornerVisibility, NONE)
        val shadowStartColor = a.getColor(R.styleable.SCardView_cardShadowStartColor, -1)
        val shadowEndColor = a.getColor(R.styleable.SCardView_cardShadowEndColor, -1)
        mUserSetMinWidth = a.getDimensionPixelSize(R.styleable.SCardView_android_minWidth, 0)
        mUserSetMinHeight = a.getDimensionPixelSize(R.styleable.SCardView_android_minHeight, 0)
        a.recycle()

        IMPL = when {
            Build.VERSION.SDK_INT >= 17 -> {
                if (cardCornerVisibility == NONE)
                    SCardViewApi17Impl()
                else
                    SCardViewBaseImpl()
            }
            else -> {
                SCardViewBaseImpl()
            }
        }
        IMPL.initStatic()

        IMPL.initialize(mCardViewDelegate, context, backgroundColor, radius,
                elevation, maxElevation, direction, cardCornerVisibility, shadowStartColor, shadowEndColor)
    }


    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        // NO OP
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        // NO OP
    }

    /**
     * Returns whether CardView will add inner padding on platforms Lollipop and after.
     *
     * @return `true` if CardView adds inner padding on platforms Lollipop and after to
     * have same dimensions with platforms before Lollipop.
     */
    fun getUseCompatPadding(): Boolean {
        return mCompatPadding
    }

    /**
     * CardView adds additional padding to draw shadows on platforms before Lollipop.
     *
     *
     * This may cause Cards to have different sizes between Lollipop and before Lollipop. If you
     * need to align CardView with other Views, you may need api version specific dimension
     * resources to account for the changes.
     * As an alternative, you can set this flag to `true` and CardView will add the same
     * padding values on platforms Lollipop and after.
     *
     *
     * Since setting this flag to true adds unnecessary gaps in the UI, default value is
     * `false`.
     *
     * @param useCompatPadding `true>` if CardView should add padding for the shadows on
     * platforms Lollipop and above.
     * @attr ref android.support.v7.cardview.R.styleable#CardView_cardUseCompatPadding
     */
    fun setUseCompatPadding(useCompatPadding: Boolean) {
        if (mCompatPadding != useCompatPadding) {
            mCompatPadding = useCompatPadding
            IMPL.onCompatPaddingChanged(mCardViewDelegate)
        }
    }

    /**
     * Sets the padding between the Card's edges and the children of CardView.
     *
     *
     * Depending on platform version or [.getUseCompatPadding] settings, CardView may
     * update these values before calling [android.view.View.setPadding].
     *
     * @param left   The left padding in pixels
     * @param top    The top padding in pixels
     * @param right  The right padding in pixels
     * @param bottom The bottom padding in pixels
     * @attr ref android.support.v7.cardview.R.styleable#CardView_contentPadding
     * @attr ref android.support.v7.cardview.R.styleable#CardView_contentPaddingLeft
     * @attr ref android.support.v7.cardview.R.styleable#CardView_contentPaddingTop
     * @attr ref android.support.v7.cardview.R.styleable#CardView_contentPaddingRight
     * @attr ref android.support.v7.cardview.R.styleable#CardView_contentPaddingBottom
     */
    fun setContentPadding(left: Int, top: Int, right: Int, bottom: Int) {
        mContentPadding.set(left, top, right, bottom)
        IMPL.updatePadding(mCardViewDelegate)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        when (widthMode) {
            View.MeasureSpec.EXACTLY, View.MeasureSpec.AT_MOST -> {
                val minWidth = Math.ceil(IMPL.getMinWidth(mCardViewDelegate).toDouble()).toInt()
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(minWidth,
                        View.MeasureSpec.getSize(widthMeasureSpec)), widthMode)
            }
            View.MeasureSpec.UNSPECIFIED -> {
            }
        }// Do nothing

        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
        when (heightMode) {
            View.MeasureSpec.EXACTLY, View.MeasureSpec.AT_MOST -> {
                val minHeight = Math.ceil(IMPL.getMinHeight(mCardViewDelegate).toDouble()).toInt()
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(minHeight,
                        View.MeasureSpec.getSize(heightMeasureSpec)), heightMode)
            }
            View.MeasureSpec.UNSPECIFIED -> {
            }
        }// Do nothing
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutChildren(left, top, right, bottom, false /* no force left gravity */)
    }

    private fun layoutChildren(left: Int, top: Int, right: Int, bottom: Int, forceLeftGravity: Boolean) {
        val count = childCount
        val bg = IMPL.getShadowBackground(mCardViewDelegate) as SRoundRectDrawableWithShadow
        val rectF = bg.getCardRectSize()
        val movePair = bg.getMoveDistance()
        val cornerRadius = bg.getCornerRadius()
        val iex = (cornerRadius - (Math.sqrt(2.0) * cornerRadius) / 2 + 0.5f).toInt()
        var parentLeft: Int
        var parentRight: Int
        var parentTop: Int
        var parentBottom: Int
        if (movePair != null) {
            val halfWidth = (right - left) / 2
            val halfHeight = (bottom - top) / 2
            val verticalMove = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) movePair.second else 0f
            val horizontalMove = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) movePair.first else 0f
            parentLeft = (halfWidth - rectF.width() / 2 + horizontalMove).toInt()
            parentRight = (halfWidth + rectF.width() / 2 + horizontalMove).toInt()
            parentTop = (halfHeight - rectF.height() / 2 + verticalMove).toInt()
            parentBottom = (halfHeight + rectF.height() / 2 + verticalMove).toInt()
            //控制边角区域是否显示内容
            if (!mUseCornerArea) {
                parentLeft += iex
                parentTop += iex
                parentRight -= iex
                parentBottom -= iex
            }
            //内容显示区域修正，防止内容显示不全
            if (parentLeft < paddingLeft)
                parentLeft = paddingLeft
            if (parentRight > (right - left - paddingRight))
                parentRight = right - left - paddingRight
            if (parentTop < paddingTop)
                parentTop = paddingTop
            if (parentBottom > (bottom - top - paddingBottom))
                parentBottom = bottom - top - paddingBottom
        } else {
            parentLeft = paddingLeft
            parentRight = right - left - paddingRight
            parentTop = paddingTop
            parentBottom = bottom - top - paddingBottom
        }


        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val lp = child.layoutParams as LayoutParams

                val width = child.measuredWidth
                val height = child.measuredHeight

                val childLeft: Int
                val childTop: Int

                var gravity = lp.gravity
                if (gravity == -1) {
                    gravity = DEFAULT_CHILD_GRAVITY
                }

                val layoutDirection = layoutDirection //Please ignore this warning , this code work well under the Android 17
                val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
                val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK

                childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                    Gravity.CENTER_HORIZONTAL -> parentLeft + (parentRight - parentLeft - width) / 2 +
                            lp.leftMargin - lp.rightMargin
                    Gravity.END -> {
                        if (!forceLeftGravity) {
                            parentRight - width - lp.rightMargin
                        } else {
                            parentLeft + lp.leftMargin
                        }
                    }
                    Gravity.START -> parentLeft + lp.leftMargin
                    else -> parentLeft + lp.leftMargin
                }

                childTop = when (verticalGravity) {
                    Gravity.TOP -> parentTop + lp.topMargin
                    Gravity.CENTER_VERTICAL -> parentTop + (parentBottom - parentTop - height) / 2 +
                            lp.topMargin - lp.bottomMargin
                    Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                    else -> parentTop + lp.topMargin
                }
                var childRight = childLeft + width
                var childBottom = childTop + height
                //根据边角控制 修正 child 显示大小
                if (!mUseCornerArea) {
                    if (childRight > parentRight)
                        childRight = parentRight
                    if (childBottom > parentBottom)
                        childBottom = parentBottom
                }
                child.layout(childLeft, childTop, childRight, childBottom)
            }
        }
    }

    override fun setMinimumWidth(minWidth: Int) {
        mUserSetMinWidth = minWidth
        super.setMinimumWidth(minWidth)
    }

    override fun setMinimumHeight(minHeight: Int) {
        mUserSetMinHeight = minHeight
        super.setMinimumHeight(minHeight)
    }

    /**
     * Updates the background color of the CardView
     *
     * @param color The new color to set for the card background
     * @attr ref android.support.v7.cardview.R.styleable#CardView_cardBackgroundColor
     */
    fun setCardBackgroundColor(@ColorInt color: Int) {
        IMPL.setBackgroundColor(mCardViewDelegate, ColorStateList.valueOf(color))
    }

    /**
     * Updates the background ColorStateList of the CardView
     *
     * @param color The new ColorStateList to set for the card background
     * @attr ref android.support.v7.cardview.R.styleable#CardView_cardBackgroundColor
     */
    fun setCardBackgroundColor(color: ColorStateList?) {
        IMPL.setBackgroundColor(mCardViewDelegate, color)
    }

    /**
     * Returns the background color state list of the CardView.
     *
     * @return The background color state list of the CardView.
     */
    fun getCardBackgroundColor(): ColorStateList {
        return IMPL.getBackgroundColor(mCardViewDelegate)
    }

    /**
     * Returns the inner padding after the Card's left edge
     *
     * @return the inner padding after the Card's left edge
     */
    fun getContentPaddingLeft(): Int {
        return mContentPadding.left
    }

    /**
     * Returns the inner padding before the Card's right edge
     *
     * @return the inner padding before the Card's right edge
     */
    fun getContentPaddingRight(): Int {
        return mContentPadding.right
    }

    /**
     * Returns the inner padding after the Card's top edge
     *
     * @return the inner padding after the Card's top edge
     */
    fun getContentPaddingTop(): Int {
        return mContentPadding.top
    }

    /**
     * Returns the inner padding before the Card's bottom edge
     *
     * @return the inner padding before the Card's bottom edge
     */
    fun getContentPaddingBottom(): Int {
        return mContentPadding.bottom
    }

    /**
     * Updates the corner radius of the CardView.
     *
     * @param radius The radius in pixels of the corners of the rectangle shape
     * @attr ref android.support.v7.cardview.R.styleable#CardView_cardCornerRadius
     * @see .setRadius
     */
    fun setRadius(radius: Float) {
        IMPL.setRadius(mCardViewDelegate, radius)
    }

    /**
     * Returns the corner radius of the CardView.
     *
     * @return Corner radius of the CardView
     * @see .getRadius
     */
    fun getRadius(): Float {
        return IMPL.getRadius(mCardViewDelegate)
    }

    /**
     * Updates the backward compatible elevation of the CardView.
     *
     * @param elevation The backward compatible elevation in pixels.
     * @attr ref android.support.v7.cardview.R.styleable#CardView_cardElevation
     * @see .getCardElevation
     * @see .setMaxCardElevation
     */
    fun setCardElevation(elevation: Float) {
        IMPL.setElevation(mCardViewDelegate, elevation)
    }

    /**
     * Returns the backward compatible elevation of the CardView.
     *
     * @return Elevation of the CardView
     * @see .setCardElevation
     * @see .getMaxCardElevation
     */
    fun getCardElevation(): Float {
        return IMPL.getElevation(mCardViewDelegate)
    }

    /**
     * Updates the backward compatible maximum elevation of the CardView.
     *
     *
     * Calling this method has no effect if device OS version is Lollipop or newer and
     * [.getUseCompatPadding] is `false`.
     *
     * @param maxElevation The backward compatible maximum elevation in pixels.
     * @attr ref android.support.v7.cardview.R.styleable#CardView_cardMaxElevation
     * @see .setCardElevation
     * @see .getMaxCardElevation
     */
    fun setMaxCardElevation(maxElevation: Float) {
        IMPL.setMaxElevation(mCardViewDelegate, maxElevation)
    }

    /**
     * Returns the backward compatible maximum elevation of the CardView.
     *
     * @return Maximum elevation of the CardView
     * @see .setMaxCardElevation
     * @see .getCardElevation
     */
    fun getMaxCardElevation(): Float {
        return IMPL.getMaxElevation(mCardViewDelegate)
    }

    /**
     * Returns whether CardView should add extra padding to content to avoid overlaps with rounded
     * corners on pre-Lollipop platforms.
     *
     * @return True if CardView prevents overlaps with rounded corners on platforms before Lollipop.
     * Default value is `true`.
     */
    fun getPreventCornerOverlap(): Boolean {
        return mPreventCornerOverlap
    }

    /**
     * On pre-Lollipop platforms, CardView does not clip the bounds of the Card for the rounded
     * corners. Instead, it adds padding to content so that it won't overlap with the rounded
     * corners. You can disable this behavior by setting this field to `false`.
     *
     *
     * Setting this value on Lollipop and above does not have any effect unless you have enabled
     * compatibility padding.
     *
     * @param preventCornerOverlap Whether CardView should add extra padding to content to avoid
     * overlaps with the CardView corners.
     * @attr ref android.support.v7.cardview.R.styleable#CardView_cardPreventCornerOverlap
     * @see .setUseCompatPadding
     */
    fun setPreventCornerOverlap(preventCornerOverlap: Boolean) {
        if (preventCornerOverlap != mPreventCornerOverlap) {
            mPreventCornerOverlap = preventCornerOverlap
            IMPL.onPreventCornerOverlapChanged(mCardViewDelegate)
        }
    }
}