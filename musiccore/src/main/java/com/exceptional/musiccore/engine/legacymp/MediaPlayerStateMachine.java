package com.exceptional.musiccore.engine.legacymp;

import android.content.Context;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXAudioObject;
import com.exceptional.musiccore.utils.Logy;

public abstract class MediaPlayerStateMachine implements JXAudioObject {

    private AudioState mExternalState = AudioState.UNINITIALIZED;
    private MPSTATE mInternalState = MPSTATE.IDLE;
    private PlaybackStateListener mPlaybackListener;
    private ERROR mLastError = ERROR.NONE;
    private final Context mContext;

    // See statediagram:
    // http://developer.android.com/reference/android/media/MediaPlayer.html
    protected enum MPSTATE {
        IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED, COMPLETED, STOPPED, END, ERROR
    }

    public MediaPlayerStateMachine(Context c) {
        // Using an activity based context could create context leaks, ensure
        // that we only reference an application context
        this.mContext = c.getApplicationContext();
    }

    protected Context getContext() {
        return mContext;
    }

    @Override
    public synchronized AudioState getState() {
        return mExternalState;
    }

    protected MPSTATE getInternalState() {
        return mInternalState;
    }

    protected synchronized void updateInternalState(MPSTATE newInternalState) {
        this.mInternalState = newInternalState;

        if (mInternalState == MPSTATE.IDLE || mInternalState == MPSTATE.INITIALIZED || mInternalState == MPSTATE.STOPPED) {
            this.mExternalState = AudioState.UNINITIALIZED;
        } else if (mInternalState == MPSTATE.PREPARING) {
            this.mExternalState = AudioState.PREPARING;
        } else if (mInternalState == MPSTATE.PREPARED) {
            this.mExternalState = AudioState.PAUSED;
        } else if (mInternalState == MPSTATE.STARTED) {
            this.mExternalState = AudioState.PLAYING;
        } else if (mInternalState == MPSTATE.PAUSED) {
            this.mExternalState = AudioState.PAUSED;
        } else if (mInternalState == MPSTATE.COMPLETED) {
            this.mExternalState = AudioState.COMPLETED;
        } else if (mInternalState == MPSTATE.ERROR) {
            this.mExternalState = AudioState.ERROR;
        } else if (mInternalState == MPSTATE.END) {
            this.mExternalState = AudioState.RELEASED;
        }
        Logy.d(MusicCoreApplication.MUSICCORE_TAG_PREFIX + "Engine:MediaPlayerPlayer", getSource() + "||" + getState().name() + "(" + getInternalState().name() + ")");
        if (mPlaybackListener != null) {
            mPlaybackListener.onPlaybackStateChanged(mExternalState);
        }
    }

    @Override
    public final void setPlaybackStateListener(PlaybackStateListener listener) {
        this.mPlaybackListener = listener;
    }

    @Override
    public synchronized final void prepare() {
        if (getInternalState() == MPSTATE.END) {
            throw new RuntimeException("Can't prepare a released JXAudioObject");
        }
        if (getInternalState() == MPSTATE.IDLE || getInternalState() == MPSTATE.INITIALIZED || getInternalState() == MPSTATE.STOPPED) {
            updateInternalState(MPSTATE.PREPARING);
            doInit();
            doPrepare();
        }
    }

    protected abstract void doInit();

    protected abstract void doPrepare();

    @Override
    public synchronized final void play() {
        if (getInternalState() == MPSTATE.PREPARED || getInternalState() == MPSTATE.STARTED || getInternalState() == MPSTATE.PAUSED
                || getInternalState() == MPSTATE.COMPLETED) {
            onPlay();
            updateInternalState(MPSTATE.STARTED);
        }
    }

    protected abstract void onPlay();

    @Override
    public synchronized final void pause() {
        if (getInternalState() == MPSTATE.STARTED || getInternalState() == MPSTATE.PAUSED || getInternalState() == MPSTATE.COMPLETED) {
            doPause();
            updateInternalState(MPSTATE.PAUSED);
        }
    }

    protected abstract void doPause();

    @Override
    public synchronized final void seek(long position) {
        if (getInternalState() == MPSTATE.PREPARED || getInternalState() == MPSTATE.STARTED || getInternalState() == MPSTATE.PAUSED
                || getInternalState() == MPSTATE.COMPLETED) {
            doSeek(position);
        }
    }

    protected abstract void doSeek(long position);

    @Override
    public synchronized final void release() {
        if (getInternalState() != MPSTATE.IDLE && getInternalState() != MPSTATE.END) {
            doRelease();
            updateInternalState(MPSTATE.END);
        }
    }

    @Override
    public synchronized void stop() {
        if (getInternalState() == MPSTATE.PREPARED || getInternalState() == MPSTATE.STARTED || getInternalState() == MPSTATE.STOPPED
                || getInternalState() == MPSTATE.PAUSED || getInternalState() == MPSTATE.COMPLETED) {
            doStop();
        }
        if (getInternalState() == MPSTATE.INITIALIZED || getInternalState() == MPSTATE.PREPARED
                || getInternalState() == MPSTATE.STARTED || getInternalState() == MPSTATE.STOPPED || getInternalState() == MPSTATE.PAUSED
                || getInternalState() == MPSTATE.COMPLETED || getInternalState() == MPSTATE.ERROR) {
            doReset();
        }
    }

    protected abstract void doStop();

    protected abstract void doReset();

    protected abstract void doRelease();

    protected synchronized void setCompleted() {
        updateInternalState(MPSTATE.COMPLETED);
    }

    @Override
    public final JXAudioObject.ERROR getLastError() {
        return mLastError;
    }

    @Override
    public final long getCurrentPosition() {
        if (getInternalState() == MPSTATE.ERROR || getInternalState() == MPSTATE.END) {
            return -1;
        } else {
            return getCurrentPositionInternal();
        }
    }

    protected abstract long getCurrentPositionInternal();

    @Override
    public final long getDuration() {
        if (getInternalState() == MPSTATE.ERROR || getInternalState() == MPSTATE.INITIALIZED || getInternalState() == MPSTATE.IDLE
                || getInternalState() == MPSTATE.END) {
            return -1;
        } else {
            return getDurationInternal();
        }
    }

    protected abstract long getDurationInternal();

    @Override
    public final long getBufferPosition() {
        if (getInternalState() == MPSTATE.ERROR || getInternalState() == MPSTATE.INITIALIZED || getInternalState() == MPSTATE.IDLE
                || getInternalState() == MPSTATE.END) {
            return -1;
        } else {
            return getBufferPositionInternal();
        }
    }

    protected abstract long getBufferPositionInternal();

    @Override
    public final boolean isBusy() {
        return isBusyInternal();
    }

    protected abstract boolean isBusyInternal();

    protected synchronized void error(ERROR error) {
        mLastError = error;
        updateInternalState(MPSTATE.ERROR);
    }
}
