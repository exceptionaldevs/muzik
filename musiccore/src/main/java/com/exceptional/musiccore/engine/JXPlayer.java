package com.exceptional.musiccore.engine;

import android.content.Context;
import android.support.annotation.NonNull;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXAudioObject.AudioState;
import com.exceptional.musiccore.engine.TrackManager.TrackManagerCallback;
import com.exceptional.musiccore.engine.audioeffects.AudioEffectCoordinator;
import com.exceptional.musiccore.engine.queue.Queue;
import com.exceptional.musiccore.library.favorites.FavoritesHelper;
import com.exceptional.musiccore.library.trackhistory.HistoryItemHelper;
import com.exceptional.musiccore.utils.Logy;

import java.util.ArrayList;
import java.util.List;

public class JXPlayer implements TrackManagerCallback {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "JXPlayer";

    private final Context mContext;
    private final List<PlayerStateListener> mPlayerStateListeners = new ArrayList<>();
    private final List<PlayerProgressListener> mPlayerProgressListeners = new ArrayList<>();
    private final Queue mQueue;
    private PlayerState mPlayerState = PlayerState.STOPPED;
    private final TrackManager mTrackManager;
    private float mCurrentProgressPercentage;
    private long mPlayPosition;
    private long mPlayBuffer;
    private long mDuration;
    private boolean mTrackBusy;
    private JXObject mInterrimTrack;

    public enum PlayerState {
        STOPPED, PAUSED, PLAYING, PREPARING
    }

    public JXPlayer(Context context) {
        mContext = context.getApplicationContext();
        mQueue = new Queue(mContext);
        mQueue.loadState();
        mTrackManager = new TrackManager(mContext, this);
    }

    public Context getContext() {
        return mContext;
    }

    public JXObject getCurrentTrack() {
        return mTrackManager.getCurrentJXObject();
    }

    public PlayerState getState() {
        return mPlayerState;
    }

    public Queue getQueue() {
        return mQueue;
    }

    /**
     * This plays an independent item
     *
     * @param track a track object that may also be in the current play list
     */
    public void play(@NonNull JXObject track) {
        mQueue.setCurrentPlayingPosition(-1);
        playTrackManager(track);
    }

    public void play() {
        play(-1);
    }

    /**
     * Used to pause/resume a track and to play specific tracks out of the playlist.
     *
     * @param newPosition passing a -1 causes the player to act on the current track, if the current track is completed or paused it will be restarted or resumed. If
     *                    there currently is no track (e.g. fresh app start), it will try to get the next (first) track from the playlist.
     */
    public void play(int newPosition) {
        if (newPosition == -1) {
            JXObject newTrack = mTrackManager.getCurrentJXObject();
            // Play position was -1, if there is no current track try a new one at least
            if (newTrack == null)
                newTrack = mQueue.requestTrack(true);
            if (newTrack != null)
                playTrackManager(newTrack);
        } else {
            // Play specific track
            JXObject toPlay = mQueue.getAndSet(newPosition);
            ActiveTrack currentlyPlaying = mTrackManager.getCurrentActiveTrack();
            if (currentlyPlaying != null && currentlyPlaying.getJXObject().equals(toPlay) && currentlyPlaying.getState() == AudioState.PLAYING) {
                mTrackManager.seek(0);
            } else {
                playTrackManager(toPlay);
            }
        }
    }

    /**
     * Will select and play the next track out of the playlist. If the playlist is empty (e.g. cleared) the current track (if there is one) will be played from
     * start
     */
    public void playNext() {
        if (mQueue.getNext(false) != null) {
            JXObject next = mQueue.getNext(true);
            if (next != null)
                playTrackManager(next);
        } else {
            // Act on current track
            stop();
        }
    }

    /**
     * Analog to {@link #playNext()} it will play the previous track if the playlist is not empty, otherwise it will play the current track (if there is one)
     * from the start
     */
    public void playPrevious() {
        if (getState() == PlayerState.PLAYING) {
            if (mCurrentProgressPercentage > 0.10f || mQueue.size() == 0) {
                seek(0);
            } else if (mQueue.hasTracks()) {
                JXObject previous = mQueue.getPrevious(true);
                if (previous != null)
                    playTrackManager(previous);
            }
        } else {
            play();
        }
    }

    private void playTrackManager(@NonNull JXObject jxObject) {
        mInterrimTrack = jxObject;
        mTrackManager.play(jxObject);

    }

    public void seek(int position) {
        mTrackManager.seek(position);
    }

    public void seek(float fraction) {
        mTrackManager.seek((long) (fraction * mDuration));
    }

    public void pause() {
        mInterrimTrack = null;
        mTrackManager.pause();
    }

    public void stop() {
        mInterrimTrack = null;
        mTrackManager.stop();
    }

    /*
    convenience function for setting favorites
     */
    public void favoriteOn() {
        if (mTrackManager.getCurrentJXObject() == null) {
            Logy.w(TAG, "Tried to favorite null track");
            return;
        }
        FavoritesHelper.setFavoriteTrack(mContext, mTrackManager.getCurrentJXObject(), true);
        notifyPlayerStateListeners(mTrackManager.getCurrentJXObject(), mPlayerState);
    }

    /*
    convenience function for setting favorites
     */
    public void favoriteOff() {
        if (mTrackManager.getCurrentJXObject() == null) {
            Logy.w(TAG, "Tried to favorite null track");
            return;
        }
        FavoritesHelper.setFavoriteTrack(mContext, mTrackManager.getCurrentJXObject(), false);
        notifyPlayerStateListeners(mTrackManager.getCurrentJXObject(), mPlayerState);
    }

    @Override
    public JXObject onGibNext(boolean select) {
        return mQueue.requestTrack(select);
    }

    private Object mPreviousTSCObject = new Object();
    private Object mPreviousTSCState = new Object();

    @Override
    public void onTrackStateChanged(JXObject jxObject, JXAudioObject.AudioState state) {
        if (state == AudioState.UNINITIALIZED || state == AudioState.ERROR || state == AudioState.RELEASED) {
            mPlayerState = PlayerState.STOPPED;
            onTrackProgressChange(jxObject, -1, 0, 0, false);
        } else if (state == AudioState.PAUSED || state == AudioState.COMPLETED) {
            mPlayerState = PlayerState.PAUSED;
        } else if (state == AudioState.PREPARING) {
            mPlayerState = PlayerState.PREPARING;
        } else {
            mPlayerState = PlayerState.PLAYING;
            HistoryItemHelper.add(getContext(), jxObject);
        }

        boolean blockForSmoothTransition = (mPlayerState == PlayerState.PREPARING || mPlayerState == PlayerState.PAUSED) && mInterrimTrack == jxObject;
        Logy.v(TAG, "STATE int:" + state.name());
        if ((mPreviousTSCObject != jxObject || mPreviousTSCState != mPlayerState) && !blockForSmoothTransition) {
            notifyPlayerStateListeners(jxObject, mPlayerState);
            Logy.v(TAG, "STATE ext:" + mPlayerState.name());
        }

        if (mInterrimTrack == jxObject && state == AudioState.PLAYING)
            mInterrimTrack = null;

        mPreviousTSCObject = jxObject;
        mPreviousTSCState = mPlayerState;
    }

    private void notifyPlayerStateListeners(JXObject jxObject, PlayerState playerState) {
        for (PlayerStateListener pl : mPlayerStateListeners)
            pl.onNewPlayerState(jxObject, playerState);
    }

    @Override
    public void onTrackProgressChange(JXObject jxObject, long currentPosition, long currentBufferPosition, long maxDuration, boolean isBusy) {
        mPlayPosition = currentPosition;
        mPlayBuffer = currentBufferPosition;
        mDuration = maxDuration;
        mTrackBusy = isBusy;
        if (mDuration == 0)
            mCurrentProgressPercentage = -1;
        else
            mCurrentProgressPercentage = (float) mPlayPosition / mDuration;
//        Logy.v(TAG, "progress:" + mPlayPosition + " bufferProgress:" + mPlayBuffer + " max:" + mDuration + " the %:" + mCurrentProgressPercentage);
        for (PlayerProgressListener pl : mPlayerProgressListeners) {
            pl.onPlayerProgressChanged(jxObject, getQueue().getCurrentPosition(), mPlayPosition, mPlayBuffer, mDuration, mTrackBusy);
        }
    }

    public AudioEffectCoordinator getAudioEffectCoordinator() {
        return mTrackManager.getAudioEffectsCoordinator();
    }

    public void addPlayerStateListener(PlayerStateListener listener) {
        if (!mPlayerStateListeners.contains(listener))
            mPlayerStateListeners.add(listener);
        JXObject currentJXObject = mTrackManager.getCurrentJXObject();
        if (currentJXObject != null)
            listener.onNewPlayerState(currentJXObject, getState());
    }

    public void removePlayerStateListener(PlayerStateListener listener) {
        mPlayerStateListeners.remove(listener);
    }

    public interface PlayerStateListener {
        /**
         * @param jxObject may be NULL if the player was stopped
         * @param state    may be other, may be the same then UI should update info
         */
        // FIXME do we need to run this on an extra thread, as this could block the player
        void onNewPlayerState(JXObject jxObject, PlayerState state);
    }

    public interface PlayerProgressListener {
        /**
         * @param jxObject
         * @param playlistPosition can be -1 if the playing track is no longer in the playlist
         * @param playbackPosition -1 if no track is playing
         * @param bufferPosition   -1 if no track is playing
         * @param maxDuration      -1 if no track is playing
         * @param isBusy
         */
        // FIXME do we need to run this on an extra thread, as this could block the player
        void onPlayerProgressChanged(JXObject jxObject, int playlistPosition, long playbackPosition, long bufferPosition, long maxDuration, boolean isBusy);
    }

    public void addPlayerProgressListener(PlayerProgressListener listener) {
        if (!mPlayerProgressListeners.contains(listener))
            mPlayerProgressListeners.add(listener);
        JXObject obj = mTrackManager.getCurrentJXObject();
        if (obj != null) {
            listener.onPlayerProgressChanged(obj, getQueue().getCurrentPosition(), mPlayPosition, mPlayBuffer, mDuration, mTrackBusy);
        }
    }

    public void removePlayerProgressListener(PlayerProgressListener listener) {
        mPlayerProgressListeners.remove(listener);
    }

    public float getCurrentProgressPercentage() {
        return mCurrentProgressPercentage;
    }

    public long getDuration() {
        return mDuration;
    }

    public long getPlayPosition() {
        return mPlayPosition;
    }

    public long getPlayBuffer() {
        return mPlayBuffer;
    }
}
