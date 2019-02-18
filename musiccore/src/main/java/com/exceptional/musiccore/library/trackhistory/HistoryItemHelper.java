package com.exceptional.musiccore.library.trackhistory;

import android.content.Context;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayStatsDB;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.utils.Logy;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Delete;
import com.yahoo.squidb.sql.Order;
import com.yahoo.squidb.sql.Query;

import java.util.List;

/**
 * Created by darken on 29.06.2015.
 */
public class HistoryItemHelper {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "HistoryItemHelper";

    public static SquidCursor<HistoryItem> buildTrackHistoryCursor(Context context) {
        JXPlayStatsDB db = JXPlayStatsDB.getInstance(context);
        Query query = Query
                .select(HistoryItem.TRACK_ID, HistoryItem.TRACK_URI_STRING, HistoryItem.TIME_PLAYED)
                .from(HistoryItem.TABLE)
                .orderBy(Order.desc(HistoryItem.TIME_PLAYED));
        return db.query(HistoryItem.class, query);
    }

    public static void add(Context context, JXObject jxObject) {
        JXPlayStatsDB db = JXPlayStatsDB.getInstance(context);
        HistoryItem item = HistoryItem.from(jxObject);
        db.persist(item);
    }

    public static void add(Context context, List<Track> tracks) {
        JXPlayStatsDB db = JXPlayStatsDB.getInstance(context);
        long start = System.currentTimeMillis();
        db.beginTransaction();
        for (Track obj : tracks) {
            db.persist(HistoryItem.from(obj.getJXObject()));
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        long stop = System.currentTimeMillis() - start;
        Logy.v(TAG, "Adding " + tracks.size() + " took " + stop + "ms");
    }

    public static void clear(Context context) {
        JXPlayStatsDB db = JXPlayStatsDB.getInstance(context);
        Delete delete = Delete.from(HistoryItem.TABLE);
        db.delete(delete);
    }
}
