package com.exceptional.musiccore.library.device;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.exceptional.musiccore.R;
import com.exceptional.musiccore.engine.metadata.JXMetaVisitor;
import com.exceptional.musiccore.library.LibraryItem;

/**
 * Created by darken on 03.06.2014.
 */
public class DirectoryItem extends LibraryItem implements Parcelable {
    public static final Creator<DirectoryItem> CREATOR = new Creator<DirectoryItem>() {
        public DirectoryItem createFromParcel(Parcel in) {
            return new DirectoryItem(in);
        }

        public DirectoryItem[] newArray(int size) {
            return new DirectoryItem[size];
        }
    };
    private String mDirectoryName;
    private int mItemCount;

    public DirectoryItem(Uri source) {
        super(source);
    }

    public DirectoryItem(Parcel in) {
        super(in);
        mDirectoryName = in.readString();
    }

    @Override
    public String getPrimaryInfo(Context context) {
        return getDirectoryName();
    }

    @Override
    public String getSecondaryInfo(Context context) {
        return context.getString(R.string.folder);
    }

    @Override
    public String getTertiaryInfo(Context context) {
        return String.valueOf(getItemCount());
    }

    public String getDirectoryName() {
        return mDirectoryName;
    }

    public void setDirectoryName(String directoryName) {
        mDirectoryName = directoryName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(mDirectoryName);
    }

    public int getItemCount() {
        return mItemCount;
    }

    public void setItemCount(int itemCount) {
        mItemCount = itemCount;
    }

    @Override
    public <T> T accept(JXMetaVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
