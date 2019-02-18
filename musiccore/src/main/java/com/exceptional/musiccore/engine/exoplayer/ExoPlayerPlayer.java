package com.exceptional.musiccore.engine.exoplayer;

import android.content.Context;
import android.net.Uri;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXAudioObject;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.utils.Logy;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.Util;


/**
 * Created by darken on 20.11.2014.
 */
public class ExoPlayerPlayer implements JXAudioObject, ExoPlayer.Listener {
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;
    private String TAG;
    private ExoPlayer mPlayer;
    private final Uri mSource;
    private final CustomAudioTrackRenderer mAudioTrackRenderer;
    private final Context mContext;

    private AudioState mExternalState = AudioState.UNINITIALIZED;

    private static final JXObject.ResourceType[] mSupportedTypes = new JXObject.ResourceType[]{
            JXObject.ResourceType._3GP,
            JXObject.ResourceType.MP4,
            JXObject.ResourceType.M4A,
            JXObject.ResourceType.MP3,
            JXObject.ResourceType.OGG,
            JXObject.ResourceType.WAV,
    };

    private PlaybackStateListener mPlaybackStateListener;
    private ERROR mLastError = ERROR.NONE;

    public static boolean isSupported(JXObject.ResourceType lookUpType) {
        for (JXObject.ResourceType supportedType : mSupportedTypes) {
            if (supportedType == lookUpType)
                return true;
        }
        return false;
    }

    public ExoPlayerPlayer(Context context, Uri uri) {
        TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "Engine:ExoPlayerPlayer:" + uri.getLastPathSegment();
        Logy.d(TAG, "ExoPlayer created:" + uri);
        mContext = context;
        mSource = uri;
        mPlayer = ExoPlayer.Factory.newInstance(1);

        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        DataSource dataSource = new DefaultUriDataSource(context, Util.getUserAgent(context, "Chroma Music"));
        SampleSource samplesource = new ExtractorSampleSource(uri, dataSource, allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);
        mAudioTrackRenderer = new CustomAudioTrackRenderer(samplesource);
        mPlayer.addListener(this);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public Uri getSource() {
        return mSource;
    }

    @Override
    public AudioState getState() {
        return mExternalState;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public void prepare() {
        mPlayer.prepare(mAudioTrackRenderer);
    }

    @Override
    public void play() {
        mPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        mPlayer.setPlayWhenReady(false);
    }

    @Override
    public void seek(long position) {
        mPlayer.seekTo(position);
    }

    @Override
    public void stop() {
        mPlayer.stop();
    }

    @Override
    public void release() {
        mPlayer.removeListener(this);
        mPlayer.release();
        mPlayer = null;
        updateExternalState(AudioState.RELEASED);
    }

    @Override
    public ERROR getLastError() {
        return mLastError;
    }

    @Override
    public long getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public long getBufferPosition() {
        return mPlayer.getBufferedPosition();
    }

    @Override
    public boolean isBusy() {
        return mPlayer.getPlaybackState() == ExoPlayer.STATE_PREPARING || mPlayer.getPlaybackState() == ExoPlayer.STATE_BUFFERING;
    }

    @Override
    public void setPlaybackStateListener(PlaybackStateListener listener) {
        mPlaybackStateListener = listener;
    }

    @Override
    public int getAudioSessionId() {
        return mAudioTrackRenderer.getAudioSessionId();
    }

    private int mPreviousPlaybackState = ExoPlayer.STATE_IDLE;
    private boolean mPreviousPlayWhenReady = false;

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_IDLE:
                updateExternalState(AudioState.UNINITIALIZED);
                break;
            case ExoPlayer.STATE_PREPARING:
                updateExternalState(AudioState.PREPARING);
                break;
            case ExoPlayer.STATE_BUFFERING:
                updateExternalState(AudioState.PREPARING);
                break;
            case ExoPlayer.STATE_READY:
                if (playWhenReady) {
                    updateExternalState(AudioState.PLAYING);
                } else {
                    updateExternalState(AudioState.PAUSED);
                }
                break;
            case ExoPlayer.STATE_ENDED:
                if (mPreviousPlaybackState == ExoPlayer.STATE_PREPARING && mPlayer.getDuration() == 0) {
                    updateExternalState(AudioState.ERROR);
                } else {
                    updateExternalState(AudioState.COMPLETED);
                }
                break;
            default:
                throw new RuntimeException("Unknown ExoPlayer state");
        }
        if (mPreviousPlayWhenReady == playWhenReady && mPreviousPlaybackState == playbackState) {
            updateExternalState(AudioState.ERROR);
        }
        mPreviousPlayWhenReady = playWhenReady;
        mPreviousPlaybackState = playbackState;
    }

    @Override
    public void onPlayWhenReadyCommitted() {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        error.printStackTrace();
        mLastError = ERROR.UNKNOWN;
        updateExternalState(AudioState.ERROR);
        Logy.e(TAG, "ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
        Logy.e(TAG, "ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
        Logy.e(TAG, "ERRRRRRRRRRR  RRRRRRRRRRRRRR  RRRRRRRRRRR");
        Logy.e(TAG, "ERRRRRRRRRR     RRRRRRRRRR§     RRRRRRRRR");
        Logy.e(TAG, "ERRRRRRRRRRR   RRRRRRRRRRRRR   RRRRRRRRRR");
        Logy.e(TAG, "ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
        Logy.e(TAG, "ERRRRRRRRRRRR               RRRRRRRRRRRRR");
        Logy.e(TAG, "ERRRRRRRRRR  RRRRRRRRRRRRRR  RRRRRRRRRRRR");
        Logy.e(TAG, "ERRRRRRRR  RRRRRRRRRRRRRRRRRR  RRRRRRRRRR");
        Logy.e(TAG, "ERRRRRRR  RRRRRRRRRRRRRRRRRRRR  RRRRRR§RR");
        Logy.e(TAG, "ERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
    }

    private void updateExternalState(AudioState externalState) {
        Logy.d(TAG, "State:" + externalState.name());
        mExternalState = externalState;
        if (mPlaybackStateListener != null)
            mPlaybackStateListener.onPlaybackStateChanged(getState());
    }
}
