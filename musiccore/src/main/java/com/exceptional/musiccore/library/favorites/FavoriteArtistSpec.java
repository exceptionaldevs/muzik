package com.exceptional.musiccore.library.favorites;

import android.net.Uri;

import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Created by darken on 12.07.2015.
 */
@TableModelSpec(className = "FavoriteArtist", tableName = "favoriteartists")
public class FavoriteArtistSpec {
    public static final Uri CONTENT_URI = Uri.parse("content://com.apodamusic.player/favorites/artists");

    private String artistUriString;

    //    @ModelMethod
    public static FavoriteArtist from(Uri uri) {
        FavoriteArtist favorite = new FavoriteArtist();
        favorite.setArtistUriString(uri.toString());
        return favorite;
    }

}
