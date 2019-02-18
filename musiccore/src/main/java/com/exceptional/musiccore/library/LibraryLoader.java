package com.exceptional.musiccore.library;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by darken on 17.11.2015.
 */
public abstract class LibraryLoader<T> extends AsyncTaskLoader<T> {
    private final LoaderArgs mLoaderArgs;

    public LibraryLoader(Context context, LoaderArgs loaderArgs) {
        super(context);
        mLoaderArgs = loaderArgs;
    }

    public LoaderArgs getLoaderArgs() {
        return mLoaderArgs;
    }
}
