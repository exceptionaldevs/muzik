package com.exceptional.musiccore.lfm.models.track;

import android.net.Uri;

import com.exceptional.musiccore.lfm.models.album.LFMBaseAlbum;
import com.exceptional.musiccore.lfm.models.album.LFMTrackAlbum;
import com.exceptional.musiccore.lfm.models.artist.LFMFakeArtist;
import com.exceptional.musiccore.lfm.models.artist.LFMTrackArtist;

public class LFMRealBaseTrack extends LFMBaseTrack {

    private static int density;
    String id;
    String duration;
    String listeners;
    String playCount;
    Wiki wiki;
    LFMTrackAlbum album;
    LFMTrackArtist artist;

    @Override
    public LFMFakeArtist getArtist() {
        return artist;
    }

    public Uri getImage() {
        if (album == null) {
            return null;
        }
        return null;
    }

    public Wiki getWiki() {
        return wiki;
    }

    @Override
    public LFMBaseAlbum getAlbum() {
        return album;
    }

    public class Wiki {
        public String published;
        public String summary;
        public String content;
    }

}
