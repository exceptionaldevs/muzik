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

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import com.exceptionaldevs.muzyka.utils.Utils;

class ShadowCircleImageViewEclairMr1 extends ShadowCircleImageViewImpl {
    private float mElevation;
    private float mPressedTranslationZ;
    private int mAnimationDuration;

    private StateListAnimator mStateListAnimator;

    ShadowDrawableWrapper mShadowDrawable;

    ShadowCircleImageViewEclairMr1(View view, ShadowViewDelegate shadowViewDelegate) {
        super(view, shadowViewDelegate);

        mAnimationDuration = view.getResources().getInteger(android.R.integer.config_shortAnimTime);

        mStateListAnimator = new StateListAnimator();
        mStateListAnimator.setTarget(view);

        // Elevate with translationZ when pressed or focused
        mStateListAnimator.addState(PRESSED_ENABLED_STATE_SET,
                setupAnimation(new ElevateToTranslationZAnimation()));
        mStateListAnimator.addState(FOCUSED_ENABLED_STATE_SET,
                setupAnimation(new ElevateToTranslationZAnimation()));
        // Reset back to elevation by default
        mStateListAnimator.addState(EMPTY_STATE_SET,
                setupAnimation(new ResetElevationAnimation()));

        mShadowDrawable = new ShadowDrawableWrapper(
                mView.getResources(),
                mShadowViewDelegate.getRadius(),
                mElevation,
                mElevation + mPressedTranslationZ);
        mShadowDrawable.setAddPaddingForCorners(false);

        mShadowViewDelegate.setBackgroundDrawable(mShadowDrawable);

        updatePadding();
    }


    @Override
    void setElevation(float elevation) {
        if (mElevation != elevation && mShadowDrawable != null) {
            mShadowDrawable.setShadowSize(elevation, elevation + mPressedTranslationZ);
            mElevation = elevation;
            updatePadding();
        }
    }

    @Override
    void setPressedTranslationZ(float translationZ) {
        if (mPressedTranslationZ != translationZ && mShadowDrawable != null) {
            mPressedTranslationZ = translationZ;
            mShadowDrawable.setMaxShadowSize(mElevation + translationZ);
            updatePadding();
        }
    }

    @Override
    void onDrawableStateChanged(int[] state) {
        mStateListAnimator.setState(state);
    }

    @Override
    void jumpDrawableToCurrentState() {
        mStateListAnimator.jumpToCurrentState();
    }

    private void updatePadding() {
        Rect rect = new Rect();
        mShadowDrawable.getPadding(rect);
        mShadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
    }

    private Animation setupAnimation(Animation animation) {
        animation.setInterpolator(Utils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        animation.setDuration(mAnimationDuration);
        return animation;
    }

    private abstract class BaseShadowAnimation extends Animation {
        private float mShadowSizeStart;
        private float mShadowSizeDiff;

        @Override
        public void reset() {
            super.reset();

            mShadowSizeStart = mShadowDrawable.getShadowSize();
            mShadowSizeDiff = getTargetShadowSize() - mShadowSizeStart;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mShadowDrawable.setShadowSize(mShadowSizeStart + (mShadowSizeDiff * interpolatedTime));
        }

        /**
         * @return the shadow size we want to animate to.
         */
        protected abstract float getTargetShadowSize();
    }

    private class ResetElevationAnimation extends BaseShadowAnimation {
        @Override
        protected float getTargetShadowSize() {
            return mElevation;
        }
    }

    private class ElevateToTranslationZAnimation extends BaseShadowAnimation {
        @Override
        protected float getTargetShadowSize() {
            return mElevation + mPressedTranslationZ;
        }
    }

    private static ColorStateList createColorStateList(int selectedColor) {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];
        int i = 0;

        states[i] = FOCUSED_ENABLED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        states[i] = PRESSED_ENABLED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        // Default enabled state
        states[i] = new int[0];
        colors[i] = Color.TRANSPARENT;
        i++;

        return new ColorStateList(states, colors);
    }
}