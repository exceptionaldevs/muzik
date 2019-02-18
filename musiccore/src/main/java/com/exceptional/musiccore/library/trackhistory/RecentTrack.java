package com.exceptional.musiccore.library.trackhistory;

import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.library.tracks.Track;

/**
 * Created by darken on 11.07.2015.
 */
public class RecentTrack extends Track {
    private final long mLastPlayed;

    public RecentTrack(JXObject jxObject, long lastPlayed) {
        super(jxObject);
        mLastPlayed = lastPlayed;
    }

    public long getLastPlayed() {
        return mLastPlayed;
    }

}