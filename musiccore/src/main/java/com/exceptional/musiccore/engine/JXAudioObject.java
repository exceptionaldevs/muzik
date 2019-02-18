package com.exceptional.musiccore.engine;

import android.net.Uri;

public interface JXAudioObject {
    /**
     * This will be what we publicly use in other classes.
     * Any actual playback implementation whether it is a FFMpeg JNI wrapper or the native
     * MediaPlayer class should be hidden behind this interface
     *
     * @author darken
     */
    interface PlaybackStateListener {
        void onPlaybackStateChanged(AudioState state);
    }

    enum AudioState {
        UNINITIALIZED, // After object creation or after calling stop()
        PREPARING, // UNINITIALIZED->PREPARING->PAUSED
        PAUSED, // state after preparing or when paused
        PLAYING, // you should be able to hear music
        COMPLETED, // track finished (playback completed)
        RELEASED, // after release, this AudioObject can no longer be used
        ERROR // error situation, release+prepare again
    }

    enum ERROR {
        UNKNOWN, NONE
    }

    Uri getSource();

    AudioState getState();

    /**
     * Wether this Player is playing a remote object such as a stream via network
     *
     * @return true if it's not a local file
     */
    boolean isRemote();

    void prepare();

    void play();

    void pause();

    void seek(long position);

    void stop();

    void release();

    ERROR getLastError();

    long getCurrentPosition();

    long getDuration();

    long getBufferPosition();

    boolean isBusy();

    void setPlaybackStateListener(PlaybackStateListener listener);

    int getAudioSessionId();

}
