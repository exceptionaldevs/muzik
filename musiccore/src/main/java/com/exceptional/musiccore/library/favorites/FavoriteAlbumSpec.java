package com.exceptional.musiccore.library.favorites;

import android.net.Uri;

import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Created by darken on 12.07.2015.
 */
@TableModelSpec(className = "FavoriteAlbum", tableName = "favoritealbums")
public class FavoriteAlbumSpec {
    public static final Uri CONTENT_URI = Uri.parse("content://com.apodamusic.player/favorites/albums");

    private String albumUriString;

    //    @ModelMethod
    public static FavoriteAlbum from(Uri uri) {
        FavoriteAlbum favorite = new FavoriteAlbum();
        favorite.setAlbumUriString(uri.toString());
        return favorite;
    }

}
