package com.exceptional.musiccore.library.favorites;

import android.net.Uri;

import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Created by darken on 27.06.2015.
 */
@TableModelSpec(className = "FavoriteTrack", tableName = "favoritetracks")
public class FavoriteTrackSpec {
    public static final Uri CONTENT_URI = Uri.parse("content://com.apodamusic.player/favorites/tracks");

    private String trackUriString;

    //    @ModelMethod
    public static FavoriteTrack from(Uri uri) {
        FavoriteTrack favorite = new FavoriteTrack();
        favorite.setTrackUriString(uri.toString());
        return favorite;
    }

}
