package com.exceptional.musiccore.library.artists;

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
import com.exceptional.musiccore.utils.UriHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by darken on 26.03.14.
 */
public class ArtistLoader extends LibraryLoader<List<Artist>> {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "ArtistLoader";
    private List<Artist> mData;

    public ArtistLoader(Context context, Bundle arguments) {
        super(context, LoaderArgs.fromBundle(arguments));

    }

    public static String[] getArtistProjection() {
        return new String[]{
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS};
    }

    @Override
    public void deliverResult(List<Artist> data) {

        if (isReset()) {
            //No no no, bad Android, keep your race conditions
            if (mData != null)
                onReleaseResources(mData);
        }
        List<Artist> oldData = mData;
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
    public void onCanceled(List<Artist> data) {
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

    protected void onReleaseResources(List<Artist> data) {
        // Hm nothing to realllllyyy release here, for now at least
    }

    @Override
    public List<Artist> loadInBackground() {
        Logy.d(TAG, "loadInBackground start...");
        long dur = System.currentTimeMillis();
        List<Artist> result = new ArrayList<Artist>();
        long processedArtists = 0;
        long processedTracks = 0;

        ContentResolver resolver = getContext().getContentResolver();

        Uri targetUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        int limit = getLoaderArgs().getResultsLimit();
        if (limit != -1) {
            int offset = 0;
            targetUri = targetUri.buildUpon().encodedQuery("limit=" + offset + "," + limit).build();
        }

        Cursor cursor = resolver.query(
                targetUri,
                getArtistProjection(),
                getLoaderArgs().getSelectionStatement(),
                getLoaderArgs().getSelectionArgs(),
                null);


        if (cursor != null) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
                int albumCount = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS));
                int trackCount = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));

                Artist item = new Artist(ContentUris.withAppendedId(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, id));

                item.setArtistName(artist);
                item.setAlbumCount(albumCount);
                item.setTrackCount(trackCount);
                item.setCoverSource(UriHelper.drawableToUri(getContext(), android.R.drawable.ic_media_play));
                result.add(item);
            }
            cursor.close();
        }
        if (!result.isEmpty()) {
            // This is a little tricky matching that very quickly gets us the duration of all this artists tracks
            final HashMap<Long, Artist> artistMapById = new HashMap<>();
            for (Artist item : result)
                artistMapById.put(Long.parseLong(item.getLibrarySource().getLastPathSegment()), item);

            final Uri baseArtWorkUri = Uri.parse("content://media/external/audio/albumart");
            List<Artist> toProcessList = new ArrayList<>(result);
            List<Artist> chunkList = new ArrayList<>();
            // Process artists in chunks of MAX 999
            while (!toProcessList.isEmpty() || !chunkList.isEmpty()) {
                if (chunkList.size() < 999 && !toProcessList.isEmpty()) {
                    chunkList.add(toProcessList.remove(0));
                    processedArtists++;
                } else {
                    StringBuilder selectionBuilder = new StringBuilder();
                    selectionBuilder.append(MediaStore.Audio.Media.ARTIST_ID + " IN (");
                    int size = chunkList.size();
                    for (int i = 0; i < size; i++) {
                        selectionBuilder.append("?");
                        if (i < size - 1)
                            selectionBuilder.append(",");
                    }
                    selectionBuilder.append(")");
                    String[] selectionArgs = new String[chunkList.size()];
                    for (int i = 0; i < size; i++) {
                        selectionArgs[i] = chunkList.get(i).getLibrarySource().getLastPathSegment();
                    }
                    chunkList.clear();

                    String[] projection = new String[]{
                            MediaStore.Audio.Media.ARTIST_ID,
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.DURATION};
                    Cursor chunkCursor = resolver.query(Track.getBaseUri(), projection, selectionBuilder.toString(), selectionArgs, null);

                    if (chunkCursor != null) {
                        while (chunkCursor.moveToNext()) {
                            long trackParentId = chunkCursor.getLong(chunkCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                            Artist parentArtist = artistMapById.get(trackParentId);
                            if (parentArtist != null) {
                                parentArtist.setArtistDuration(parentArtist.getArtistDuration() + chunkCursor.getLong(chunkCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                                Uri albumArtUri = ContentUris.withAppendedId(baseArtWorkUri, chunkCursor.getLong(chunkCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
                                parentArtist.setCoverSource(albumArtUri);
                                processedTracks++;
                            }
                        }
                        chunkCursor.close();
                    }
                }
            }


        }
        Logy.d(TAG, "loadInBackground done:" + (System.currentTimeMillis() - dur) + ", artists:" + processedArtists + ", tracks:" + processedTracks);
        return result;
    }

}