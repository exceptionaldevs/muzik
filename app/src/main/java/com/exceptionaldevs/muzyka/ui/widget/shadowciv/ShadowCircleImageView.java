/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exceptionaldevs.muzyka.ui.widget.shadowciv;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.ui.widget.CircleImageView;

/**
 * Floating action buttons are used for a special type of promoted action. They are distinguished
 * by a circled icon floating above the UI and have special motion behaviors related to morphing,
 * launching, and the transferring anchor point.
 * <p/>
 * Floating action buttons come in two sizes: the default, which should be used in most cases, and
 * the mini, which should only be used to create visual continuity with other elements on the
 * screen.
 */
public class ShadowCircleImageView extends CircleImageView {

    // These values must match those in the attrs declaration
    private static final int SIZE_MINI = 1;
    private static final int SIZE_NORMAL = 0;

    private final Rect mShadowPadding;

    private final ShadowCircleImageViewImpl mImpl;
    private final int mSize;
    private int mContentDiameter;

    public ShadowCircleImageView(Context context) {
        this(context, null);
    }

    public ShadowCircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowCircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mShadowPadding = new Rect();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, defStyleAttr, R.style.Widget_Design_FloatingActionButton);

        mSize = a.getInt(R.styleable.FloatingActionButton_fabSize, SIZE_NORMAL);
        final float elevation = a.getDimension(R.styleable.FloatingActionButton_elevation, 0f);
        final float pressedTranslationZ = a.getDimension(
                R.styleable.FloatingActionButton_pressedTranslationZ, 0f);
        a.recycle();

        final ShadowViewDelegate delegate = new ShadowViewDelegate() {
            @Override
            public float getRadius() {
                return getSizeDimension() / 2f;
            }

            @Override
            public void setShadowPadding(int left, int top, int right, int bottom) {
                mShadowPadding.set(left, top, right, bottom);
                setPadding(left, top, right, bottom);
            }

            @Override
            public void setBackgroundDrawable(Drawable background) {
                ShadowCircleImageView.super.setBackgroundDrawable(background);
            }
        };

        if (Build.VERSION.SDK_INT >= 21) {
            mImpl = new ShadowCircleImageViewLollipop(this, delegate);
        } else {
            mImpl = new ShadowCircleImageViewEclairMr1(this, delegate);
        }

        mImpl.setElevation(elevation);
        mImpl.setPressedTranslationZ(pressedTranslationZ);

        setClickable(true);
    }

    public int getContentDiameter() {
        return mContentDiameter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int preferredSize = getSizeDimension();

        final int w = resolveAdjustedSize(preferredSize, widthMeasureSpec);
        final int h = resolveAdjustedSize(preferredSize, heightMeasureSpec);

        // As we want to stay circular, we set both dimensions to be the
        // smallest resolved dimension
        mContentDiameter = Math.min(w, h);

        // We add the shadow's padding to the measured dimension
        setMeasuredDimension(
                mContentDiameter + mShadowPadding.left + mShadowPadding.right,
                mContentDiameter + mShadowPadding.top + mShadowPadding.bottom);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }


    final int getSizeDimension() {
//        switch (mSize) {
//            case SIZE_MINI:
//                return getResources().getDimensionPixelSize(R.dimen.fab_size_mini);
//            case SIZE_NORMAL:
//            default:
//                return getResources().getDimensionPixelSize(R.dimen.fab_size_normal);
//        }

        //TODO: hardcoded, for heartview, but maybe there is a better solution?
        return getResources().getDimensionPixelSize(R.dimen.heartview_diameter);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mImpl.onDrawableStateChanged(getDrawableState());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        mImpl.jumpDrawableToCurrentState();
    }

    private static int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                // Parent says we can be as big as we want. Just don't be larger
                // than max size imposed on ourselves.
                result = desiredSize;
                break;
            case MeasureSpec.AT_MOST:
                // Parent says we can be as big as we want, up to specSize.
                // Don't be larger than specSize, and don't be larger than
                // the max size imposed on ourselves.
                result = Math.min(desiredSize, specSize);
                break;
            case MeasureSpec.EXACTLY:
                // No choice. Do what we are told.
                result = specSize;
                break;
        }
        return result;
    }

    static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3:
                return PorterDuff.Mode.SRC_OVER;
            case 5:
                return PorterDuff.Mode.SRC_IN;
            case 9:
                return PorterDuff.Mode.SRC_ATOP;
            case 14:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            default:
                return defaultMode;
        }
    }


}
