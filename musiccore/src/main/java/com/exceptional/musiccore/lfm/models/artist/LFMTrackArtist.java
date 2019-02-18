package com.exceptional.musiccore.lfm.models.artist;

public class LFMTrackArtist extends LFMFakeArtist {

    String name;
    String url;

    @Override
    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getIdentifier() {
        if (mbid != null && !(mbid.length() == 0)) {
            return mbid;
        } else if (name != null && !(name.length() == 0)) {
            return name;
        }
        return super.getIdentifier();
    }
}
