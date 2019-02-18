package com.exceptional.musiccore.engine;

/**
 * Created by darken on 02.05.2015.
 */
abstract class ATask {
    private final TT mType;

    enum TT {
        PREPARE, PLAY, PAUSE, STOP, RELEASE, WF_PAUSED, SEEK
    }

    public TT getType() {
        return mType;
    }

    public ATask(TT type) {
        mType = type;
    }

    static ATask seek(long pos) {
        return new Seek(pos);
    }

    public static class Seek extends ATask {
        private final long mSeekPosition;

        public Seek(long pos) {
            super(TT.SEEK);
            mSeekPosition = pos;
        }

        public long getSeekPosition() {
            return mSeekPosition;
        }
    }

    static Prepare prepare() {
        return new Prepare();
    }

    public static class Prepare extends ATask {

        public Prepare() {
            super(TT.PREPARE);
        }
    }

    static Play play() {
        return new Play();
    }

    public static class Play extends ATask {
        public Play() {
            super(TT.PLAY);
        }
    }

    static Pause pause() {
        return new Pause();
    }

    static class Pause extends ATask {
        public Pause() {
            super(TT.PAUSE);
        }
    }

    static Stop stop() {
        return new Stop();
    }

    static class Stop extends ATask {
        public Stop() {
            super(TT.STOP);
        }
    }

    static WaitForPaused waitForPaused() {
        return new WaitForPaused();
    }

    static class WaitForPaused extends ATask {
        public WaitForPaused() {
            super(TT.WF_PAUSED);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ATask))
            return false;

        ATask foo = (ATask) o;
        if (mType != foo.mType)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + mType.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return mType.toString();
    }
}
