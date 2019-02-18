package com.exceptional.musiccore.library;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.exceptional.musiccore.engine.metadata.JXMetaAcceptor;
import com.exceptional.musiccore.engine.metadata.JXMetaVisitor;
import com.exceptional.musiccore.engine.metadata.LibrarySource;


public class LibraryItem implements Parcelable, LibrarySource, JXMetaAcceptor {
    private final Uri mLibrarySource;
    private Uri mCoverSource;

    public LibraryItem(Parcel in) {
        mLibrarySource = in.readParcelable(Uri.class.getClassLoader());
        mCoverSource = in.readParcelable(Uri.class.getClassLoader());
    }

    public LibraryItem(Uri librarySource) {
        mLibrarySource = librarySource;
    }

    @Override
    public Uri getLibrarySource() {
        return mLibrarySource;
    }

    public Uri getCoverSource() {
        return mCoverSource;
    }

    public void setCoverSource(Uri coverSource) {
        mCoverSource = coverSource;
    }

    public String getPrimaryInfo(Context context) {
        return null;
    }

    public String getSecondaryInfo(Context context) {
        return null;
    }

    public String getTertiaryInfo(Context context) {
        return null;
    }

    public String getSwipeInfo(Context context) {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(mLibrarySource, flags);
        out.writeParcelable(mCoverSource, flags);
    }

    public static final Creator<LibraryItem> CREATOR = new Creator<LibraryItem>() {
        public LibraryItem createFromParcel(Parcel in) {
            return new LibraryItem(in);
        }

        public LibraryItem[] newArray(int size) {
            return new LibraryItem[size];
        }
    };

    @Override
    public <T> T accept(JXMetaVisitor<T> visitor) {
        return visitor.visit(this);
    }
}



