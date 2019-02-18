package com.exceptional.musiccore.library.device;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Formatter;

import com.exceptional.musiccore.library.LibraryItem;

import java.io.File;

/**
 * Created by darken on 18.12.2014.
 */
public class FileItem extends LibraryItem implements Parcelable {
    private final File mFile;

    public FileItem(Uri source) {
        super(source);
        mFile = new File(getLibrarySource().getPath());
    }

    @Override
    public String getPrimaryInfo(Context context) {
        return getFile().getName();
    }

    @Override
    public String getTertiaryInfo(Context context) {
        return Formatter.formatShortFileSize(context, getFile().length());
    }

    public File getFile() {
        return mFile;
    }

    public FileItem(Parcel in) {
        super(in);
        mFile = new File(getLibrarySource().getPath());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }

    public static final Creator<FileItem> CREATOR = new Creator<FileItem>() {
        public FileItem createFromParcel(Parcel in) {
            return new FileItem(in);
        }

        public FileItem[] newArray(int size) {
            return new FileItem[size];
        }
    };


}
