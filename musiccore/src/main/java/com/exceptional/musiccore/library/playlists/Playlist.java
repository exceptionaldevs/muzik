package com.exceptional.musiccore.library.playlists;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import com.exceptional.musiccore.engine.metadata.JXMetaVisitor;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.tracks.TrackLoader;
import com.exceptional.musiccore.utils.Logy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This represents a playlist data-wise
 */
public class Playlist extends LibraryItem implements Parcelable, TrackLoader.TrackUriSource {
    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
    private static final Pattern TRACKS_URI_PATTERN = Pattern.compile("(?>content:\\/\\/media\\/[\\w]+\\/audio\\/playlists\\/)([0-9]+)(?>\\/members)");
    private static String TAG = "JX:PlaylistItem";
    private String mName;
    private long mDateAdded;
    private long mLastModified;

    public Playlist(Uri source) {
        super(source);
        TAG += "JX:PlaylistItem:" + getLibrarySource().toString();
    }

    public Playlist(Parcel in) {
        super(in);
        TAG += "JX:PlaylistItem:" + getLibrarySource().toString();
        mName = in.readString();
        mDateAdded = in.readLong();
        mLastModified = in.readLong();
    }

    public static Playlist fromPlaylistTrackUri(Uri uri) {
        Matcher m = TRACKS_URI_PATTERN.matcher(uri.toString());
        if (m.matches()) {
            int playlistId = Integer.parseInt(m.group(1));
            return Playlist.fromPlaylistID(playlistId);
        }
        return null;
    }

    public static Playlist fromPlaylistID(int playlistID) {
        return new Playlist(ContentUris.withAppendedId(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, playlistID));
    }

    public static Playlist createPlaylistWithName(Context c, String playlistName) throws Exception {
        ContentValues mInserts = new ContentValues();
        mInserts.put(MediaStore.Audio.Playlists.NAME, playlistName);
        mInserts.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        mInserts.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        Uri playlistUri = c.getContentResolver().insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, mInserts);
        if (playlistUri == null)
            throw new Exception("Playlist name already exists");
        Logy.d(TAG, "Created playlist(" + playlistName + "):" + playlistUri);
        return new Playlist(playlistUri);
    }

    public static boolean doesPlaylistExist(Context c, String name) {
        String selection = MediaStore.Audio.Playlists.NAME + " = '" + name + "'";
        Cursor cursor = c.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{},
                selection, null, null);
        boolean playlistExists = false;
        if (cursor != null) {
            playlistExists = cursor.getCount() > 0;
            cursor.close();
        }
        return playlistExists;
    }

    public static Playlist mergePlaylists(Context context, List<Playlist> toMerge, String newName) throws Exception {
        Playlist mergeResult = createPlaylistWithName(context, newName);
        List<Uri> collectedTracks = new ArrayList<>();
        for (Playlist playlist : toMerge) {
            collectedTracks.addAll(playlist.getTrackUris(context));
        }
        mergeResult.addTracks(context, collectedTracks);
        return mergeResult;
    }

    public static Uri getUriForAllPlaylists() {
        return MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public long getDateAdded() {
        return mDateAdded;
    }

    public void setDateAdded(long dateAdded) {
        mDateAdded = dateAdded;
    }

    public long getLastModified() {
        return mLastModified;
    }

    public void setLastModified(long lastModified) {
        mLastModified = lastModified;
    }

    @Override
    public String getPrimaryInfo(Context context) {
        return mName;
    }

    @Override
    public String getSecondaryInfo(Context context) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(mLastModified);
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mName);
        out.writeLong(mDateAdded);
        out.writeLong(mLastModified);
    }

    public Uri getUriForTracks() {
        return MediaStore.Audio.Playlists.Members.getContentUri("external", getPlaylistId());
    }

    public long getPlaylistId() {
        return Long.parseLong(getLibrarySource().getLastPathSegment());
    }

    public void rename(Context c, String newName) {
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Audio.Playlists.NAME, newName);
        c.getContentResolver().update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values, "_id=" + getPlaylistId(), null);
    }

    public void deletePlaylist(Context c) {
        List<String> segments = getLibrarySource().getPathSegments();
        if (segments.size() >= 2 && segments.get(segments.size() - 2).equals("playlists")) {
            String playlistID = segments.get(segments.size() - 1);
            String where = MediaStore.Audio.Playlists._ID + "=?";
            String[] whereVal = {playlistID};
            c.getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
            Logy.d(TAG, "Deleted playlist:" + getLibrarySource());
        }
    }

    public void addTracks(Context c, List<Uri> tracks) {
        List<String> segments = getLibrarySource().getPathSegments();
        long playlistID = -1;
        if (segments.size() >= 2 && segments.get(segments.size() - 2).equals("playlists")) {
            playlistID = Long.parseLong(segments.get(segments.size() - 1));
        }
        if (playlistID != -1) {
            String[] cols = new String[]{
                    "count(*)"
            };
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID);
            Cursor cur = c.getContentResolver().query(uri, cols, null, null, null);
            cur.moveToFirst();
            final int base = cur.getInt(0);
            cur.close();
            ContentValues[] values = new ContentValues[tracks.size()];
            for (int i = 0; i < tracks.size(); i++) {
                Uri track = tracks.get(i);
                int audioId = Integer.parseInt(track.getLastPathSegment());
                values[i] = new ContentValues();
                values[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + i + 1);
                values[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
            }
            c.getContentResolver().bulkInsert(uri, values);
            Logy.d(TAG, "Added " + tracks.size() + " tracks.");
        }
    }

    public void removeTracks(Context c, List<Uri> tracks) {
        List<String> segments = getLibrarySource().getPathSegments();
        long playlistID = -1;
        if (segments.size() >= 2 && segments.get(segments.size() - 2).equals("playlists")) {
            playlistID = Long.parseLong(segments.get(segments.size() - 1));
        }
        Uri playlistUri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID);
        ArrayList<ContentProviderOperation> operationList = new ArrayList<>();
        for (Uri track : tracks) {
            int audioId = Integer.parseInt(track.getLastPathSegment());
            operationList.add(ContentProviderOperation.newDelete(playlistUri).withSelection(MediaStore.Audio.Playlists.Members.AUDIO_ID + " =? ", new String[]{String.valueOf(audioId)}).build());
        }
        try {
            c.getContentResolver().applyBatch(MediaStore.AUTHORITY, operationList);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public List<Uri> getTrackUris(Context c) {
        List<Uri> trackUris = new ArrayList<>();
        List<String> segments = getLibrarySource().getPathSegments();
        if (segments.size() >= 2 && segments.get(segments.size() - 2).equals("playlists")) {
            long playlistID = Long.parseLong(segments.get(segments.size() - 1));
            String[] projection = {
                    MediaStore.Audio.Playlists.Members.AUDIO_ID
            };
            Cursor cursor = c.getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID),
                    projection,
                    null,
                    null,
                    MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    long audioId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                    Logy.d(TAG, "audioId:" + audioId);
                    trackUris.add(ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId));
                }
                cursor.close();
            }
        }
        return trackUris;
    }

    public void moveTrack(Context context, int oldPosition, int newPosition) {
        MediaStore.Audio.Playlists.Members.moveItem(context.getContentResolver(), getPlaylistId(), oldPosition, newPosition);
    }

    @Override
    public <T> T accept(JXMetaVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
