package com.exceptional.musiccore.engine.metadata;

import android.net.Uri;

/**
 * Created by darken on 24.10.2015.
 * A basic library object
 */
public interface LibrarySource {
    /**
     * An unique identifier for this file within our app.
     *
     * @return A media store uri
     */
    Uri getLibrarySource();
}
