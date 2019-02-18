package com.exceptional.musiccore.lfm.models;

import com.exceptional.musiccore.lfm.models.track.LFMTrackListTrack;
import com.google.gson.annotations.SerializedName;

public class LFMTrackHistoryPage {

    @SerializedName("@attr")
    LFMAttr attr;
    LFMTrackListTrack.BaseTrackList track;

    public LFMAttr getAttr() {
        return attr;
    }

    public LFMTrackListTrack.BaseTrackList getTrack() {
        return track;
    }
}
