package com.exceptional.musiccore.library.albums;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.exceptional.musiccore.R;
import com.exceptional.musiccore.engine.metadata.JXMetaVisitor;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.library.tracks.TrackLoader;
import com.exceptional.musiccore.utils.DateUtil;

/**
 * Created by darken on 03.06.2014.
 */
public class Album extends LibraryItem implements Parcelable, TrackLoader.TrackUriSource {
    public static final Creator<Album> CREATOR = new Creator<Album>() {
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    private String mArtistName;
    private String mAlbumName;
    private int mFirstYear;
    private int mLastYear;
    private int mTrackCount;
    private long mAlbumDuration;

    public Album(Uri source) {
        super(source);
    }

    public Album(Parcel in) {
        super(in);
        mArtistName = in.readString();
        mAlbumName = in.readString();
        mFirstYear = in.readInt();
        mLastYear = in.readInt();
        mTrackCount = in.readInt();
        mAlbumDuration = in.readLong();
    }

    public static Uri getUriForAllAlbums() {
        return MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public int getFirstYear() {
        return mFirstYear;
    }

    public void setFirstYear(int firstYear) {
        mFirstYear = firstYear;
    }

    public int getLastYear() {
        return mLastYear;
    }

    public void setLastYear(int lastYear) {
        mLastYear = lastYear;
    }

    public int getTrackCount() {
        return mTrackCount;
    }

    public void setTrackCount(int songCount) {
        mTrackCount = songCount;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    @Override
    public String getPrimaryInfo(Context context) {
        return getAlbumName();
    }

    @Override
    public String getSecondaryInfo(Context context) {
        return getArtistName();
    }

    @Override
    public String getTertiaryInfo(Context context) {
        return getTrackCount() + " " + context.getString(R.string.tracks) + " (" + DateUtil.formatDuration(getAlbumDuration()) + ")";
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
        out.writeString(mAlbumName);
        out.writeInt(mFirstYear);
        out.writeInt(mLastYear);
        out.writeInt(mTrackCount);
        out.writeLong(mAlbumDuration);
    }

    public long getAlbumDuration() {
        return mAlbumDuration;
    }

    public void setAlbumDuration(long albumDuration) {
        mAlbumDuration = albumDuration;
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
