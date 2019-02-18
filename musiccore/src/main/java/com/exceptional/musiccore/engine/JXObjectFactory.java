package com.exceptional.musiccore.engine;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.metadata.JXMetaFile;
import com.exceptional.musiccore.library.albums.Album;
import com.exceptional.musiccore.library.artists.Artist;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.utils.Logy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JXObjectFactory {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "JXObjectFactory";
    private final Context mContext;

    /**
     * Make sure to pass an application context.
     * The passed context might be passed on to objects whose lifetime is not dependend on the activity but on the service
     *
     * @param c an application context
     */
    public JXObjectFactory(Context c) {
        this.mContext = c.getApplicationContext();
    }

    private Context getContext() {
        return mContext;
    }


    /**
     * Single direct uris<br>
     * content://media/external/audio/media/710
     *
     * @param uriList
     * @return
     */
    public List<JXObject> make(final List<Uri> uriList) {
        long timeStart = System.currentTimeMillis();
        List<JXObject> results = new ArrayList<>();
        Map<Uri, JXObject> jxObjectMap = new HashMap<>();
        List<Uri> toProcessList = new ArrayList<>(uriList);
        List<Uri> chunkList = new ArrayList<>();
        while (!toProcessList.isEmpty() || !chunkList.isEmpty()) {
            // SQL is limited to 999 variables per query
            // https://raw.githubusercontent.com/android/platform_external_sqlite/master/dist/sqlite3.c
            if (chunkList.size() < 999 && !toProcessList.isEmpty()) {
                Uri newUri = toProcessList.remove(0);
                if (!jxObjectMap.containsKey(newUri))
                    chunkList.add(newUri);
            } else {
                Cursor cursor = buildCursorForMediaUris(getContext(), chunkList);
                chunkList.clear();
                if (cursor != null) {
                    jxObjectMap.putAll(getJXObjectsFromCursor(cursor));
                    cursor.close();
                }
            }
        }
        for (Uri uri : uriList) {
            if (jxObjectMap.containsKey(uri))
                results.add(jxObjectMap.get(uri));
        }
        long timeStop = System.currentTimeMillis();
        Logy.d(TAG, "MAKETIME:" + (timeStop - timeStart) + " in cnt:" + uriList.size() + " out cnt:" + results.size());
        return results;
    }

    private static Cursor buildCursorForMediaUris(Context context, List<Uri> toQuery) {
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < toQuery.size(); i++) {
            selectionBuilder.append("?");
            if (i < toQuery.size() - 1)
                selectionBuilder.append(",");
        }
        selectionBuilder.append(")");
        String[] selectionArgs = new String[toQuery.size()];
        for (int i = 0; i < toQuery.size(); i++) {
            selectionArgs[i] = toQuery.get(i).getLastPathSegment();
        }

        String[] projection = getTrackProjection();
        String sortOrder = null;

        return context.getContentResolver().query(Track.getBaseUri(), projection, selectionBuilder.toString(), selectionArgs, sortOrder);
    }


    public static Map<Uri, JXObject> getJXObjectsFromCursor(Cursor cursor) {
        Map<Uri, JXObject> results = new HashMap<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                // Identifies this track internally in our provider
                Uri sourceUri = Uri.parse(Track.getBaseUri() + "/" + id);

                String targetString = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if (targetString == null) {
                    Logy.e(TAG, "No MediaStore.Audio.Media.DATA value for " + sourceUri.getPath());
                    continue;
                }
                // External identifier, e.g. file path.
                Uri targetUri = Uri.parse(targetString);

                JXObject jxObj = JXObject.instantiateJXObject(sourceUri, targetUri);
                if (jxObj == null) {
                    Logy.e(TAG, "Couldn't make JXObject for " + sourceUri);
                    continue;
                }

                JXMetaFile metaObject = new JXMetaFile(sourceUri, targetUri);

                int artistIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
                if (artistIdColumn != -1) {
                    Uri artistUri = ContentUris.withAppendedId(Artist.getUriForAllArtists(), cursor.getLong(artistIdColumn));
                    metaObject.setArtistUri(artistUri);
                }
                int artistNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                if (artistNameColumn != -1) {
                    metaObject.setArtistName(cursor.getString(artistNameColumn));
                }

                int albumNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                if (albumNameColumn != -1)
                    metaObject.setAlbumName(cursor.getString(albumNameColumn));

                int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                if (albumIdColumn != -1) {
                    long albumId = cursor.getLong(albumIdColumn);
                    Uri albumUri = ContentUris.withAppendedId(Album.getUriForAllAlbums(), albumId);
                    metaObject.setAlbumUri(albumUri);

                    // TODO Check if cover actually exists
                    Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                    metaObject.setCoverSource(albumArtUri);
                }

                metaObject.setTrackName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                metaObject.setTrackDuration(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                metaObject.setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));

                int trackNumberColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
                if (trackNumberColumn != -1)
                    metaObject.setTrackNumber(cursor.getInt(trackNumberColumn));

                int yearColumn = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR);
                if (yearColumn != -1)
                    metaObject.setYear(cursor.getInt(yearColumn));

                int composerColumn = cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
                if (composerColumn != -1)
                    metaObject.setComposer(cursor.getString(composerColumn));


                metaObject.setDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                int lastAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                if (lastAddedColumn != -1)
                    metaObject.setLastAdded(cursor.getLong(lastAddedColumn));
                int lastModifiedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);
                if (lastModifiedColumn != -1)
                    metaObject.setLastModified(cursor.getLong(lastModifiedColumn));

                jxObj.setJXMetaFile(metaObject);
                results.put(jxObj.getLibrarySource(), jxObj);
            }
        }
        return results;
    }

    public static String[] getTrackProjection() {
        return new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.COMPOSER,
                MediaStore.Audio.Media.YEAR,
        };
    }
}
