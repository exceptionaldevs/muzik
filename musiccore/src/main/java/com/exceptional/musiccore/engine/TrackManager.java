package com.exceptional.musiccore.engine;

import android.content.Context;
import android.support.annotation.NonNull;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXAudioObject.AudioState;
import com.exceptional.musiccore.engine.audioeffects.AudioEffectCoordinator;
import com.exceptional.musiccore.utils.Logy;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TrackManager implements ActiveTrack.ActiveTrackCallback {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "Engine:TrackManager";
    private static final int ALLOWED_WORKERS = 3;
    private static final long PROGRESS_UPDATE_INTERVAL = 350;
    private final LinkedBlockingDeque<Runnable> mWorkerThreadQueue = new LinkedBlockingDeque<>();
    private final TrackManagerThreadFactory mThreadFactory = new TrackManagerThreadFactory();
    private final ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(ALLOWED_WORKERS, ALLOWED_WORKERS, Long.MAX_VALUE, TimeUnit.NANOSECONDS,
            mWorkerThreadQueue, mThreadFactory);
    private final AudioEffectCoordinator mAudioEffectCoordinator;
    private final Context mContext;
    private final TrackManagerCallback mCallbackToPlayer;
    private ActiveTrack mCurrent;
    private long mPreviousProgressUpdate;
    private final Object mControlLock = new Object();
    private boolean mStopped;

    public interface TrackManagerCallback {
        void onTrackStateChanged(JXObject jxObject, AudioState state);

        void onTrackProgressChange(JXObject jxObject, long currentPosition, long currentBufferPosition, long maxDuration, boolean isBusy);

        JXObject onGibNext(boolean select);
    }

    public TrackManager(Context context, TrackManagerCallback trackManagerCallback) {
        mContext = context;
        mAudioEffectCoordinator = new AudioEffectCoordinator(context);
        mCallbackToPlayer = trackManagerCallback;
    }

    public AudioEffectCoordinator getAudioEffectsCoordinator() {
        return mAudioEffectCoordinator;
    }

    public ActiveTrack getCurrentActiveTrack() {
        return mCurrent;
    }

    public JXObject getCurrentJXObject() {
        if (mCurrent == null)
            return null;
        return mCurrent.getJXObject();
    }

    public void play(@NonNull JXObject toPlay) {
        mStopped = false;
        synchronized (mControlLock) {
            if (mCurrent != null && mCurrent.getJXObject().equals(toPlay)) {
                // Target is already active, make sure it plays
                mCurrent.addTask(ATask.play());
                if (mCurrent.getState() == AudioState.COMPLETED || mCurrent.getState() == AudioState.PLAYING)
                    mCurrent.addTask(ATask.seek(0));
            } else {
                // No ActiveTrack found, it's a new track
                if (mCurrent != null)
                    mCurrent.addTask(ATask.stop());

                // Immediately play this track
                mCurrent = newActiveTrack(toPlay);
                mCurrent.addTask(ATask.play());
                mThreadPool.execute(mCurrent);
            }
        }
    }

    private ActiveTrack newActiveTrack(@NonNull JXObject nextTrack) {
        return new ActiveTrack(mContext, nextTrack, getAudioEffectsCoordinator(), this);
    }

    public void seek(long position) {
        if (mCurrent != null) {
            mCurrent.addTask(ATask.seek(position));
        }
    }

    public void pause() {
        if (mCurrent != null) {
            mCurrent.addTask(ATask.pause());
        }
    }

    public void stop() {
        if (mCurrent != null) {
            mCurrent.addTask(ATask.stop());
        }
        mStopped = true;
    }

    @Override
    public void onPlayingProgress(ActiveTrack activeTrack) {
        if (activeTrack == mCurrent) {
            long lastProgressUpdate = System.currentTimeMillis() - mPreviousProgressUpdate;
            if (lastProgressUpdate > PROGRESS_UPDATE_INTERVAL) {
                mCallbackToPlayer.onTrackProgressChange(activeTrack.getJXObject(), activeTrack.getCurrentPosition(), activeTrack.getCurrentBufferPosition(), activeTrack.getDuration(), activeTrack.isBusy());
                mPreviousProgressUpdate = System.currentTimeMillis();
            }
        } else {
            Logy.w(TAG, "Stale progress call from: " + activeTrack.getJXObject().getPlayableSource().getLastPathSegment());
        }

    }

    @Override
    public synchronized void onTrackStateChange(ActiveTrack activeTrack) {
        synchronized (mControlLock) {
            Logy.v(TAG, "onTrackStateChanged:" + activeTrack.getJXObject().getPlayableSource().getLastPathSegment() + " | " + activeTrack.getState().toString());
            if (activeTrack == mCurrent) {
                Logy.v(TAG, "Current:" + activeTrack.getJXObject().getPlayableSource().getLastPathSegment());
                if (activeTrack.getState() == AudioState.RELEASED)
                    mCurrent = null;
                mCallbackToPlayer.onTrackStateChanged(activeTrack.getJXObject(), activeTrack.getState());
                if (activeTrack.getState() == AudioState.COMPLETED || activeTrack.getState() == AudioState.RELEASED) {
                    if (mStopped) {
                        Logy.v(TAG, "stop() was called, not continuing!");
                        return;
                    }
                    Logy.v(TAG, "PROCEEDING!!:" + activeTrack.getJXObject().getPlayableSource().getLastPathSegment() + " | " + activeTrack.getState().toString());
                    final JXObject next = mCallbackToPlayer.onGibNext(true);
                    if (next != null) {
                        play(next);
                    } else {
                        stop();
                    }
                }

            }
        }
    }
}
