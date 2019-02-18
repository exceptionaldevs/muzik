package com.exceptional.musiccore.library.playlists;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.v4.content.Loader;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.library.LibraryLoader;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.utils.Logy;
import com.exceptional.musiccore.utils.UriHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darken on 11/12/14.
 */
public abstract class PlaylistsLoader extends LibraryLoader<List<Playlist>> {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "PlaylistsLoader";
    private final ContentObserver mContentObserver = new Loader.ForceLoadContentObserver();
    private List<Playlist> mData;
    private Cursor mCursor;

    public PlaylistsLoader(Context context, Bundle arguments) {
        super(context, LoaderArgs.fromBundle(arguments));
    }

    public static String[] getPlaylistProjection() {
        return new String[]{
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME,
                MediaStore.Audio.Playlists.DATE_ADDED,
                MediaStore.Audio.Playlists.DATE_MODIFIED};
    }

    @Override
    public void deliverResult(List<Playlist> data) {
        Logy.d(TAG, "deliverResult(...)");
        if (isReset()) {
            //No no no, bad Android, keep your race conditions
            if (mData != null)
                onReleaseResources(mData);
        }
        List<Playlist> oldData = mData;
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
            mCursor.unregisterContentObserver(mContentObserver);
            mCursor = null;
        }
        cancelLoad();
        super.onStopLoading();
    }

    @Override
    public void onCanceled(List<Playlist> data) {
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

    protected void onReleaseResources(List<Playlist> data) {
        Logy.d(TAG, "onReleaseResources(...)");
        if (data != null && !data.isEmpty())
            data.clear();
    }

    @Override
    public void onContentChanged() {
        Logy.d(TAG, "onContentChanged()");
        Thread.currentThread().getStackTrace();
        super.onContentChanged();
    }

    @DrawableRes
    public abstract int supplyDefaultPlaylistIcon();

    @Override
    public List<Playlist> loadInBackground() {
        long dur = System.currentTimeMillis();
        Logy.d(TAG, "loadInBackground()");
        List<Playlist> result = new ArrayList<>();

        if (mCursor != null) {
            mCursor.unregisterContentObserver(mContentObserver);
            mCursor.close();
        }

        ContentResolver resolver = getContext().getContentResolver();

        Uri targetUri = Playlist.getUriForAllPlaylists();
        int limit = getLoaderArgs().getResultsLimit();
        if (limit != -1) {
            int offset = 0;
            targetUri = targetUri.buildUpon().encodedQuery("limit=" + offset + "," + limit).build();
        }

        Cursor mCursor = resolver.query(
                targetUri,
                getPlaylistProjection(),
                getLoaderArgs().getSelectionStatement(),
                getLoaderArgs().getSelectionArgs(),
                MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        if (mCursor != null)
            mCursor.registerContentObserver(mContentObserver);
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                long playlistID = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Playlists._ID));
                String playlistName = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Playlists.NAME));

                long dateAdded = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Playlists.DATE_ADDED));
                long dateLastModified = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Playlists.DATE_MODIFIED));

                Playlist playlist = new Playlist(ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, playlistID));
                playlist.setName(playlistName);
                playlist.setDateAdded(dateAdded);
                playlist.setLastModified(dateLastModified);
                playlist.setCoverSource(UriHelper.drawableToUri(getContext(), supplyDefaultPlaylistIcon()));
                result.add(playlist);
            }
        }

        Logy.d(TAG, "loadInBackground() done:" + (System.currentTimeMillis() - dur) + " itemcnt:" + result.size());
        return result;
    }
}
