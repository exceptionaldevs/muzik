package com.exceptional.musiccore.library.tracks;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.metadata.JXMetaVisitor;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.TrackItemMetaDataVisitor;
import com.exceptional.musiccore.utils.DateUtil;

/**
 * Created by darken on 03.06.2014.
 */
public class Track extends LibraryItem implements Parcelable, TrackLoader.TrackUriSource {
    public static final String URI_TRACKS_TAG = "tracks";
    private final JXObject mJXObject;
    private String mTitle;
    private String mArtist;
    private String mAlbumName;
    private long mSize;
    private long mDuration;

    public Track(JXObject jxObject) {
        super(jxObject.getLibrarySource());
        mJXObject = jxObject;
        mJXObject.getJXMetaFile().accept(new TrackItemMetaDataVisitor(this));
    }

    protected Track(Parcel in) {
        super(in);
        mJXObject = in.readParcelable(JXObject.class.getClassLoader());
        mTitle = in.readString();
        mArtist = in.readString();
        mAlbumName = in.readString();
        mSize = in.readLong();
        mDuration = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(mJXObject, flags);
        dest.writeString(mTitle);
        dest.writeString(mArtist);
        dest.writeString(mAlbumName);
        dest.writeLong(mSize);
        dest.writeLong(mDuration);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    public static Uri getBaseUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    public Uri getUriForTracks() {
        return getLibrarySource();
    }

    public JXObject getJXObject() {
        return mJXObject;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getArtistName() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    @Override
    public String getPrimaryInfo(Context context) {
        return getTitle();
    }

    @Override
    public String getSecondaryInfo(Context context) {
        return getArtistName();
    }

    @Override
    public String getTertiaryInfo(Context context) {
        return DateUtil.formatDuration(getDuration());
    }

    @Override
    public String getSwipeInfo(Context context) {
        return "1";
    }

    @Override
    public <T> T accept(JXMetaVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
