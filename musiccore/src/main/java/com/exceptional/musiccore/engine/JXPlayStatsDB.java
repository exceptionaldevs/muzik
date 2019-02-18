package com.exceptional.musiccore.engine;

import android.content.Context;
import android.net.Uri;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.library.favorites.FavoriteTrack;
import com.exceptional.musiccore.library.trackhistory.HistoryItem;
import com.exceptional.musiccore.utils.Logy;
import com.yahoo.squidb.data.AbstractModel;
import com.yahoo.squidb.data.SquidDatabase;
import com.yahoo.squidb.data.UriNotifier;
import com.yahoo.squidb.data.adapter.SQLiteDatabaseWrapper;
import com.yahoo.squidb.sql.SqlTable;
import com.yahoo.squidb.sql.Table;

import java.util.Set;

/**
 * Created by darken on 29.06.2015.
 */

public class JXPlayStatsDB extends SquidDatabase {

    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "JXPlayStatsDB";
    private volatile static JXPlayStatsDB sInstance;

    public static JXPlayStatsDB getInstance(Context c) {
        if (sInstance == null) {
            synchronized (JXPlayStatsDB.class) {
                if (sInstance == null)
                    sInstance = new JXPlayStatsDB(c.getApplicationContext());
            }
        }
        return sInstance;
    }


    private static final int VERSION = 1;

    private JXPlayStatsDB(Context context) {
        super(context);
        registerDataChangedNotifier(mUriNotifier);
        Logy.i(TAG, "PlayStatsDB created.");
    }

    @Override
    public String getName() {
        return "jxdb.db";
    }


    private final UriNotifier mUriNotifier = new UriNotifier() {
        @Override
        protected boolean accumulateNotificationObjects(Set<Uri> accumulatorSet, SqlTable<?> table, SquidDatabase database, DBOperation operation, AbstractModel modelValues, long rowId) {
            boolean accumulatorSetChanged = false;
            if (HistoryItem.TABLE.equals(table)) {
                accumulatorSetChanged |= accumulatorSet.add(HistoryItem.CONTENT_URI);
            } else if (FavoriteTrack.TABLE.equals(table)) {
                accumulatorSetChanged |= accumulatorSet.add(FavoriteTrack.CONTENT_URI);
            }
            return accumulatorSetChanged;
        }
    };

    @Override
    protected int getVersion() {
        return VERSION;
    }

    @Override
    protected Table[] getTables() {
        return new Table[]{
                HistoryItem.TABLE,
                FavoriteTrack.TABLE
        };
    }

    @Override
    protected boolean onUpgrade(SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
        return false;
    }

}
