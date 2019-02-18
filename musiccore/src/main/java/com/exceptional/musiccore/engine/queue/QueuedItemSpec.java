package com.exceptional.musiccore.engine.queue;

import android.net.Uri;

import com.yahoo.squidb.annotations.TableModelSpec;

/**
 * Created by darken on 27.06.2015.
 */
@TableModelSpec(className = "QueuedItem", tableName = "queue")
public class QueuedItemSpec {
    public static final Uri CONTENT_URI = Uri.parse("content://com.apodamusic.player/queue");
    public String trackUri;
}
