package com.exceptionaldevs.muzyka.ui.widget;

import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.ui.widget.shadowciv.ShadowCircleImageView;
import com.wnafee.vector.MorphButton;

/**
 * Created by sebnap on 14.01.16.
 */
public class Miniplayer extends FrameLayout implements View.OnClickListener, View.OnTouchListener {
    private static final String TAG = "Miniplayer";
    private MorphButton mForwardView;
    private MorphButton mRewindView;
    private RepeatButton mRepeatView;
    private ShuffleButton mShuffleView;
    private TextView mTextView;
    private OnClickPlayButtons mClickListener;
    private OnTouchPlayButtons mTouchListener;
    private Vibrator mVibrator;
    private boolean mUseVibrating;
    private SeekArc mSeekArc;
    private HeartView mHeartView;
    private PlayPauseButton mPlayView;
    private boolean isFortuneMode;

    public void switchToFortuneWheelIcon(boolean fortuneIcon) {
        if(fortuneIcon){
            isFortuneMode = true;
            mPlayView.setImageDrawable(getResources().getDrawable(R.drawable.fortunewheel));
            mPlayView.invalidate();
        }else{
            isFortuneMode = false;
            mPlayView.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_24dp));
            mPlayView.invalidate();
        }
    }

    public boolean isFortuneMode() {
        return isFortuneMode;
    }

    private enum ButtonEvent {
        PLAY,
        PAUSE,
        FORWARD,
        REWIND,
        REPEAT_ONE,
        REPEAT_ALL,
        REPEAT_NONE,
        SHUFFLE_ON,
        SHUFFLE_OFF,
        STOP
    }

    public interface OnClickPlayButtons {
        public void onClickPlay(View v);

        public void onClickFortune(View v);

        public void onClickForward(View v);

        public void onClickRewind(View v);

        public void onClickRepeat(View v, RepeatButton.RepeatState rs);

        public void onClickShuffle(View v, ShuffleButton.ShuffleState ss);
    }

    public interface OnTouchPlayButtons {
        public boolean onTouchPlay(View v, MotionEvent event);

        public boolean onTouchFortune(View v, MotionEvent event);

        public boolean onTouchForward(View v, MotionEvent event);

        public boolean onTouchRewind(View v, MotionEvent event);

        public boolean onTouchRepeat(View v, RepeatButton.RepeatState rs, MotionEvent event);

        public boolean onTouchShuffle(View v, ShuffleButton.ShuffleState ss, MotionEvent event);
    }

    public Miniplayer(Context context) {
        this(context, null);
    }

    public Miniplayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Miniplayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void marqueeText(boolean start) {
        // for marquee
        if (mTextView != null)
            mTextView.setSelected(start);
    }

    public RepeatButton.RepeatState getRepeatState() {
        return mRepeatView.getState();
    }

    public void setRepeatState(RepeatButton.RepeatState repeatState) {
        mRepeatView.setRepeatState(repeatState);
    }

    public ShuffleButton.ShuffleState getShuffleState() {
        return mShuffleView.getState();
    }

    public void setShuffleState(ShuffleButton.ShuffleState shuffleState) {
        mShuffleView.setShuffleState(shuffleState);
    }

    public void setPlayState(PlayPauseButton.PlayState playState) {
        mPlayView.setPlayState(playState);
    }

    public PlayPauseButton.PlayState getPlayState() {
        return mPlayView.getPlayState();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mForwardView = (MorphButton) findViewById(R.id.nextBtn);
        mRewindView = (MorphButton) findViewById(R.id.rewindBtn);

        mRepeatView = (RepeatButton) findViewById(R.id.repeatBtn);
        mShuffleView = (ShuffleButton) findViewById(R.id.shuffleBtn);

        mTextView = (TextView) findViewById(R.id.playerText);

        mSeekArc = (SeekArc) findViewById(R.id.progressBar);
        mHeartView = (HeartView) findViewById(R.id.heartview);

        mPlayView = mHeartView.getPlayPauseFab();

        mForwardView.setOnClickListener(this);
        mRewindView.setOnClickListener(this);
        mRepeatView.setOnClickListener(this);
        mShuffleView.setOnClickListener(this);
        mPlayView.setOnClickListener(this);

        mForwardView.setOnTouchListener(this);
        mRewindView.setOnTouchListener(this);
        mRepeatView.setOnTouchListener(this);
        mShuffleView.setOnTouchListener(this);
        mPlayView.setOnTouchListener(this);
    }

    public ShadowCircleImageView getCoverView() {
        return mHeartView.getCoverView();
    }

    public HeartView getHeartView() {
        return mHeartView;
    }

    public SeekArc getSeekArc() {
        return mSeekArc;
    }

    public OnClickPlayButtons getClickListener() {
        return mClickListener;
    }

    public void setClickListener(OnClickPlayButtons clickListener) {
        this.mClickListener = clickListener;
    }

    public OnTouchPlayButtons getTouchListener() {
        return mTouchListener;
    }

    public void setTouchListener(OnTouchPlayButtons touchListener) {
        mTouchListener = touchListener;
    }

    public void setOnSeekArcChangeListener(SeekArc.OnSeekArcChangeListener l) {
        mSeekArc.setOnSeekArcChangeListener(l);
    }

    public void setProgress(long progress) {
        mSeekArc.setProgress(progress);
    }

    public long getProgress() {
        return mSeekArc.getProgress();
    }

    public void setMax(long mMax) {
        mSeekArc.setMax(mMax);
    }

    public long getMax() {
        return mSeekArc.getMax();
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null) {
            if (v == mPlayView) {
                if(isFortuneMode()) {
                    mClickListener.onClickFortune(v);
                    Log.v(TAG, "onClickFortune");
                }else{
                    mClickListener.onClickPlay(v);
                    Log.v(TAG, "onClickPlay");
                }
            } else if (v == mForwardView) {
                mClickListener.onClickForward(v);
                Log.v(TAG, "onClickForward");
            } else if (v == mRewindView) {
                mClickListener.onClickRewind(v);
                Log.v(TAG, "onClickRewind");
            } else if (v == mRepeatView) {
                mClickListener.onClickRepeat(v, mRepeatView.getState());
                Log.v(TAG, "onClickRepeat");
            } else if (v == mShuffleView) {
                mClickListener.onClickShuffle(v, mShuffleView.getState());
                Log.v(TAG, "onClickShuffle");
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mTouchListener != null) {
            if (v == mPlayView) {
                if(isFortuneMode()) {
                    mTouchListener.onTouchFortune(v, event);
                    Log.v(TAG, "onTouchFortune");
                }else{
                    mTouchListener.onTouchPlay(v, event);
                    Log.v(TAG, "onTouchPlay");
                }
            } else if (v == mForwardView) {
                return mTouchListener.onTouchForward(v, event);
            } else if (v == mRewindView) {
                return mTouchListener.onTouchRewind(v, event);
            } else if (v == mRepeatView) {
                return mTouchListener.onTouchRepeat(v, mRepeatView.getState(), event);
            } else if (v == mShuffleView) {
                return mTouchListener.onTouchShuffle(v, mShuffleView.getState(), event);
            }
        }
        return false;
    }

    public void setVibrator(Vibrator vibrator, boolean useVibrating) {
        mVibrator = vibrator;
        mUseVibrating = useVibrating;
    }

    private boolean shouldVibrate() {
        return mVibrator != null && mUseVibrating;
    }

    private void vibrateOn(ButtonEvent event) {
        if (shouldVibrate()) {
            switch (event) {
                case PLAY:
                    mVibrator.vibrate(25);
                    break;
                case PAUSE:
                    mVibrator.vibrate(50);
                    break;
                case FORWARD:
                    mVibrator.vibrate(25);
                    break;
                case REWIND:
                    mVibrator.vibrate(25);
                    break;
//                case REPEAT_ONE:
//                case REPEAT_ALL:
//                case REPEAT_NONE:
//                case SHUFFLE_ON:
//                case SHUFFLE_OFF:
//                case STOP:
            }
        }
    }

    public void setTextFields(String artist, String title, String album) {
        StringBuilder builder = new StringBuilder();
        builder.append(title);
        builder.append(" - ");
        builder.append(artist);
        if (mTextView != null)
            mTextView.setText(builder.toString());
    }

}
