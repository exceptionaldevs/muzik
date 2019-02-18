package com.exceptionaldevs.muzyka.settings.tabs;

import android.support.annotation.StringRes;

import com.exceptionaldevs.muzyka.R;

/**
 * Created by darken on 22.01.2016.
 */
public enum TabIdentifier {
    ARTISTS(R.string.artists, R.string.artists_caption),
    ALBUMS(R.string.albums, R.string.albums_caption),
    TRACKS(R.string.tracks, R.string.tracks_caption),
    PLAYLISTS(R.string.playlists, R.string.playlists_caption);
//    FAVORITES("Favorites", "Your favorites!"),
//    HISTORY("History", "What did you listen to?")

    public final int titleRes;
    public final int summaryRes;

    TabIdentifier(@StringRes int titleRes, @StringRes int summaryRes) {
        this.titleRes = titleRes;
        this.summaryRes = summaryRes;
    }
}
