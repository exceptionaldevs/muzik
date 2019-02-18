package com.exceptional.musiccore.library.favorites;

import android.net.Uri;

import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Created by darken on 12.07.2015.
 */
@TableModelSpec(className = "FavoritePlaylist", tableName = "favoriteplaylists")
public class FavoritePlaylistSpec {
    public static final Uri CONTENT_URI = Uri.parse("content://com.apodamusic.player/favorites/playlists");

    private String playlistUriString;

    //    @ModelMethod
    public static FavoritePlaylist from(Uri uri) {
        FavoritePlaylist favorite = new FavoritePlaylist();
        favorite.setPlaylistUriString(uri.toString());
        return favorite;
    }

}
