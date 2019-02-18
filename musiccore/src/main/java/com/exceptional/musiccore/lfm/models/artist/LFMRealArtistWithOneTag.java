package com.exceptional.musiccore.lfm.models.artist;


import com.exceptional.musiccore.lfm.models.LFMTag;

public class LFMRealArtistWithOneTag extends LFMRealBaseArtist {
    LFMTag tags;

    @Override
    public String getTags() {
        return tags.getName();
    }
}
