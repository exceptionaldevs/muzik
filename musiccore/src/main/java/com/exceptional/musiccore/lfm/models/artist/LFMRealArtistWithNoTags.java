package com.exceptional.musiccore.lfm.models.artist;

public class LFMRealArtistWithNoTags extends LFMRealBaseArtist {
    @Override
    public String getTags() {
        return null;
    }
}
