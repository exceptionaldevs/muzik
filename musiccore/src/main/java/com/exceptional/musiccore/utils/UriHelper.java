package com.exceptional.musiccore.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

/**
 * Created by darken on 13.12.2014.
 */
public class UriHelper {

    public static Uri drawableToUri(Context context, int drawableResId) {
        Resources resources = context.getResources();
        return Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        resources.getResourcePackageName(drawableResId) + '/' +
                        resources.getResourceTypeName(drawableResId) + '/' +
                        resources.getResourceEntryName(drawableResId));
    }

    public static Bundle uriToBundle(Uri uri) {
        Bundle b = new Bundle();
        b.putParcelable("generic.uri", uri);
        return b;
    }

    public static Uri uriFromBundle(Bundle b) {
        return b.getParcelable("generic.uri");
    }
}
