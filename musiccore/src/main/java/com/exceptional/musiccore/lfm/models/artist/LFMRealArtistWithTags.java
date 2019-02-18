package com.exceptional.musiccore.lfm.models.artist;


import com.exceptional.musiccore.lfm.models.LFMTag;

import java.io.StringWriter;
import java.util.List;

public class LFMRealArtistWithTags extends LFMRealBaseArtist {
    Tags tags;
    String tagStrings;

    @Override
    public String getTags() {
        if (tagStrings == null) {
            StringWriter stringWriter = new StringWriter();
            for (LFMTag tag : tags.tag) {
                stringWriter.append(tag.getName());
                stringWriter.append(", ");
            }
            tagStrings = stringWriter.toString();
        }
        return tagStrings;
    }

    class Tags {
        List<LFMTag> tag;
    }
}
