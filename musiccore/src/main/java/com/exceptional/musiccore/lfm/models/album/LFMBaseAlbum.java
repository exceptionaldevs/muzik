package com.exceptional.musiccore.lfm.models.album;

import com.google.gson.annotations.SerializedName;

public class LFMBaseAlbum {
    @SerializedName("#text")
    String name;
    String mbid;

    public String getName() {
        return name;
    }

    public String getMbid() {
        return mbid;
    }
}
