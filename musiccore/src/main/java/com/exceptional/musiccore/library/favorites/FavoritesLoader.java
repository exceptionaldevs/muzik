package com.exceptional.musiccore.library.favorites;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXObjectFactory;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.utils.Logy;
import com.yahoo.squidb.data.SquidCursor;

import java.util.ArrayList;
import java.util.List;

public class FavoritesLoader extends AsyncTaskLoader<List<Track>> {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "FavoritesLoader";
    private List<Track> mData;
    private SquidCursor<FavoriteTrack> mCursor;
    private final ContentObserver mContentObserver = new ForceLoadContentObserver();

    public FavoritesLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(List<Track> data) {
        Logy.d(TAG, "deliverResult(...)");
        if (isReset()) {
            //No no no, bad Android, keep your race conditions
            if (mData != null)
                onReleaseResources(mData);
        }
        List<Track> oldData = mData;
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
            getContext().getContentResolver().registerContentObserver(FavoriteTrack.CONTENT_URI, false, mContentObserver);
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
    public void onCanceled(List<Track> data) {
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

    protected void onReleaseResources(List<Track> data) {
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
    public List<Track> loadInBackground() {
        long dur = System.currentTimeMillis();
        Logy.d(TAG, "loadInBackground()");
        List<Track> result = new ArrayList<>();

        if (mCursor == null) {
            mCursor = FavoritesHelper.buildFavoriteTracksCursor(getContext());
        } else {
            mCursor.requery();
        }
        if (mCursor.getCount() > 0) {
            final FavoriteTrack recentTrack = new FavoriteTrack();
            JXObjectFactory jxObjectFactory = new JXObjectFactory(getContext());
            List<Uri> uris = new ArrayList<>();
            while (mCursor.moveToNext()) {
                recentTrack.readPropertiesFromCursor(mCursor);
                uris.add(Uri.parse(recentTrack.getTrackUriString()));
            }
            List<JXObject> jxObjectList = jxObjectFactory.make(uris);
            result = new ArrayList<>(jxObjectList.size());
            // Reverse order as the last favorited item should be the first in the list.
            for (int i = jxObjectList.size() - 1; i >= 0; i--)
                result.add(new Track(jxObjectList.get(i)));
        }
        Logy.d(TAG, "loadInBackground() done:" + (System.currentTimeMillis() - dur) + " itemcnt:" + result.size());
        return result;
    }

}
