package com.exceptional.musiccore.lfm.models.artist;

import com.exceptional.musiccore.lfm.models.LFMImage;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public abstract class LFMRealBaseArtist extends LFMTrackArtist {

    @SerializedName("image")
    List<LFMImage> images;

    //TODO: fallback if images to small or not available
    public LFMImage getBiggestImage() {
        for (int i = images.size() - 1; i >= 0; i--) {
            if (images.get(i).getSize().equals(LFMImage.SIZE_MEGA)) {
                return images.get(i);
            }
            if (images.get(i).getSize().equals(LFMImage.SIZE_EXTRALARGE)) {
                return images.get(i);
            }
            if (images.get(i).getSize().equals(LFMImage.SIZE_LARGE)) {
                return images.get(i);
            }
        }
        return null;
    }

    public abstract String getTags();


}
