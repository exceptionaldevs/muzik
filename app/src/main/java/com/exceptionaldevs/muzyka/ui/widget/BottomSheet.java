/*
 * Copyright 2015 Google Inc.
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

package com.exceptionaldevs.muzyka.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.utils.Logy;
import com.exceptionaldevs.muzyka.utils.ViewOffsetHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FrameLayout} which can be dragged downward to be dismissed (either directly or via a
 * specified nested scrolling child).  It expects to contain a single child view and exposes a
 * listener interface to react to it's dismissal.
 * <p/>
 * View dragging has the benefit of reporting it's velocity allowing us to respond to flings etc
 * but does not allow children to scroll.  Nested scrolling allows child views to scroll (duh)
 * but does not report velocity. We combine both to get the best experience we can with the APIs.
 */
public class BottomSheet extends FrameLayout {

    private boolean firstLayout;
    private int peekDistance = -1;
    // configurable attributes
    private int dragDismissDistance = Integer.MAX_VALUE;
    private boolean hasScrollingChild = false;
    private int scrollingChildId = -1;

    // child views & helpers
    private View dragView;
    private View scrollingChild;
    private ViewDragHelper viewDragHelper;
    private ViewOffsetHelper dragViewOffsetHelper;

    // state
    private final int FLING_VELOCITY;
    private List<Listener> listeners;
    private boolean isDismissing;
    public boolean isSettleing;
    private int dragViewLeft;
    private int dragViewTop;
    private int dragViewBottom;
    private boolean lastNestedScrollWasDownward;
    private int mPeekTop;
    private boolean shouldCheckIsExpanded;
    private boolean mPeeked;

    public BottomSheet(Context context) {
        this(context, null, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        FLING_VELOCITY = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        final TypedArray a =
                getContext().obtainStyledAttributes(attrs, R.styleable.BottomSheet, 0, 0);

        if (a.hasValue(R.styleable.BottomSheet_scrollingChild)) {
            hasScrollingChild = true;
            scrollingChildId = a.getResourceId(R.styleable.BottomSheet_scrollingChild,
                    scrollingChildId);
        }
        if (a.hasValue(R.styleable.BottomSheet_dragDismissDistance)) {
            dragDismissDistance = a.getDimensionPixelSize(
                    R.styleable.BottomSheet_dragDismissDistance, dragDismissDistance);
        }

        if (a.hasValue(R.styleable.BottomSheet_peekDistance)) {
            peekDistance = a.getDimensionPixelSize(
                    R.styleable.BottomSheet_peekDistance, peekDistance);
        }
        a.recycle();

        firstLayout = true;
    }

    public void addListener(Listener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }


    public interface Listener {
        void onDragDismissed();

        void onDrag(int top);

        void onBottomSheetExpanded(int top);
    }

    public void doDismiss() {
        viewDragHelper.settleCapturedViewAt(dragViewLeft, dragViewBottom);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
//        if (dragView != null) {
//            throw new UnsupportedOperationException("BottomSheet must only have 1 child view");
//        }
        if (dragView == null) {
            dragView = child;
            dragViewOffsetHelper = new ViewOffsetHelper(dragView);
            if (hasScrollingChild) {
                scrollingChild = dragView.findViewById(scrollingChildId);
            }
        }
        super.addView(child, index, params);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        viewDragHelper = ViewDragHelper.create(this, dragHelperCallbacks);
    }

    int mLayoutCount = 0;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (dragView != null && dragView.isLaidOut()) {
            dragViewLeft = dragView.getLeft();
            dragViewTop = dragView.getTop();
            dragViewBottom = dragView.getBottom();
            dragViewOffsetHelper.onViewLayout();

            mLayoutCount++;
            Logy.d("dragViewOffsetHelper", "mPeeked " + mPeeked + " dragViewTop " + dragViewTop + " layout " + mLayoutCount + " " + "offset" + dragViewOffsetHelper.getTopAndBottomOffset());
        }

        if (firstLayout && peekDistance != -1) {
            mPeekTop = Math.max((bottom - peekDistance) + dragViewTop, dragViewTop);
            dragViewOffsetHelper.setTopAndBottomOffset(mPeekTop);
            firstLayout = false;
            mPeeked = true;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel();
            return false;
        }
        if (scrollingChild != null
                && isDraggableViewUnder(scrollingChild, (int) ev.getX(), (int) ev.getY())
                && (scrollingChild.canScrollVertically(1)
                || scrollingChild.canScrollVertically(-1))) {
            return false;
        }

        return isDraggableViewUnder(this, (int) ev.getX(), (int) ev.getY())
                && (viewDragHelper.shouldInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev));
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        viewDragHelper.processTouchEvent(ev);
        if (viewDragHelper.getCapturedView() == null) {
            return super.onTouchEvent(ev);
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & View.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        // if scrolling downward, use any unconsumed (i.e. not used by the scrolling child)
        // to drag the sheet downward
        lastNestedScrollWasDownward = dyUnconsumed < 0;
        if (lastNestedScrollWasDownward) {
            dragView.offsetTopAndBottom(-dyUnconsumed);
            dragViewOffsetHelper.setTopAndBottomOffset(dragView.getTop(), false);
            mPeeked = false;
        }
        shouldCheckIsExpanded = true;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // if scrolling upward & the sheet has been dragged downward
        // then drag back into place before allowing scrolls
        if (dy > 0) {
            final int dragDisplacement = dragView.getTop() - dragViewTop;
            if (dragDisplacement > 0) {
                final int consume = Math.min(dragDisplacement, dy);
                dragView.offsetTopAndBottom(-consume);
                dragViewOffsetHelper.setTopAndBottomOffset(dragView.getTop(), false);
                mPeeked = false;
                consumed[1] = consume;
                lastNestedScrollWasDownward = false;
            }
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        if (mPeeked) {
            // we are peeked return
            return;
        }

        final int dragDisplacement = dragView.getTop() - dragViewTop;
        if (dragDisplacement == 0 && shouldCheckIsExpanded) {
            dispatchExpandedCallback();
        }

        if (dragDisplacement == 0) return; // we are not displaced return

        // check if we should perform a dismiss or settle back into place
        final boolean dismiss =
                lastNestedScrollWasDownward && dragDisplacement >= dragDismissDistance;
        // animate either back into place or to bottom
        ObjectAnimator settleAnim = ObjectAnimator.ofInt(dragViewOffsetHelper,
                ViewOffsetHelper.OFFSET_Y,
                dragView.getTop(),
                dismiss ? dragViewBottom : dragViewTop);
        settleAnim.setDuration(200L);
        settleAnim.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.fast_out_slow_in));
        settleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dragViewOffsetHelper.setTopAndBottomOffset(dismiss ? dragViewBottom : dragViewTop, false);

                if (dismiss) {
                    dispatchDismissCallback();
                } else {
                    dispatchExpandedCallback();
                }
            }
        });

        settleAnim.start();
    }

    protected void dispatchDismissCallback() {
        if (listeners != null && listeners.size() > 0) {
            for (Listener listener : listeners) {
                listener.onDragDismissed();
            }
        }
    }

    protected void dispatchDragCallback() {
        if (listeners != null && listeners.size() > 0) {
            for (Listener listener : listeners) {
                listener.onDrag(getTop());
            }
        }
    }

    protected void dispatchExpandedCallback() {
        if (listeners != null && listeners.size() > 0) {
            for (Listener listener : listeners) {
                listener.onBottomSheetExpanded(getTop());
            }
        }
    }

    private boolean isDraggableViewUnder(View v, int x, int y) {
        return getVisibility() == VISIBLE && viewDragHelper.isViewUnder(v, x, y);
    }

    private ViewDragHelper.Callback dragHelperCallbacks = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // sebnap says: we don't intercept the touch if touch happens on scrolling child, therefore we can easily drag the dragview
            // if we have a scrolling child and it can scroll then don't drag, it'll be handled
            // by nested scrolling
//            boolean childCanScroll = scrollingChild != null  && (scrollingChild.canScrollVertically(1) || scrollingChild.canScrollVertically(-1));
//            return !childCanScroll;
            return dragView == child;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return Math.min(Math.max(top, dragViewTop), dragViewBottom);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return dragViewLeft;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return dragViewBottom - dragViewTop;
        }

        @Override
        public void onViewPositionChanged(View child, int left, int top, int dx, int dy) {
            dispatchDragCallback();
            mPeeked = false;
            dragViewOffsetHelper.setTopAndBottomOffset(top, false);
        }

        @Override
        public void onViewReleased(View releasedChild, float velocityX, float velocityY) {
            if (velocityY >= FLING_VELOCITY) {
                isDismissing = true;
                doDismiss();
            } else {
                // settle back into position
                isSettleing = true;
                viewDragHelper.settleCapturedViewAt(dragViewLeft, dragViewTop);
            }
            ViewCompat.postInvalidateOnAnimation(BottomSheet.this);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (isDismissing && state == ViewDragHelper.STATE_IDLE) {
                isDismissing = false;
                dispatchDismissCallback();
            }
            if (isSettleing && state == ViewDragHelper.STATE_IDLE) {
                isSettleing = false;
                dispatchExpandedCallback();
            }
        }
    };
}
