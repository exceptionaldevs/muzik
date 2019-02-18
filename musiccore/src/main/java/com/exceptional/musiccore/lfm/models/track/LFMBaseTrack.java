package com.exceptional.musiccore.lfm.models.track;

import android.net.Uri;

import com.exceptional.musiccore.lfm.models.album.LFMBaseAlbum;
import com.exceptional.musiccore.lfm.models.artist.LFMFakeArtist;

abstract class LFMBaseTrack {

    String name;
    String mbid;
    String url;

    public abstract LFMFakeArtist getArtist();

    public abstract Uri getImage();

    public abstract LFMBaseAlbum getAlbum();

    public String getName() {
        return name;
    }

    public String getMbid() {
        return mbid;
    }

    public Uri getUrl() {
        return Uri.parse(url);
    }

    @Override
    public String toString() {
        return getName();
    }
}
