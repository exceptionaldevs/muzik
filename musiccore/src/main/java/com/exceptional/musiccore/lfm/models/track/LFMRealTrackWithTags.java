package com.exceptional.musiccore.lfm.models.track;


import com.exceptional.musiccore.lfm.models.LFMTag;

import java.io.StringWriter;
import java.util.List;

public class LFMRealTrackWithTags extends LFMRealBaseTrack {

    String tagStrings;
    TopTags toptags;

    public String getToptags() {
        if (tagStrings == null) {
            StringWriter stringWriter = new StringWriter();
            for (LFMTag tag : toptags.tag) {
                stringWriter.append(tag.getName());
                stringWriter.append(", ");
            }
            tagStrings = stringWriter.toString();
        }
        return tagStrings;
    }

    public class TopTags {
        List<LFMTag> tag;
    }
}
