package com.exceptionaldevs.muzyka.ui.widget.fortunewheel;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.ui.widget.CircleImageView;
import com.exceptionaldevs.muzyka.ui.widget.FABToggle;
import com.exceptionaldevs.muzyka.utils.AnimatorListener;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by sebnap on 28.01.16.
 * <p/>
 * {@link FortuneWheel}, make us rich!
 * <p/>
 * psst, let me tell you a secret, the first binded drawable always wins ;)
 */
public class FortuneWheel extends FrameLayout implements PieView.BindDrawablesCallback {
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    @BindView(R.id.pieView) PieView mPieView;
    @BindView(R.id.circleImageView) CircleImageView mWinnerView;
    @BindView(R.id.vDotsView) DotsView mDotsView;
    @BindView(R.id.vCircle) CircleView mCircle;
    @BindView(R.id.fab) FABToggle mFab;

    private AnimatorSet animatorSet;
    private FortuneWheelCallback mCallback;
    private Animator.AnimatorListener mDotsListener;
    private Animator.AnimatorListener mAllListener;

    public FortuneWheel(Context context) {
        super(context);
        init();
    }

    public FortuneWheel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FortuneWheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FortuneWheel(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.fortune_wheel, this, true);
        ButterKnife.bind(this);
    }

    public int getSize() {
        return mPieView.getSize();
    }

    public void setSize(int size) {
        mPieView.setSize(size);
        if (mCallback != null) {
            mCallback.onBindWinnerView(mWinnerView);
        }
    }

    /**
     * two different colors A and B in two similar shades
     * usually 1 will be lighter than the 2
     * <p/>
     * Dots will fade A1 to A2 and B1 to B2
     * Circle will fade A1 to B1
     *
     * @param colorA1
     * @param colorA2
     * @param colorB1
     * @param colorB2
     */
    public void setColors(int colorA1, int colorA2, int colorB1, int colorB2) {
        mDotsView.setColors(colorA1, colorA2, colorB1, colorB2);
        mCircle.setColors(colorA1, colorB1);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPieView.setCallback(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int size;
        if (widthMode == MeasureSpec.EXACTLY && widthSize > 0) {
            size = widthSize;
        } else if (heightMode == MeasureSpec.EXACTLY && heightSize > 0) {
            size = heightSize;
        } else {
            size = widthSize < heightSize ? widthSize : heightSize;
        }

        int finalMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);

        super.onMeasure(finalMeasureSpec, finalMeasureSpec);

        LayoutParams lp = new LayoutParams(mPieView.getRadius() * 2, mPieView.getRadius() * 2);
        lp.gravity = Gravity.CENTER;
        mWinnerView.setLayoutParams(lp);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mCallback != null) {
            mCallback.onBindWinnerView(mWinnerView);
        }
    }

    public void performAnimation() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }

        mPieView.setVisibility(VISIBLE);
        mPieView.setScaleX(1);
        mPieView.setScaleY(1);

        mFab.setVisibility(VISIBLE);
        mFab.setScaleX(1);
        mFab.setScaleY(1);

        ValueAnimator wheelAnimator = ValueAnimator.ofFloat(0, 360 * 9);
        wheelAnimator.setDuration(2000);
        wheelAnimator.addUpdateListener(new WheelAnimatorListener(mPieView, mFab));
        wheelAnimator.setInterpolator(new OvershootInterpolator(0.4f));

        ValueAnimator wheelScaleAnimator = ValueAnimator.ofFloat(0, 1);
        wheelScaleAnimator.addUpdateListener(new WheelScaleUpdateListener(mPieView, mFab));
        wheelScaleAnimator.setInterpolator(new AnticipateInterpolator(2));
        wheelScaleAnimator.setDuration(400);
        wheelScaleAnimator.addListener(new WheelScaleListener(mPieView, mFab));

        mWinnerView.animate().cancel();
        mWinnerView.setScaleX(0);
        mWinnerView.setScaleY(0);
        mWinnerView.setAlpha(1f);
        mCircle.setInnerCircleRadiusProgress(0);
        mCircle.setOuterCircleRadiusProgress(0);
        mDotsView.setCurrentProgress(0);

        animatorSet = new AnimatorSet();
        AnimatorSet subAnimatorSet = new AnimatorSet();

        ObjectAnimator outerCircleAnimator = ObjectAnimator.ofFloat(mCircle, CircleView.OUTER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        outerCircleAnimator.setDuration(250);
        outerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator innerCircleAnimator = ObjectAnimator.ofFloat(mCircle, CircleView.INNER_CIRCLE_RADIUS_PROGRESS, 0.1f, 1f);
        innerCircleAnimator.setDuration(200);
        innerCircleAnimator.setStartDelay(200);
        innerCircleAnimator.setInterpolator(DECCELERATE_INTERPOLATOR);

        ObjectAnimator starScaleYAnimator = ObjectAnimator.ofFloat(mWinnerView, ImageView.SCALE_Y, 0.2f, 1f);
        starScaleYAnimator.setDuration(350);
        starScaleYAnimator.setStartDelay(250);
        starScaleYAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator starScaleXAnimator = ObjectAnimator.ofFloat(mWinnerView, ImageView.SCALE_X, 0.2f, 1f);
        starScaleXAnimator.setDuration(350);
        starScaleXAnimator.setStartDelay(250);
        starScaleXAnimator.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator dotsAnimator = ObjectAnimator.ofFloat(mDotsView, DotsView.DOTS_PROGRESS, 0, 1f);
        dotsAnimator.setDuration(900);
        dotsAnimator.setStartDelay(50);
        dotsAnimator.setInterpolator(ACCELERATE_DECELERATE_INTERPOLATOR);

        if (mDotsListener != null) {
            dotsAnimator.addListener(mDotsListener);
        }

        subAnimatorSet.playTogether(
                outerCircleAnimator,
                innerCircleAnimator,
                starScaleYAnimator,
                starScaleXAnimator,
                dotsAnimator
        );

        subAnimatorSet.addListener(new AllAnimatorListener(mWinnerView, mCircle, mDotsView));

        animatorSet.playSequentially(wheelAnimator, wheelScaleAnimator, subAnimatorSet);

        if (mAllListener != null) {
            animatorSet.addListener(mAllListener);
        }

        animatorSet.start();
    }

    public boolean isAnimationRunning() {
        return animatorSet != null && animatorSet.isRunning();
    }

    public AnimatorSet getCurrentAnimation() {
        return animatorSet;
    }

    public FortuneWheelCallback getCallback() {
        return mCallback;
    }

    public void setCallback(FortuneWheelCallback callback) {
        mCallback = callback;
    }

    @Override
    public void onBindDrawable(PieView pieView, PiePieceDrawable drawable, int width, int height, int position) {
        if (mCallback != null) {
            mCallback.onBindDrawable(pieView, drawable, width, height, position);
        }
    }


    public interface FortuneWheelCallback {
        void onBindDrawable(PieView pieView, PiePieceDrawable drawable, int width, int height, int position);

        void onBindWinnerView(CircleImageView mWinnerView);
    }

    public Animator.AnimatorListener getDotsListener() {
        return mDotsListener;
    }

    public void setDotsListener(Animator.AnimatorListener dotsListener) {
        mDotsListener = dotsListener;
    }

    public Animator.AnimatorListener getAllListener() {
        return mAllListener;
    }

    public void setAllListener(Animator.AnimatorListener allListener) {
        mAllListener = allListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        animatorSet.cancel();
        animatorSet.removeAllListeners();
        super.onDetachedFromWindow();
    }

    private static class WheelAnimatorListener implements ValueAnimator.AnimatorUpdateListener {
        private WeakReference<View> mPieView;
        private WeakReference<View> mFab;

        public WheelAnimatorListener(View pieView, View fab) {
            mPieView = new WeakReference<>(pieView);
            mFab = new WeakReference<>(fab);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mPieView.get() != null && mFab.get() != null) {
                float dd = (float) animation.getAnimatedValue();
                mPieView.get().setRotation(dd);
                mFab.get().setRotation(dd);
            } else {
                animation.removeAllListeners();
            }
        }
    }

    private static class WheelScaleUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private WeakReference<View> mPieView;
        private WeakReference<View> mFab;

        public WheelScaleUpdateListener(View pieView, View fab) {
            mPieView = new WeakReference<>(pieView);
            mFab = new WeakReference<>(fab);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (mPieView.get() != null && mFab.get() != null) {
                mPieView.get().setScaleX(1 - animation.getAnimatedFraction());
                mPieView.get().setScaleY(1 - animation.getAnimatedFraction());
                mFab.get().setScaleX(1 - animation.getAnimatedFraction());
                mFab.get().setScaleY(1 - animation.getAnimatedFraction());
            } else {
                animation.removeAllListeners();
            }
        }
    }

    private static class WheelScaleListener extends AnimatorListener {
        private WeakReference<View> mPieView;
        private WeakReference<View> mFab;

        public WheelScaleListener(View pieView, View fab) {
            mPieView = new WeakReference<>(pieView);
            mFab = new WeakReference<>(fab);
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mPieView.get() != null && mFab.get() != null) {
                mPieView.get().setVisibility(INVISIBLE);
                mFab.get().setVisibility(INVISIBLE);
            } else {
                animation.removeAllListeners();
            }
        }
    }


    private static class AllAnimatorListener extends AnimatorListener {
        private WeakReference<View> mWinnerView;
        private WeakReference<CircleView> mCircle;
        private WeakReference<DotsView> mDots;


        public AllAnimatorListener(View winnerView, CircleView circle, DotsView dots) {
            mWinnerView = new WeakReference<>(winnerView);
            mCircle = new WeakReference<>(circle);
            mDots = new WeakReference<>(dots);
        }

        @Override
        public void onAnimationStart(Animator animation) {
            if (mWinnerView.get() != null && mCircle.get() != null && mDots.get() != null) {
                mWinnerView.get().setVisibility(VISIBLE);
            } else {
                animation.removeAllListeners();
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (mWinnerView.get() != null && mCircle.get() != null && mDots.get() != null) {
                mCircle.get().setInnerCircleRadiusProgress(0);
                mCircle.get().setOuterCircleRadiusProgress(0);
                mDots.get().setCurrentProgress(0);
                mWinnerView.get().setScaleX(1);
                mWinnerView.get().setScaleY(1);
            } else {
                animation.removeAllListeners();
            }
        }
    }
}
