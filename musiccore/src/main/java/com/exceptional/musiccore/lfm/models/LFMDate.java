package com.exceptional.musiccore.lfm.models;

import java.util.Date;

public class LFMDate {
    public static final long MILLIS_PER_SEC = 1000L;
    String uts;
    long time;

    public Date getDate() {
        if (time == 0L) {
            time = Long.parseLong(uts) * MILLIS_PER_SEC;
        }
        return new Date(Long.parseLong(uts) * MILLIS_PER_SEC);
    }
}
