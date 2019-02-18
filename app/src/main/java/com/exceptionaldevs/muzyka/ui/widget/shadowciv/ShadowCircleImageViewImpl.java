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
import android.graphics.drawable.Drawable;
import android.view.View;

abstract class ShadowCircleImageViewImpl {

    static final int[] PRESSED_ENABLED_STATE_SET = {android.R.attr.state_pressed,
            android.R.attr.state_enabled};
    static final int[] FOCUSED_ENABLED_STATE_SET = {android.R.attr.state_focused,
            android.R.attr.state_enabled};
    static final int[] EMPTY_STATE_SET = new int[0];

    final View mView;
    final ShadowViewDelegate mShadowViewDelegate;

    ShadowCircleImageViewImpl(View view, ShadowViewDelegate shadowViewDelegate) {
        mView = view;
        mShadowViewDelegate = shadowViewDelegate;
    }

    abstract void setElevation(float elevation);

    abstract void setPressedTranslationZ(float translationZ);

    abstract void onDrawableStateChanged(int[] state);

    abstract void jumpDrawableToCurrentState();

    Drawable createBorderDrawable(ColorStateList backgroundTint) {
//        Drawable borderDrawable = ContextCompat.getDrawable(mView.getContext(),
//                R.drawable.fab_background);
//        borderDrawable = DrawableCompat.wrap(borderDrawable);
//        DrawableCompat.setTintList(borderDrawable, backgroundTint);
//        DrawableCompat.setTintMode(borderDrawable, PorterDuff.Mode.DST_OVER);
        return null;
    }
}
