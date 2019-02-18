package com.exceptional.musiccore.engine.metadata;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class JXMetaFile implements JXMetaAcceptor, LibrarySource, Parcelable {
    private final Uri mLibrarySource;
    private final Uri mRealSource;
    private Uri mCoverSource;
    private String mArtistName;
    private String mAlbumName;
    private String mTrackName;
    private long mTrackDuration;
    private Uri mArtistUri;
    private Uri mAlbumUri;
    private Uri mPath;
    private long mSize;
    private int mTrackNumber;
    private int mYear;
    private String mComposer;
    private String mDisplayName;
    private long mLastAdded;
    private long mLastModified;


    public JXMetaFile(Uri librarySource, Uri realSource) {
        mLibrarySource = librarySource;
        mRealSource = realSource;
    }

    protected JXMetaFile(Parcel in) {
        mLibrarySource = in.readParcelable(Uri.class.getClassLoader());
        mRealSource = in.readParcelable(Uri.class.getClassLoader());
        mCoverSource = in.readParcelable(Uri.class.getClassLoader());
        mArtistName = in.readString();
        mAlbumName = in.readString();
        mTrackName = in.readString();
        mTrackDuration = in.readLong();
        mArtistUri = in.readParcelable(Uri.class.getClassLoader());
        mAlbumUri = in.readParcelable(Uri.class.getClassLoader());
        mPath = in.readParcelable(Uri.class.getClassLoader());
        mSize = in.readLong();
        mTrackNumber = in.readInt();
        mYear = in.readInt();
        mComposer = in.readString();
        mDisplayName = in.readString();
        mLastAdded = in.readLong();
        mLastModified = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLibrarySource, flags);
        dest.writeParcelable(mRealSource, flags);
        dest.writeParcelable(mCoverSource, flags);
        dest.writeString(mArtistName);
        dest.writeString(mAlbumName);
        dest.writeString(mTrackName);
        dest.writeLong(mTrackDuration);
        dest.writeParcelable(mArtistUri, flags);
        dest.writeParcelable(mAlbumUri, flags);
        dest.writeParcelable(mPath, flags);
        dest.writeLong(mSize);
        dest.writeInt(mTrackNumber);
        dest.writeInt(mYear);
        dest.writeString(mComposer);
        dest.writeString(mDisplayName);
        dest.writeLong(mLastAdded);
        dest.writeLong(mLastModified);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<JXMetaFile> CREATOR = new Creator<JXMetaFile>() {
        @Override
        public JXMetaFile createFromParcel(Parcel in) {
            return new JXMetaFile(in);
        }

        @Override
        public JXMetaFile[] newArray(int size) {
            return new JXMetaFile[size];
        }
    };

    @Override
    public <T> T accept(JXMetaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String trackName) {
        mTrackName = trackName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public long getTrackDuration() {
        return mTrackDuration;
    }

    public void setTrackDuration(long trackDuration) {
        mTrackDuration = trackDuration;
    }

    public Uri getPath() {
        return mPath;
    }

    public void setArtistUri(Uri artistUri) {
        mArtistUri = artistUri;
    }

    public Uri getArtistUri() {
        return mArtistUri;
    }

    public void setAlbumUri(Uri albumUri) {
        mAlbumUri = albumUri;
    }

    public Uri getAlbumUri() {
        return mAlbumUri;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public long getSize() {
        return mSize;
    }

    public void setTrackNumber(int trackNumber) {
        mTrackNumber = trackNumber;
    }

    public int getTrackNumber() {
        return mTrackNumber;
    }

    public void setYear(int year) {
        mYear = year;
    }

    public int getYear() {
        return mYear;
    }

    public void setComposer(String composer) {
        mComposer = composer;
    }

    public String getComposer() {
        return mComposer;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setLastAdded(long lastAdded) {
        mLastAdded = lastAdded;
    }

    public long getLastAdded() {
        return mLastAdded;
    }

    public void setLastModified(long lastModified) {
        mLastModified = lastModified;
    }

    public long getLastModified() {
        return mLastModified;
    }

    /**
     * Should return the same value as {@link com.exceptional.musiccore.engine.JXAudioObject#getSource()}
     *
     * @return points to the location this meta data is about
     */
    @Override
    public Uri getLibrarySource() {
        return mLibrarySource;
    }

    public void setCoverSource(Uri coverSource) {
        mCoverSource = coverSource;
    }

    public Uri getCoverSource() {
        return mCoverSource;
    }
}
