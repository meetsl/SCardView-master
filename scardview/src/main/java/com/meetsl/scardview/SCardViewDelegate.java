package com.meetsl.scardview;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * create by bigman
 * create date == 2018/7/10
 * create time == 11:01
 */

public interface SCardViewDelegate {
    void setCardBackground(Drawable drawable);

    Drawable getCardBackground();

    boolean getUseCompatPadding();

    boolean getPreventCornerOverlap();

    void setShadowPadding(int left, int top, int right, int bottom);

    void setMinWidthHeightInternal(int width, int height);

    View getCardView();
}
