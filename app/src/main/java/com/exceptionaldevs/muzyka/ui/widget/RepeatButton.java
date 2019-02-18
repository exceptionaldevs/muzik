package com.exceptionaldevs.muzyka.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import com.exceptionaldevs.muzyka.R;

public class RepeatButton extends ImageButton {

    private static final int[] STATE_NONE = {R.attr.state_repeat_none};
    private static final int[] STATE_ONE = {R.attr.state_repeat_one};
    private static final int[] STATE_ALL = {R.attr.state_repeat_all};

    public enum RepeatState {
        NONE, ONE, ALL
    }

    RepeatState mRepeatState = RepeatState.NONE;

    public RepeatButton(Context context) {
        super(context);
    }

    public RepeatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RepeatButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RepeatState getState() {
        return mRepeatState;
    }

    public void setRepeatState(RepeatState repeatState) {
        mRepeatState = repeatState;
        refreshDrawableState();
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);

        if (mRepeatState == RepeatState.NONE) {
            mergeDrawableStates(drawableState, STATE_NONE);
        } else if (mRepeatState == RepeatState.ONE) {
            mergeDrawableStates(drawableState, STATE_ONE);
        } else if (mRepeatState == RepeatState.ALL) {
            mergeDrawableStates(drawableState, STATE_ALL);
        }

        return drawableState;
    }

    @Override
    public boolean performClick() {
        nextState();
        animateStateChange();
        return super.performClick();
    }

    private void nextState() {
        mRepeatState = getNextRepeatState();
        refreshDrawableState();
    }

    private void animateStateChange() {
        if (mRepeatState == RepeatState.ONE) {
            setRotationX(180);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            boolean flipped = false;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                RepeatButton.this.setRotationX(animation.getAnimatedFraction() * 180 + 180);
                if (!flipped && animation.getAnimatedFraction() > 0.5f) {
                    flipped = true;
                    refreshDrawableState();
                }
            }
        });

        animator.setInterpolator(AnimationUtils.loadInterpolator(
                getContext(),
                android.R.interpolator.fast_out_linear_in));
        animator.start();
    }

    private RepeatState getNextRepeatState() {
        if (mRepeatState == RepeatState.NONE) {
            return RepeatState.ONE;
        }
        if (mRepeatState == RepeatState.ONE) {
            return RepeatState.ALL;
        }
        if (mRepeatState == RepeatState.ALL) {
            return RepeatState.NONE;
        }
        return RepeatState.NONE;
    }
}