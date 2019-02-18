package com.exceptional.musiccore.lfm.models.album;

import com.exceptional.musiccore.lfm.models.LFMImage;

import java.util.List;

public class LFMTrackAlbum extends LFMBaseAlbum {
    String artist;
    String title;
    List<LFMImage> images;

    @Override
    public String getName() {
        return title;
    }

    public List<LFMImage> getImages() {
        return images;
    }
}
