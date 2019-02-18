package com.exceptionaldevs.muzyka.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import com.exceptionaldevs.muzyka.R;

public class ShuffleButton extends ImageButton {

    private static final int[] STATE_ON = {R.attr.state_shuffle_on};
    private static final int[] STATE_OFF = {R.attr.state_shuffle_off};

    public enum ShuffleState {
        ON, OFF
    }

    ShuffleState mShuffleState = ShuffleState.OFF;

    public ShuffleButton(Context context) {
        super(context);
    }

    public ShuffleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShuffleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ShuffleState getState() {
        return mShuffleState;
    }

    public void setShuffleState(ShuffleState shuffleState) {
        mShuffleState = shuffleState;
        refreshDrawableState();
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);

        if (mShuffleState == ShuffleState.OFF) {
            mergeDrawableStates(drawableState, STATE_OFF);
        } else if (mShuffleState == ShuffleState.ON) {
            mergeDrawableStates(drawableState, STATE_ON);
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
        mShuffleState = getNextShuffleState();
    }

    private void animateStateChange() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            boolean flipped = false;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ShuffleButton.this.setRotationX(animation.getAnimatedFraction() * 180);
                if (animation.getAnimatedFraction() > 0.5f && !flipped) {
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

    private ShuffleState getNextShuffleState(){
        if (mShuffleState == ShuffleState.OFF) {
            return ShuffleState.ON;
        }
        if (mShuffleState == ShuffleState.ON) {
            return ShuffleState.OFF;
        }
        return ShuffleState.OFF;
    }
}