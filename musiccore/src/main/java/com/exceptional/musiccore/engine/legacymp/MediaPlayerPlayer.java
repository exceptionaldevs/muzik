package com.exceptional.musiccore.engine.legacymp;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.SourceResolver;
import com.exceptional.musiccore.utils.Logy;

public class MediaPlayerPlayer extends MediaPlayerStateMachine {
    private String TAG;
    private MediaPlayer mPlayer;
    private final Uri mSource;
    private boolean mSeeking = false;
    private boolean mBuffering = false;
    private boolean mRemote = false;
    private final SourceResolver mSourceResolver;

    private static final JXObject.ResourceType[] mSupportedTypes = new JXObject.ResourceType[]{
            JXObject.ResourceType._3GP,
            JXObject.ResourceType.MP4,
            JXObject.ResourceType.M4A,
            JXObject.ResourceType.FLAC,
            JXObject.ResourceType.MP3,
            JXObject.ResourceType.OGG,
            JXObject.ResourceType.WAV,
    };

    public static boolean isSupported(JXObject.ResourceType lookUpType) {
        for (JXObject.ResourceType supportedType : mSupportedTypes) {
            if (supportedType == lookUpType)
                return true;
        }
        return false;
    }

    public MediaPlayerPlayer(Context context, Uri resource) {
        this(context, resource, null);
    }

    public MediaPlayerPlayer(Context context, Uri resource, SourceResolver resolver) {
        super(context);
        this.mSource = resource;
        this.mSourceResolver = resolver;
        TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "Engine:MediaPlayer:" + resource.getLastPathSegment();
        Logy.d(TAG, "MediaPlayer created:" + resource);
    }

    @Override
    public Uri getSource() {
        return mSource;
    }

    @Override
    protected void doInit() {
        mPlayer = new MediaPlayer();
        mPlayer.setScreenOnWhilePlaying(false);
        try {
            Uri realSource = getSource();
            if (mSourceResolver != null)
                realSource = mSourceResolver.resolve(getSource());
            mPlayer.setDataSource(getContext(), realSource);
        } catch (Exception e) {
            e.printStackTrace();
            Logy.e(TAG, "Unsupported source:" + getSource());
            error(ERROR.UNKNOWN);
            return;
        }
        mPlayer.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Logy.e(TAG, "ERROR what:" + what + " extra:" + extra + " item:" + mSource.toString());
                error(ERROR.UNKNOWN);
                return false;
            }
        });
        mPlayer.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                setCompleted();
            }
        });
        mPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                Logy.d(TAG, "buffer%:" + percent);
            }
        });
        mPlayer.setOnInfoListener(new OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Logy.d(TAG, "infoCode:" + what + " extra:" + extra);
                if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    mBuffering = true;
                } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    mBuffering = false;
                }
                return true;
            }
        });
        updateInternalState(MPSTATE.INITIALIZED);
    }

    @Override
    protected void doPrepare() {
        try {
            mPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            error(ERROR.UNKNOWN);
            return;
        }
        // STOP does not necessarily reset the position
        mPlayer.seekTo(0);
        updateInternalState(MPSTATE.PREPARED);
    }

    @Override
    protected void onPlay() {
        // http://stackoverflow.com/questions/19916293/mediaplayer-streaming-issues-on-android-4-4-api-19
        mPlayer.start();
    }

    @Override
    protected void doSeek(long position) {
        mPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                mSeeking = false;
            }
        });
        mSeeking = true;
        mPlayer.seekTo((int) position);
    }

    @Override
    protected void doPause() {
        mPlayer.pause();
    }

    @Override
    protected void doStop() {
        mPlayer.stop();
    }

    @Override
    protected void doReset() {
        mPlayer.reset();
    }

    @Override
    protected void doRelease() {
        mPlayer.release();
        mPlayer = null;
    }

    @Override
    protected long getCurrentPositionInternal() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    protected long getDurationInternal() {
        return mPlayer.getDuration();
    }

    @Override
    protected long getBufferPositionInternal() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    protected boolean isBusyInternal() {
        return mBuffering || mSeeking;
    }

    @Override
    public boolean isRemote() {
        return mRemote;
    }

    @Override
    public int getAudioSessionId() {
        if (mPlayer == null) {
            return -1;
        } else {
            return mPlayer.getAudioSessionId();
        }
    }

}
