package com.exceptional.musiccore.library.trackhistory;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXObjectFactory;
import com.exceptional.musiccore.utils.Logy;
import com.yahoo.squidb.data.SquidCursor;

import java.util.ArrayList;
import java.util.List;

public class TrackHistoryLoader extends AsyncTaskLoader<List<RecentTrack>> {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "TrackHistoryLoader";
    private List<RecentTrack> mData;
    private SquidCursor<HistoryItem> mCursor;
    private final ContentObserver mContentObserver = new ForceLoadContentObserver();

    public TrackHistoryLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(List<RecentTrack> data) {
        Logy.d(TAG, "deliverResult(...)");
        if (isReset()) {
            //No no no, bad Android, keep your race conditions
            if (mData != null)
                onReleaseResources(mData);
        }
        List<RecentTrack> oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null)
            onReleaseResources(oldData);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            getContext().getContentResolver().registerContentObserver(HistoryItem.CONTENT_URI, false, mContentObserver);
        }
        if (takeContentChanged() || mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Logy.d(TAG, "onStopLoading()");
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        getContext().getContentResolver().unregisterContentObserver(mContentObserver);
        cancelLoad();
        super.onStopLoading();
    }

    @Override
    public void onCanceled(List<RecentTrack> data) {
        Logy.d(TAG, "onCanceled(...)");
        onReleaseResources(data);
        super.onCanceled(data);
    }

    @Override
    protected void onReset() {
        Logy.d(TAG, "onReset()");
        onStopLoading();

        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }
        super.onReset();
    }

    protected void onReleaseResources(List<RecentTrack> data) {
        Logy.d(TAG, "onReleaseResources(...)");

        if (data != null && !data.isEmpty())
            data.clear();
    }

    @Override
    public void onContentChanged() {
        Logy.d(TAG, "onContentChanged()");
        super.onContentChanged();
    }

    @Override
    public List<RecentTrack> loadInBackground() {
        long dur = System.currentTimeMillis();
        Logy.d(TAG, "loadInBackground()");
        List<RecentTrack> result = new ArrayList<>();

        if (mCursor == null) {
            mCursor = HistoryItemHelper.buildTrackHistoryCursor(getContext());
        } else {
            mCursor.requery();
        }
        if (mCursor.getCount() > 0) {
            final HistoryItem historyItem = new HistoryItem();
            JXObjectFactory jxObjectFactory = new JXObjectFactory(getContext());
            List<Uri> uris = new ArrayList<>();
            List<Long> playedTimeList = new ArrayList<>();
            while (mCursor.moveToNext()) {
                historyItem.readPropertiesFromCursor(mCursor);
                uris.add(Uri.parse(historyItem.getTrackUriString()));
                playedTimeList.add(historyItem.getTimePlayed());
            }
            for (JXObject jxObject : jxObjectFactory.make(uris)) {
                // If the factory doesn't resolve an URI, it is no issue.
                // All items for the same URI will fail to resolve and thus no shift or
                // wrong offset happens in regard to the playedTimeList entries.
                int timePos = uris.indexOf(jxObject.getLibrarySource());
                long time = 0;
                if (timePos != -1) {
                    uris.remove(timePos);
                    time = playedTimeList.remove(timePos);
                }
                RecentTrack track = new RecentTrack(jxObject, time);
                result.add(track);
            }

        }
        Logy.d(TAG, "loadInBackground() done:" + (System.currentTimeMillis() - dur) + " itemcnt:" + result.size());
        return result;
    }

}
