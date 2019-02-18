package com.exceptional.musiccore.engine;

public class PlayerOptions {
    private RepeatMode mRepeatMode = RepeatMode.NONE;
    private ShuffleMode mShuffleMode = ShuffleMode.NONE;

    public enum RepeatMode {
        NONE, TRACK, PLAYLIST
    }

    public enum ShuffleMode {
        NONE, RANDOM_TRACK
    }

    public RepeatMode getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(RepeatMode repeat) {
        this.mRepeatMode = repeat;
    }

    public ShuffleMode getShuffleMode() {
        return mShuffleMode;
    }

    public void setShuffleMode(ShuffleMode shuffle) {
        this.mShuffleMode = shuffle;
    }
}
