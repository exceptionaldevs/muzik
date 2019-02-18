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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.TargetApi;
import android.graphics.Outline;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ShadowCircleImageViewLollipop extends ShadowCircleImageViewImpl {
    private Interpolator mInterpolator;

    ShadowCircleImageViewLollipop(View view, ShadowViewDelegate shadowViewDelegate) {
        super(view, shadowViewDelegate);

        if (!view.isInEditMode()) {
            mInterpolator = AnimationUtils.loadInterpolator(mView.getContext(),
                    android.R.interpolator.fast_out_slow_in);
        }

        ViewOutlineProvider outlineProvider = new ViewOutlineProvider() {

            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, (int) mShadowViewDelegate.getRadius() * 2, (int) mShadowViewDelegate.getRadius() * 2);
            }
        };

        mView.setOutlineProvider(outlineProvider);
    }

    @Override
    public void setElevation(float elevation) {
        ViewCompat.setElevation(mView, elevation);
    }

    @Override
    void setPressedTranslationZ(float translationZ) {
        StateListAnimator stateListAnimator = new StateListAnimator();

        // Animate translationZ to our value when pressed or focused
        stateListAnimator.addState(PRESSED_ENABLED_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", translationZ)));
        stateListAnimator.addState(FOCUSED_ENABLED_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", translationZ)));
        // Animate translationZ to 0 otherwise
        stateListAnimator.addState(EMPTY_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", 0f)));

        mView.setStateListAnimator(stateListAnimator);
    }

    @Override
    void onDrawableStateChanged(int[] state) {
        // no-op
    }

    @Override
    void jumpDrawableToCurrentState() {
        // no-op
    }

    private Animator setupAnimator(Animator animator) {
        animator.setInterpolator(mInterpolator);
        return animator;
    }
}
