package com.exceptional.musiccore.lfm.models.track;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.exceptional.musiccore.lfm.models.LFMImage;
import com.exceptional.musiccore.lfm.models.album.LFMBaseAlbum;
import com.exceptional.musiccore.lfm.models.artist.LFMFakeArtist;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;

public class LFMTrackListTrack extends LFMBaseTrack {

    private static int density;
    LFMBaseAlbum album;
    LFMFakeArtist artist;
    ArrayList<LFMImage> image;
    Date date;
    @SerializedName("@attr")
    @Nullable
    IsNowPlaying isNowPlaying;

    @Override
    public LFMFakeArtist getArtist() {
        return artist;
    }

    public Date getDateTime() {
        if (isNowPlaying != null && isNowPlaying.nowPlaying != null && isNowPlaying.nowPlaying.equals("true")) {
            return new Date();
        }
        return date;
    }

    public Uri getImage() {
        return null;
    }

    @Override
    public LFMBaseAlbum getAlbum() {
        return album;
    }

    public static class BaseTrackList extends ArrayList<LFMTrackListTrack> {
    }

    private class IsNowPlaying {
        @SerializedName("nowplaying")
        @Nullable
        String nowPlaying;
    }
}
