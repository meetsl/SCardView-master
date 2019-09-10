package com.meetsl.scardview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt


internal interface SCardViewImpl {
    fun initialize(cardView: SCardViewDelegate, context: Context, backgroundColor: ColorStateList,
                   radius: Float, elevation: Float, maxElevation: Float, direction: Int, cornerVisibility: Int, startColor: Int = -1, endColor: Int = -1)

    fun setRadius(cardView: SCardViewDelegate, radius: Float)

    fun getRadius(cardView: SCardViewDelegate): Float

    fun setElevation(cardView: SCardViewDelegate, elevation: Float)

    fun getElevation(cardView: SCardViewDelegate): Float

    fun initStatic()

    fun setMaxElevation(cardView: SCardViewDelegate, maxElevation: Float)

    fun getMaxElevation(cardView: SCardViewDelegate): Float

    fun getMinWidth(cardView: SCardViewDelegate): Float

    fun getMinHeight(cardView: SCardViewDelegate): Float

    fun updatePadding(cardView: SCardViewDelegate)

    fun onCompatPaddingChanged(cardView: SCardViewDelegate)

    fun onPreventCornerOverlapChanged(cardView: SCardViewDelegate)

    fun setBackgroundColor(cardView: SCardViewDelegate, color: ColorStateList?)

    fun setShadowColor(cardView: SCardViewDelegate, @ColorInt startColor: Int, @ColorInt endColor: Int)

    fun getBackgroundColor(cardView: SCardViewDelegate): ColorStateList

    fun getShadowBackground(cardView: SCardViewDelegate): Drawable

    fun setColors(cardView: SCardViewDelegate, backgroundColor: Int, shadowStartColor: Int, shadowEndColor: Int)
}
