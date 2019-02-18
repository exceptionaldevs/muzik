package com.exceptional.musiccore.engine;

import android.net.Uri;

public interface SourceResolver {
    public Uri resolve(Uri input);
}
