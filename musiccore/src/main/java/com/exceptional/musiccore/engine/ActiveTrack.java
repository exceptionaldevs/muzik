package com.exceptional.musiccore.engine;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.audioeffects.AudioEffectCoordinator;
import com.exceptional.musiccore.utils.Logy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by darken on 20.03.14.
 */
public class ActiveTrack implements Runnable, JXAudioObject.PlaybackStateListener {
    private static final int PRIORITY_PLAYING = 1;
    private static final int PRIORITY_PREPARING = 0;
    private final JXObject mJXObject;
    private final LinkedBlockingDeque<ATask> mTasks = new LinkedBlockingDeque<>();
    private final AudioEffectCoordinator mAudioEffectCoordinator;
    private final ActiveTrackCallback mActiveTrackCallback;
    private final Context mContext;
    private String TAG;
    private volatile JXAudioObject mJXAudioObject;
    private Integer mPriority = null;
    private volatile long mLastCycle;
    private volatile boolean mRunning = true;
    private long mCurrentPosition;
    private long mCurrentBufferPosition;
    private long mDuration;
    private boolean mIsBusy;
    private volatile JXAudioObject.AudioState mState = JXAudioObject.AudioState.UNINITIALIZED;
    private Handler mPlayerHandler;
    private HandlerThread mHandlerThread;

    protected ActiveTrack(Context context, JXObject jxObject, AudioEffectCoordinator audioEffectCoordinator, ActiveTrackCallback activeTrackCallback) {
        TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "Engine:ActiveTrack:" + jxObject.getPlayableSource().getLastPathSegment();
        mContext = context.getApplicationContext();
        this.mJXObject = jxObject;
        this.mAudioEffectCoordinator = audioEffectCoordinator;
        this.mActiveTrackCallback = activeTrackCallback;
    }

    interface ActiveTrackCallback {
        void onPlayingProgress(ActiveTrack activeTrack);

        void onTrackStateChange(ActiveTrack activeTrack);
    }

    private static JXAudioObject instantiateAudioObject(Context context, JXObject jxObject) {
        JXAudioObject result;
        try {
            if (jxObject.getSourceResolver() == null) {
                Constructor<? extends JXAudioObject> constructor = jxObject.getAudioObjectClass().getConstructor(Context.class, Uri.class);
                result = constructor.newInstance(context, jxObject.getPlayableSource());
            } else {
                Constructor<? extends JXAudioObject> constructor = jxObject.getAudioObjectClass().getConstructor(Context.class, Uri.class, SourceResolver.class);
                result = constructor.newInstance(context, jxObject.getPlayableSource(), jxObject.getSourceResolver());
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException("This shouldn't happen");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("This shouldn't happen");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("This shouldn't happen");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("This shouldn't happen");
        }
        return result;
    }

    public JXObject getJXObject() {
        return mJXObject;
    }

    public void addTask(ATask task) {
        if (mState == JXAudioObject.AudioState.RELEASED) {
            Logy.e(TAG, "In RELEASED state, no longer accepting tasks:" + task);
            return;
        }
        synchronized (mTasks) {
            mTasks.add(task);
        }
    }

    public Integer getPriority() {
        return mPriority;
    }

    public void setPriority(Integer priority) {
        mPriority = priority;
    }

    public JXAudioObject.AudioState getState() {
        return mState;
    }

    @Override
    public void run() {
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mPlayerHandler = new Handler(mHandlerThread.getLooper());
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                mJXAudioObject = instantiateAudioObject(mContext, mJXObject);
                mJXAudioObject.setPlaybackStateListener(ActiveTrack.this);
                Logy.v(TAG, "AudioObject instantiated.");
            }
        });

        int tid = android.os.Process.myTid();
        while (mRunning) {
            if (mJXAudioObject == null)
                continue;

            if (mPriority != null) {
                int was = android.os.Process.getThreadPriority(tid);
                if (getPriority() == PRIORITY_PLAYING && was != android.os.Process.THREAD_PRIORITY_AUDIO) {
                    android.os.Process.setThreadPriority(tid, android.os.Process.THREAD_PRIORITY_AUDIO);
                    Logy.v(TAG, "Priority is now PLAYING");
                } else if (getPriority() == PRIORITY_PREPARING && was != android.os.Process.THREAD_PRIORITY_BACKGROUND) {
                    android.os.Process.setThreadPriority(tid, android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    Logy.v(TAG, "Priority is now PREPARING");
                }
            }
            ATask atHand;
            synchronized (mTasks) {
                if (mState == JXAudioObject.AudioState.RELEASED) {
                    mTasks.clear();
                    continue;
                } else if (mTasks.contains(ATask.stop()) || mState == JXAudioObject.AudioState.ERROR) {
                    atHand = ATask.stop();
                    mTasks.clear();
                } else {
                    atHand = mTasks.poll();
                }
            }
            if (atHand == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Logy.v(TAG, "Process task:" + atHand.toString());
                // TODO can we really only wait for PAUSED in the state PREPARING?
                if (atHand.getType() == ATask.TT.WF_PAUSED && mState != JXAudioObject.AudioState.PAUSED) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mTasks.addFirst(ATask.waitForPaused());
                    continue;
                } else if (atHand.getType() == ATask.TT.STOP) {
                    mTasks.clear();
                    mJXAudioObject.stop();
                    mAudioEffectCoordinator.releaseSession(mJXAudioObject.getAudioSessionId());
                    mJXAudioObject.release();
                } else if (atHand.getType() == ATask.TT.PREPARE) {
                    mJXAudioObject.prepare();
                    mTasks.addFirst(ATask.waitForPaused());
                } else if (atHand.getType() == ATask.TT.PLAY) {
                    if (mState == JXAudioObject.AudioState.PLAYING || mState == JXAudioObject.AudioState.ERROR)
                        continue;
                    if (mState == JXAudioObject.AudioState.UNINITIALIZED) {
                        mTasks.addFirst(ATask.play());
                        mTasks.addFirst(ATask.waitForPaused());
                        mTasks.addFirst(ATask.prepare());
                        continue;
                    }
                    if (mState == JXAudioObject.AudioState.COMPLETED) {
                        mJXAudioObject.seek(0);
                    }
                    mJXAudioObject.play();
                    try {
                        mAudioEffectCoordinator.addSession(mJXAudioObject.getAudioSessionId());
                    } catch (Exception e) {
                        e.printStackTrace();
                        mTasks.add(ATask.stop());
                    }
                } else if (atHand.getType() == ATask.TT.PAUSE) {
                    mJXAudioObject.pause();
                } else if (atHand.getType() == ATask.TT.SEEK) {
                    ATask.Seek seek = (ATask.Seek) atHand;
                    mJXAudioObject.seek(seek.getSeekPosition());
                }
            }

            updateProgress();
            long lastCycle = (System.currentTimeMillis() - mLastCycle);
            if (lastCycle > 100) {
                mPlayerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mActiveTrackCallback.onPlayingProgress(ActiveTrack.this);
                    }
                });
                mLastCycle = System.currentTimeMillis();
            }
        }
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                mHandlerThread.quit();
                Logy.i(TAG, "Player handler quit.");
            }
        });
    }

    private void updateProgress() {
        JXAudioObject.AudioState state = mJXAudioObject.getState();
        if (state == JXAudioObject.AudioState.PLAYING || state == JXAudioObject.AudioState.PAUSED || state == JXAudioObject.AudioState.COMPLETED) {
            mCurrentPosition = mJXAudioObject.getCurrentPosition();
            mCurrentBufferPosition = mJXAudioObject.getBufferPosition();
            mDuration = mJXAudioObject.getDuration();
            mIsBusy = mJXAudioObject.isBusy();
        } else {
            mCurrentPosition = -1;
            mCurrentBufferPosition = -1;
            mDuration = -1;
            mIsBusy = false;
        }
    }

    public long getCurrentPosition() {
        return mCurrentPosition;
    }

    public long getCurrentBufferPosition() {
        return mCurrentBufferPosition;
    }

    public long getDuration() {
        return mDuration;
    }

    public boolean isBusy() {
        return mIsBusy;
    }

    @Override
    public void onPlaybackStateChanged(JXAudioObject.AudioState state) {
        Logy.v(TAG, "onPlaybackStateChanged:" + state);
        if (state == JXAudioObject.AudioState.RELEASED) {
            Logy.d(TAG, "Released");
            mRunning = false;
        }
        mState = state;
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                mActiveTrackCallback.onTrackStateChange(ActiveTrack.this);
            }
        });
    }
}
