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

package com.exceptionaldevs.muzyka.utils;

import android.support.v4.view.ViewCompat;
import android.util.Property;
import android.view.View;

/**
 * Borrowed from the design lib.
 *
 * Utility helper for moving a {@link View} around using
 * {@link View#offsetLeftAndRight(int)} and
 * {@link View#offsetTopAndBottom(int)}.
 * <p>
 * Also the setting of absolute offsets (similar to translationX/Y), rather than additive
 * offsets.
 */
public class ViewOffsetHelper {

    private final View mView;

    private int mLayoutTop;
    private int mLayoutLeft;
    private int mOffsetTop;
    private int mOffsetLeft;

    public ViewOffsetHelper(View view) {
        mView = view;
    }

    public void onViewLayout() {
        // Now grab the intended top
        mLayoutTop = mView.getTop();
        mLayoutLeft = mView.getLeft();

        // And offset it as needed
        updateOffsets();
    }

    private void updateOffsets() {
        ViewCompat.offsetTopAndBottom(mView, mOffsetTop - (mView.getTop() - mLayoutTop));
        ViewCompat.offsetLeftAndRight(mView, mOffsetLeft - (mView.getLeft() - mLayoutLeft));

        // Manually invalidate the view and parent to make sure we get drawn pre-M
//        if (Build.VERSION.SDK_INT < 23) {
//            tickleInvalidationFlag(mView);
//            final ViewParent vp = mView.getParent();
//            if (vp instanceof View) {
//                tickleInvalidationFlag((View) vp);
//            }
//        }
        // commented this out it helps with the flickering of the bottom sheet, while sliding in
    }

    private static void tickleInvalidationFlag(View view) {
        final float x = ViewCompat.getTranslationX(view);
        ViewCompat.setTranslationY(view, x + 1);
        ViewCompat.setTranslationY(view, x);
    }

    /**
     * Set the top and bottom offset for this {@link ViewOffsetHelper}'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    public boolean setTopAndBottomOffset(int offset, boolean moveView) {
        if (mOffsetTop != offset) {
            mOffsetTop = offset;
            if (moveView) {
                updateOffsets();
            }
            return true;
        }
        return false;
    }

    public boolean setTopAndBottomOffset(int offset) {
        return setTopAndBottomOffset(offset, true);
    }

    /**
     * Set the left and right offset for this {@link ViewOffsetHelper}'s view.
     *
     * @param offset the offset in px.
     * @return true if the offset has changed
     */
    public boolean setLeftAndRightOffset(int offset, boolean moveView) {
        if (mOffsetLeft != offset) {
            mOffsetLeft = offset;
            if (moveView) {
                updateOffsets();
            }
            return true;
        }
        return false;
    }

    public boolean setLeftAndRightOffset(int offset) {
        return setLeftAndRightOffset(offset, true);
    }

    public int getTopAndBottomOffset() {
        return mOffsetTop;
    }

    public int getLeftAndRightOffset() {
        return mOffsetLeft;
    }

    public static final Property<ViewOffsetHelper, Integer> OFFSET_Y = new AnimUtils
            .IntProperty<ViewOffsetHelper>("topAndBottomOffset") {

        @Override
        public void setValue(ViewOffsetHelper viewOffsetHelper, int offset) {
            viewOffsetHelper.setTopAndBottomOffset(offset);
        }

        @Override
        public Integer get(ViewOffsetHelper viewOffsetHelper) {
            return viewOffsetHelper.getTopAndBottomOffset();
        }
    };
}
