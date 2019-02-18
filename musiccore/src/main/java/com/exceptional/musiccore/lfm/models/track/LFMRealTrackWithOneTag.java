package com.exceptional.musiccore.lfm.models.track;


import com.exceptional.musiccore.lfm.models.LFMTag;

public class LFMRealTrackWithOneTag extends LFMRealBaseTrack {
    LFMTag toptags;
    String tagStrings;

    public String getToptags() {
        if (tagStrings == null) {
            tagStrings = toptags.getName();
        }
        return tagStrings;
    }
}
