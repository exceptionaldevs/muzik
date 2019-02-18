package com.exceptional.musiccore.library.artists;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.exceptional.musiccore.R;
import com.exceptional.musiccore.engine.metadata.JXMetaVisitor;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.library.tracks.TrackLoader;
import com.exceptional.musiccore.utils.DateUtil;

/**
 * This item is an Artist.
 */
public class Artist extends LibraryItem implements Parcelable, TrackLoader.TrackUriSource {
    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
    private String mArtistName;
    private int mAlbumCount;
    private int mTrackCount;
    private long mArtistDuration;

    public Artist(Uri source) {
        super(source);
    }

    public Artist(Parcel in) {
        super(in);
        mArtistName = in.readString();
        mAlbumCount = in.readInt();
        mTrackCount = in.readInt();
    }

    public static Uri getUriForAllArtists() {
        return MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
    }

    public Uri getUriForAlbums() {
        return MediaStore.Audio.Artists.Albums.getContentUri("external", getArtistId());
    }

    public long getArtistId() {
        return Long.parseLong(getLibrarySource().getLastPathSegment());
    }

    @Nullable
    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public int getAlbumCount() {
        return mAlbumCount;
    }

    public void setAlbumCount(int albumCount) {
        mAlbumCount = albumCount;
    }

    public int getTrackCount() {
        return mTrackCount;
    }

    public void setTrackCount(int trackCount) {
        mTrackCount = trackCount;
    }

    @Override
    public String getPrimaryInfo(Context context) {
        return getArtistName();
    }

    @Override
    public String getSecondaryInfo(Context context) {
        return getTrackCount() + " " + context.getString(R.string.tracks);
    }

    @Override
    public String getTertiaryInfo(Context context) {
        return DateUtil.formatDuration(getArtistDuration());
    }

    @Override
    public String getSwipeInfo(Context context) {
        return String.valueOf(getTrackCount());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mArtistName);
        out.writeInt(mAlbumCount);
        out.writeInt(mTrackCount);
    }

    public long getArtistDuration() {
        return mArtistDuration;
    }

    public void setArtistDuration(long artistDuration) {
        mArtistDuration = artistDuration;
    }

    @Override
    public Uri getUriForTracks() {
        return Uri.withAppendedPath(getLibrarySource(), Track.URI_TRACKS_TAG);
    }

    @Override
    public <T> T accept(JXMetaVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
