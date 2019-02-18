package com.exceptional.musiccore.engine.queue;

import android.content.Context;
import android.net.Uri;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.utils.Logy;
import com.yahoo.squidb.data.AbstractModel;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.data.SquidDatabase;
import com.yahoo.squidb.data.UriNotifier;
import com.yahoo.squidb.data.adapter.SQLiteDatabaseWrapper;
import com.yahoo.squidb.sql.Delete;
import com.yahoo.squidb.sql.Order;
import com.yahoo.squidb.sql.Query;
import com.yahoo.squidb.sql.SqlTable;
import com.yahoo.squidb.sql.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by darken on 29.06.2015.
 */
public class QueueDB extends SquidDatabase {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "QueueDB";
    private volatile static QueueDB sInstance;
    private List<Uri> mUriList;
    private final Context mContext;
    private static final int VERSION = 1;

    public static QueueDB getInstance(Context c) {
        if (sInstance == null) {
            synchronized (QueueDB.class) {
                if (sInstance == null)
                    sInstance = new QueueDB(c.getApplicationContext());
            }
        }
        return sInstance;
    }

    private final UriNotifier mUriNotifier = new UriNotifier(QueuedItem.TABLE) {

        @Override
        protected boolean accumulateNotificationObjects(Set<Uri> accumulatorSet, SqlTable<?> table, SquidDatabase database, DBOperation operation, AbstractModel modelValues, long rowId) {
            boolean accumulatorSetChanged = false;
            if (QueuedItem.TABLE.equals(table)) {
                accumulatorSetChanged |= accumulatorSet.add(QueuedItem.CONTENT_URI);
            }
            return accumulatorSetChanged;
        }
    };

    public QueueDB(Context context) {
        super(context);
        mContext = context;
        registerDataChangedNotifier(mUriNotifier);
    }

    private Context getContext() {
        return mContext;
    }

    public List<Uri> getQueue() {
        long start = System.currentTimeMillis();
        List<Uri> result = new ArrayList<>();
        SquidCursor<QueuedItem> cursor = buildQueueCursor();
        if (cursor.getCount() > 0) {
            final QueuedItem item = new QueuedItem();

            while (cursor.moveToNext()) {
                item.readPropertiesFromCursor(cursor);
                result.add(Uri.parse(item.getTrackUri()));
            }

        }
        cursor.close();

        long stop = System.currentTimeMillis() - start;
        Logy.v(TAG, "Loading " + result.size() + " took " + stop + "ms");
        return result;
    }

    public void setQueue(List<JXObject> queue) {
        long start = System.currentTimeMillis();
        clearQueue();
        Collections.reverse(queue);
        beginTransaction();
        for (JXObject obj : queue) {
            QueuedItem item = new QueuedItem();
            item.setTrackUri(obj.getLibrarySource().toString());
            persist(item);
        }
        setTransactionSuccessful();
        endTransaction();
        long stop = System.currentTimeMillis() - start;
        Logy.v(TAG, "Saving " + queue.size() + " took " + stop + "ms");
    }

    public void clearQueue() {
        Delete delete = Delete.from(QueuedItem.TABLE);
        delete(delete);
    }

    public SquidCursor<QueuedItem> buildQueueCursor() {
        Query query = Query
                .select(QueuedItem.TRACK_URI)
                .from(QueuedItem.TABLE)
                .orderBy(Order.desc(QueuedItem.ID));
        return query(QueuedItem.class, query);
    }


    @Override
    public String getName() {
        return "queue.db";
    }

    @Override
    protected int getVersion() {
        return VERSION;
    }

    @Override
    protected Table[] getTables() {
        return new Table[]{
                QueuedItem.TABLE,
        };
    }

    @Override
    protected boolean onUpgrade(SQLiteDatabaseWrapper db, int oldVersion, int newVersion) {
        return false;
    }


}


