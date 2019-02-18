package com.exceptional.musiccore.library.tracks;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXObjectFactory;
import com.exceptional.musiccore.library.LibraryLoader;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.utils.Logy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darken on 26.03.14.
 */
public class TrackLoader extends LibraryLoader<List<Track>> {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "TrackLoader";
    private List<Track> mData;

    public TrackLoader(Context context, Bundle arguments) {
        super(context, LoaderArgs.fromBundle(arguments));
    }

    public interface TrackUriSource {
        Uri getUriForTracks();
    }

    public static List<Uri> resolveToTrackUris(Context context, LoaderArgs loaderArgs) {
        List<Uri> trackUris = new ArrayList<>();
        Uri toResolve = loaderArgs.getUri();
        Uri queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE
        };
        StringBuilder selection = new StringBuilder(MediaStore.Audio.Media.IS_MUSIC + " != 0");
        String[] selectionArgs = null;
        String sortOrder = MediaStore.Audio.Media.TRACK;
        if (toResolve.toString().contains(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI.toString())
                && toResolve.getLastPathSegment().equals(Track.URI_TRACKS_TAG)) {
            // This is a request for tracks from an artist.
            // content://media/external/audio/artists/[ID]
            selection.append(" AND ");
            selection.append(MediaStore.Audio.Media.ARTIST_ID + "=?");
            selectionArgs = new String[]{toResolve.getPathSegments().get(toResolve.getPathSegments().size() - 2)};
        } else if (toResolve.toString().contains(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.toString())
                && toResolve.getLastPathSegment().equals(Track.URI_TRACKS_TAG)) {
            // This is a request for tracks from an album.
            // content://media/external/audio/albums/[ID]
            selection.append(" AND ");
            selection.append(MediaStore.Audio.Media.ALBUM_ID + "=?");
            selectionArgs = new String[]{toResolve.getPathSegments().get(toResolve.getPathSegments().size() - 2)};
        } else if (toResolve.toString().contains(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI.toString())
                && toResolve.getLastPathSegment().equals(MediaStore.Audio.Playlists.Members.CONTENT_DIRECTORY)) {
            // This is a request for tracks from a playlist
            // content://media/external/audio/playlists/[ID]/members
            selection = new StringBuilder();
            projection = new String[]{
                    MediaStore.Audio.Playlists.Members.AUDIO_ID
            };
            queryUri = toResolve;
            sortOrder = MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER;
        } else if (toResolve.toString().equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
            // WE want all tracks
            if (loaderArgs.getSelectionStatement() != null) {
                // we want to filter the tracks
                selection.append(" AND ");
                selection.append(loaderArgs.getSelectionStatement());
                selectionArgs = loaderArgs.getSelectionArgs();
            }
        } else if (toResolve.toString().contains(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
            // Direct track uri, no need to do anything
            // content://media/external/audio/media/[ID]
            trackUris.add(toResolve);
            return trackUris;
        }


        int limit = loaderArgs.getResultsLimit();
        if (limit != -1) {
            int offset = 0;
            queryUri = queryUri.buildUpon().encodedQuery("limit=" + offset + "," + limit).build();
        }

        Cursor cursor = context.getContentResolver().query(queryUri, projection, selection.toString(), selectionArgs, sortOrder);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                trackUris.add(ContentUris.withAppendedId(Track.getBaseUri(), id));
            }
            cursor.close();
        }
        return trackUris;
    }

    @Override
    public void deliverResult(List<Track> data) {
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
        forceLoad();

        // TODO we could deliver a cached result and monitor the provider for changes
        // Keep in mind that we would only need to monitor for changes that affect the current search
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<Track> data) {
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

    protected void onReleaseResources(List<Track> data) {
        // Hm nothing to realllllyyy release here, for now at least
    }

    @Override
    public List<Track> loadInBackground() {
        Logy.d(TAG, "loadInBackground start...");
        long start = System.currentTimeMillis();
        List<Track> results = new ArrayList<>();

        List<Uri> trackUris = resolveToTrackUris(getContext(), getLoaderArgs());

        long milestoneTrackUris = System.currentTimeMillis();
        Logy.d(TAG, "loadInBackground milestoneTrackUris:" + (milestoneTrackUris - start) + " itemcnt:" + trackUris.size());


        JXObjectFactory jxObjectFactory = new JXObjectFactory(getContext());
        for (JXObject jxObject : jxObjectFactory.make(trackUris))
            results.add(new Track(jxObject));

        long finish = System.currentTimeMillis();
        Logy.d(TAG, "loadInBackground finish:" + (finish - start) + " itemcnt:" + results.size());
        return results;
    }

}