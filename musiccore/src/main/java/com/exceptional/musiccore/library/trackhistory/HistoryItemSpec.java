package com.exceptional.musiccore.library.trackhistory;

import android.net.Uri;

import com.exceptional.musiccore.engine.JXObject;
import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Created by darken on 27.06.2015.
 */
@TableModelSpec(className = "HistoryItem", tableName = "trackhistory")
public class HistoryItemSpec {
    public static final Uri CONTENT_URI = Uri.parse("content://com.apodamusic.player/trackhistory");
    public long trackId;
    public String trackUriString;
    public long timePlayed;

    //    @ModelMethod
    public static HistoryItem from(JXObject jxObject) {
        HistoryItem recentTrack = new HistoryItem();
        recentTrack.setTrackUriString(jxObject.getLibrarySource().toString());
        recentTrack.setTimePlayed(System.currentTimeMillis());
        recentTrack.setTrackId(Long.valueOf(jxObject.getLibrarySource().getLastPathSegment()));
        return recentTrack;
    }
}
