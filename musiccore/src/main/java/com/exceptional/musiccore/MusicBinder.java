package com.exceptional.musiccore;

import android.os.Binder;
import android.support.annotation.NonNull;

import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayer;
import com.exceptional.musiccore.engine.audioeffects.AudioEffectCoordinator;
import com.exceptional.musiccore.engine.queue.Queue;
import com.exceptional.musiccore.engine.queuetasks.JXTasker;

import java.util.HashMap;

/**
 * Created by darken on 12.01.2016.
 */
public class MusicBinder extends Binder {
    public static final int TIMEKEEPER_THRESHOLD = 650;
    private final MusicService musicService;
    private final HashMap<String, Long> mTimeKeeper = new HashMap<>();

    public MusicBinder(MusicService musicService) {
        this.musicService = musicService;
    }

    protected MusicService getService() {
        return musicService;
    }

    public void togglePlayback() {
        if (getService().getPlayer().getState() == JXPlayer.PlayerState.PLAYING) {
            pause();
        } else {
            play();
        }
    }

    public void seek(int position) {
        getPlayer().seek(position);
    }

    public void seek(float fraction) {
        getPlayer().seek(fraction);
    }

    public void play() {
        this.play(false);
    }

    public void play(boolean force) {
        if (isAllowed(MusicService.ACTION_PLAYER_PLAY) || force) {
            getPlayer().play();
            logCurrentActionTime(MusicService.ACTION_PLAYER_PLAY);
        }
    }

    public void play(@NonNull JXObject object) {
        if (isAllowed(MusicService.ACTION_PLAYER_PLAY)) {
            getPlayer().play(object);
            logCurrentActionTime(MusicService.ACTION_PLAYER_PLAY);
        }
    }

    public void play(int position) {
        if (isAllowed(MusicService.ACTION_PLAYER_PLAY)) {
            getPlayer().play(position);
            logCurrentActionTime(MusicService.ACTION_PLAYER_PLAY);
        }
    }

    public void pause() {
        this.pause(false);
    }

    public void pause(boolean force) {
        if (isAllowed(MusicService.ACTION_PLAYER_PAUSE) || force) {
            getPlayer().pause();
            logCurrentActionTime(MusicService.ACTION_PLAYER_PAUSE);
        }
    }

    public void stop() {
        this.stop(false);
    }

    public void stop(boolean force) {
        if (isAllowed(MusicService.ACTION_PLAYER_STOP) || force) {
            getPlayer().stop();
            logCurrentActionTime(MusicService.ACTION_PLAYER_STOP);
        }
    }

    public void next() {
        if (isAllowed(MusicService.ACTION_PLAYER_NEXT)) {
            getPlayer().playNext();
            logCurrentActionTime(MusicService.ACTION_PLAYER_NEXT);
        }
    }

    public void previous() {
        if (isAllowed(MusicService.ACTION_PLAYER_PREVIOUS)) {
            getPlayer().playPrevious();
            logCurrentActionTime(MusicService.ACTION_PLAYER_PREVIOUS);
        }
    }

    public void favoriteOn() {
        if (isAllowed(MusicService.ACTION_FAVORITE_ON)) {
            getPlayer().favoriteOn();
            logCurrentActionTime(MusicService.ACTION_FAVORITE_ON);
        }
    }

    public void favoriteOff() {
        if (isAllowed(MusicService.ACTION_FAVORITE_OFF)) {
            getPlayer().favoriteOff();
            logCurrentActionTime(MusicService.ACTION_FAVORITE_OFF);
        }
    }

    private void logCurrentActionTime(String action) {
        mTimeKeeper.put(action, System.currentTimeMillis());
    }

    private boolean isAllowed(String action) {
        if (mTimeKeeper.containsKey(action)) {
            if (System.currentTimeMillis() < mTimeKeeper.get(action) + TIMEKEEPER_THRESHOLD) {
                return false; // new action happens before old action time + threshold
            }
        }
        return true;
    }

    public AudioEffectCoordinator getAudioEffectCoordinator() {
        return getService().getPlayer().getAudioEffectCoordinator();
    }

    public Queue getQueueHandler() {
        return getService().getPlayer().getQueue();
    }

    public void addPlayerStateListener(JXPlayer.PlayerStateListener listener) {
        getService().getPlayer().addPlayerStateListener(listener);
    }

    public void removePlayerStateListener(JXPlayer.PlayerStateListener listener) {
        getService().getPlayer().removePlayerStateListener(listener);
    }

    public void addPlayerProgressListener(JXPlayer.PlayerProgressListener listener) {
        getService().getPlayer().addPlayerProgressListener(listener);
    }

    public void removePlayerProgressListener(JXPlayer.PlayerProgressListener listener) {
        getService().getPlayer().removePlayerProgressListener(listener);
    }

    public JXPlayer getPlayer() {
        return getService().getPlayer();
    }

    public JXTasker getTasker() {
        return getService().getTasker();
    }

}
