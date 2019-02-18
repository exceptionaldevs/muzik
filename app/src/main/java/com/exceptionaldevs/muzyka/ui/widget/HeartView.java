package com.exceptionaldevs.muzyka.ui.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.ui.widget.shadowciv.ShadowCircleImageView;

import java.lang.ref.WeakReference;

/**
 * Created by sebnap on 23.06.15.
 */
public class HeartView extends FrameLayout {

    private final ShadowCircleImageView mCoverView;
    private final PlayPauseButton mFabView;
    private Rotation mRotation;
    private boolean mIsPlaying;

    public HeartView(Context context) {
        this(context, null);
    }

    public HeartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HeartView, defStyleAttr, 0);
        int coverDiameter = a.getDimensionPixelSize(R.styleable.HeartView_coverdiameter, (int) getResources().getDimension(R.dimen.heartview_diameter));
        a.recycle();


        mCoverView = new ShadowCircleImageView(context);
        LayoutParams lp = new LayoutParams(coverDiameter, coverDiameter, Gravity.CENTER);
        mCoverView.setLayoutParams(lp);

        addView(mCoverView);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.heartview_heart_fab, this);
        mFabView = (PlayPauseButton) findViewById(R.id.heartview_heart_fab);

        mRotation = new Rotation(mCoverView);

        setPadding(50, 50, 50, 50);
        setClipToPadding(false);
    }

    public ShadowCircleImageView getCoverView() {
        return mCoverView;
    }

    public PlayPauseButton getPlayPauseFab() {
        return mFabView;
    }

    public void startRotation(int rotationCount) {
        mRotation.start(rotationCount);
    }

    public void setCoverRotation(float rotation) {
        mRotation.setRotation(rotation);
    }

    public void setPlaying(boolean playing) {
        mIsPlaying = playing;
        if(mIsPlaying != mRotation.isRunning()) {
            startRotation(mIsPlaying ? -1 : 0);
        }
    }

    public void pauseRotation() {
        mRotation.pause();
    }

    public void playRotation() {
        mRotation.start();
    }



    private static final class Rotation {
        private static final int DELAY = 0;
        private static final int ROTATION_PER_SEC = 6; // 360° / 60s

        private static final byte STOPPED = 0x0;
        private static final byte STARTING = 0x1;
        private static final byte RUNNING = 0x2;
        private static final byte PAUSED = 0x3;

        private final WeakReference<View> mView;
        private final Choreographer mChoreographer;

        private byte mStatus = STOPPED;
        private final float mRotationPerSecond;
        private int mRepeatLimit;

        private float mRotation;
        private long mLastAnimationMs;

        Rotation(View v) {
            mRotationPerSecond = ROTATION_PER_SEC;
            mView = new WeakReference<View>(v);
            mChoreographer = Choreographer.getInstance();
        }

        private Choreographer.FrameCallback mTickCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                tick(frameTimeNanos);
            }
        };

        private Choreographer.FrameCallback mStartCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                mStatus = RUNNING;
                mLastAnimationMs = (long) (frameTimeNanos / 1000000f);
                tick(frameTimeNanos);
            }
        };

        private Choreographer.FrameCallback mRestartCallback = new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                if (mStatus == RUNNING) {
                    if (mRepeatLimit >= 0) {
                        mRepeatLimit--;
                    }
                    start(mRepeatLimit);
                }
            }
        };

        void tick(long frameTimeNanos) {
            if (mStatus != RUNNING) {
                return;
            }

            mChoreographer.removeFrameCallback(mTickCallback);

            final View view = mView.get();
            if (view != null) {
                long currentMs = (long) (frameTimeNanos / 1000000f);
                long deltaMs = currentMs - mLastAnimationMs;
                mLastAnimationMs = currentMs;
                float deltaRot = deltaMs / 1000f * mRotationPerSecond;
                mRotation += deltaRot;
                if (mRotation >= 360) {
                    mRotation = 0;
                    mChoreographer.postFrameCallbackDelayed(mRestartCallback, DELAY);
                } else {
                    mChoreographer.postFrameCallback(mTickCallback);
                }
                doRotate();
            }
        }

        void pause() {
            mStatus = PAUSED;
            mChoreographer.removeFrameCallback(mStartCallback);
            mChoreographer.removeFrameCallback(mRestartCallback);
            mChoreographer.removeFrameCallback(mTickCallback);
        }

        void stop() {
            mStatus = STOPPED;
            mChoreographer.removeFrameCallback(mStartCallback);
            mChoreographer.removeFrameCallback(mRestartCallback);
            mChoreographer.removeFrameCallback(mTickCallback);
            resetRotation();
        }

        private void resetRotation() {
            mRotation = 0.0f;
            doRotate();
        }

        void start(int repeatLimit) {
            if (repeatLimit == 0) {
                stop();
                return;
            }

            mRepeatLimit = repeatLimit;
            final View view = mView.get();
            if (view != null) {
                if (mStatus != PAUSED) {
                    mRotation = 0.0f;
                }
                mStatus = STARTING;
                doRotate();
                mChoreographer.postFrameCallback(mStartCallback);
            }
        }

        void start() {
            start(mRepeatLimit);
        }

        float getRotation() {
            return mRotation;
        }

        public void setRotation(float rotation) {
            mRotation = rotation;
            doRotate();
        }

        private void doRotate() {
            final View view = mView.get();
            if (view != null)
                view.setRotation(mRotation);
        }

        boolean isRunning() {
            return mStatus == RUNNING;
        }

        boolean isStopped() {
            return mStatus == STOPPED;
        }

        boolean isPaused() {
            return mStatus == PAUSED;
        }

    }

    public void startPauseRotation(boolean start) {
        if (mRotation != null && mIsPlaying) {
            if (start) {
                mRotation.start();
            } else {
                mRotation.pause();
            }
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        startPauseRotation(focused);
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        startPauseRotation(hasWindowFocus);
    }

}
