package com.exceptional.musiccore.library.favorites;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayStatsDB;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.utils.Logy;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.sql.Criterion;
import com.yahoo.squidb.sql.Delete;
import com.yahoo.squidb.sql.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darken on 29.06.2015.
 */
public class FavoritesHelper {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "FavoritesHelper";


    public static boolean isFavorite(@NonNull Context context, @NonNull Uri uri) {
        JXPlayStatsDB db = JXPlayStatsDB.getInstance(context);
        Criterion criterion = FavoriteTrack.TRACK_URI_STRING.eq(uri.toString());
        Query query = Query.select(FavoriteTrack.TRACK_URI_STRING).from(FavoriteTrack.TABLE).where(criterion);
        FavoriteTrack favoriteTrack = db.fetchByQuery(FavoriteTrack.class, query);
        Logy.d(TAG, "isFavorite(" + uri.toString() + "):" + (favoriteTrack != null));
        return favoriteTrack != null;
    }

    public static boolean isFavorite(@NonNull Context context, @Nullable JXObject track) {
        return track != null && isFavorite(context, track.getLibrarySource());
    }


    public static void setFavorite(Context context, List<Uri> uriList, boolean favorite) {
        long start = System.currentTimeMillis();
        JXPlayStatsDB db = JXPlayStatsDB.getInstance(context);
        if (favorite) {
            db.beginTransaction();
            List<String> stringUris = new ArrayList<>(uriList.size());
            // A track can only be favorited once.
            for (Uri uri : uriList) {
                if (!stringUris.contains(uri.toString()))
                    stringUris.add(uri.toString());
            }
            Criterion criterion = FavoriteTrack.TRACK_URI_STRING.in(stringUris);
            Delete delete = Delete.from(FavoriteTrack.TABLE).where(criterion);
            db.delete(delete);
            for (String itemUriString : stringUris) {
                FavoriteTrack favoriteTrack = FavoriteTrack.from(Uri.parse(itemUriString));
                db.persist(favoriteTrack);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } else {
            db.beginTransaction();
            List<String> stringUris = new ArrayList<>();
            for (Uri uri : uriList) {
                if (!stringUris.contains(uri.toString()))
                    stringUris.add(uri.toString());
            }
            Criterion criterion = FavoriteTrack.TRACK_URI_STRING.in(stringUris);
            Delete delete = Delete.from(FavoriteTrack.TABLE).where(criterion);
            db.delete(delete);

            db.setTransactionSuccessful();
            db.endTransaction();
        }
        Logy.d(TAG, "setFavorite(size:" + uriList.size() + "state:" + favorite + ") " + (System.currentTimeMillis() - start) + "ms");
    }

    public static void setFavoriteTracks(Context context, List<Track> trackList, boolean favorite) {
        List<Uri> uriList = new ArrayList<>();
        for (Track track : trackList)
            uriList.add(track.getLibrarySource());
        setFavorite(context, uriList, favorite);
    }

    public static void resetFavoriteTracks(Context context, List<FavoriteTrack> favoriteTrackList) {
        List<Uri> uriList = new ArrayList<>();
        for (FavoriteTrack track : favoriteTrackList)
            uriList.add(Uri.parse(track.getTrackUriString()));
        clearFavoriteTracks(context);
        setFavorite(context, uriList, true);
    }

    public static void setFavoriteTrack(@NonNull Context context, @NonNull JXObject track, boolean favorite) {
        List<Uri> uriList = new ArrayList<>();
        uriList.add(track.getLibrarySource());
        setFavorite(context, uriList, favorite);
    }

    public static void clearFavoriteTracks(Context context) {
        Logy.i(TAG, "Favorite tracks cleared.");
        JXPlayStatsDB db = JXPlayStatsDB.getInstance(context);
        Delete delete = Delete.from(FavoriteTrack.TABLE);
        db.delete(delete);
    }

    public static SquidCursor<FavoriteTrack> buildFavoriteTracksCursor(Context context) {
        JXPlayStatsDB db = JXPlayStatsDB.getInstance(context);
        Query query = Query
                .select(FavoriteTrack.TRACK_URI_STRING)
                .from(FavoriteTrack.TABLE);
        return db.query(FavoriteTrack.class, query);
    }

    public static List<FavoriteTrack> getFavoriteTracks(Context context) {
        List<FavoriteTrack> result = new ArrayList<>();
        SquidCursor<FavoriteTrack> cursor = buildFavoriteTracksCursor(context);
        try {
            while (cursor.moveToNext()) {
                FavoriteTrack favoriteTrack = new FavoriteTrack();
                favoriteTrack.readPropertiesFromCursor(cursor);
                result.add(favoriteTrack);
            }
        } finally {
            cursor.close();
        }
        return result;
    }
}