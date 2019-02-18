package com.exceptional.musiccore.glide;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.concurrent.atomic.AtomicInteger;

public class FlagRequestListener implements RequestListener {
    public static class Flags {
        AtomicInteger flags = new AtomicInteger();

        synchronized void setFlagTrue(int flag) {
            flags.set(flags.get() | flag);
        }

        synchronized boolean checkFlags(int allFlags) {
            return flags.get() == allFlags;
        }
    }

    private final Flags mFlags;
    private final int mMyFlag;
    private final int mAllFlags;
    private final Runnable mRunnable;

    public FlagRequestListener(Flags flags, int myFlag, int allFlags, Runnable runnable) {
        mFlags = flags;
        mMyFlag = myFlag;
        mAllFlags = allFlags;
        mRunnable = runnable;
    }

    @Override
    public boolean onLoadFailed(GlideException e, Object model, Target target, boolean isFirstResource) {
        mFlags.setFlagTrue(mMyFlag);
        if (mRunnable != null && mFlags.checkFlags(mAllFlags)) {
            //every Request is ready
            mRunnable.run();
        }
        return false;
    }

    @Override
    public boolean onResourceReady(Object resource, Object model, Target target, DataSource dataSource, boolean isFirstResource) {
        mFlags.setFlagTrue(mMyFlag);
        if (mRunnable != null && mFlags.checkFlags(mAllFlags)) {
            //every Request is ready
            mRunnable.run();
        }
        return false;
    }
}