package com.exceptional.musiccore.lfm.models.artist;

import com.google.gson.annotations.SerializedName;

public class LFMFakeArtist {
    @SerializedName("#text")
    public String text;
    public String mbid;

    public String getName() {
        return text;
    }

    public String getMbid() {
        return mbid;
    }

    public String getIdentifier() {
        if (mbid == null || mbid.length() == 0) {
            return text;
        } else {
            return mbid;
        }
    }
}
