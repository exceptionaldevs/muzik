package com.exceptional.musiccore.library.albums;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.library.LibraryLoader;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.utils.Logy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by darken on 26.03.14.
 */
public class AlbumLoader extends LibraryLoader<List<Album>> {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "AlbumLoader";


    private List<Album> mData;

    public AlbumLoader(Context context, Bundle loaderArgs) {
        super(context, LoaderArgs.fromBundle(loaderArgs));
    }

    public static String[] getAlbumProjection() {
        return new String[]{
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                MediaStore.Audio.Albums.FIRST_YEAR,
                MediaStore.Audio.Albums.LAST_YEAR,
                MediaStore.Audio.Albums.ALBUM_ART,
        };
    }

    @Override
    public void deliverResult(List<Album> data) {

        if (isReset()) {
            //No no no, bad Android, keep your race conditions
            if (mData != null)
                onReleaseResources(mData);
        }
        List<Album> oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null)
            onReleaseResources(oldData);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();

        // TODO we could deliver a cached result and monitor the provider for changes
        // Keep in mind that we would only need to monitor for changes that affect the current search
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<Album> data) {
        onReleaseResources(data);
    }

    @Override
    protected void onReset() {
        onStopLoading();

        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }
    }

    protected void onReleaseResources(List<Album> data) {
        // Hm nothing to realllllyyy release here, for now at least
    }

    @Override
    public List<Album> loadInBackground() {
        Logy.d(TAG, "loadInBackground start...");
        long dur = System.currentTimeMillis();
        List<Album> result = new ArrayList<Album>();

        ContentResolver resolver = getContext().getContentResolver();
        Uri targetUri = getLoaderArgs().getUri();
        int limit = getLoaderArgs().getResultsLimit();
        if (limit != -1) {
            int offset = 0;
            targetUri = targetUri.buildUpon().encodedQuery("limit=" + offset + "," + limit).build();
        }

        Cursor cursor = resolver.query(
                targetUri,
                getAlbumProjection(),
                getLoaderArgs().getSelectionStatement(),
                getLoaderArgs().getSelectionArgs(),
                null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Albums._ID));
                Album item = new Album(ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id));

                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, id);
                item.setCoverSource(albumArtUri);

                item.setArtistName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));

                int albumNameColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
                if (albumNameColumn != -1)
                    item.setAlbumName(cursor.getString(albumNameColumn));

                int firstYearColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.FIRST_YEAR);
                if (firstYearColumn != -1)
                    item.setFirstYear(cursor.getInt(firstYearColumn));

                int lastYearColumn = cursor.getColumnIndex(MediaStore.Audio.Albums.LAST_YEAR);
                if (lastYearColumn != -1)
                    item.setLastYear(cursor.getInt(lastYearColumn));

                item.setTrackCount(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS)));
                result.add(item);
            }
            cursor.close();
        }


        if (!result.isEmpty()) {
            HashMap<Long, Album> albumItemMap = new HashMap<>();
            for (Album item : result)
                albumItemMap.put(Long.parseLong(item.getLibrarySource().getLastPathSegment()), item);

            List<Album> toProcessList = new ArrayList<>(result);
            List<Album> chunkList = new ArrayList<>();
            // Process artists in chunks of MAX 999
            while (!toProcessList.isEmpty() || !chunkList.isEmpty()) {
                if (chunkList.size() < 999 && !toProcessList.isEmpty()) {
                    chunkList.add(toProcessList.remove(0));
                } else {
                    StringBuilder selectionBuilder = new StringBuilder();
                    selectionBuilder.append(MediaStore.Audio.Media.ALBUM_ID + " IN (");
                    int size = chunkList.size();
                    for (int i = 0; i < size; i++) {
                        selectionBuilder.append("?");
                        if (i < size - 1)
                            selectionBuilder.append(",");
                    }
                    selectionBuilder.append(")");

                    String[] selectionArgs = new String[chunkList.size()];
                    for (int i = 0; i < size; i++)
                        selectionArgs[i] = chunkList.get(i).getLibrarySource().getLastPathSegment();
                    chunkList.clear();

                    String[] projection = new String[]{
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.DURATION};
                    Cursor tracksInAlbums = resolver.query(Track.getBaseUri(), projection, selectionBuilder.toString(), selectionArgs, null);

                    if (tracksInAlbums != null) {
                        while (tracksInAlbums.moveToNext()) {
                            long trackParentId = tracksInAlbums.getLong(tracksInAlbums.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                            Album parentAlbum = albumItemMap.get(trackParentId);
                            if (parentAlbum != null)
                                parentAlbum.setAlbumDuration(parentAlbum.getAlbumDuration() + tracksInAlbums.getLong(tracksInAlbums.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                        }
                        tracksInAlbums.close();
                    }
                }
            }

        }
        Logy.d(TAG, "loadInBackground done:" + (System.currentTimeMillis() - dur));
        return result;
    }

}