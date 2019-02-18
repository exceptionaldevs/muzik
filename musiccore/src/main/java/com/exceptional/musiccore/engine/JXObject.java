package com.exceptional.musiccore.engine;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.exceptional.musiccore.engine.exoplayer.ExoPlayerPlayer;
import com.exceptional.musiccore.engine.legacymp.MediaPlayerPlayer;
import com.exceptional.musiccore.engine.metadata.JXMetaFile;
import com.exceptional.musiccore.engine.metadata.LibrarySource;
import com.exceptional.musiccore.utils.ApiHelper;

/**
 * Main holder object for audio resources of any kind that our app should handle
 */
public class JXObject implements LibrarySource, Parcelable {
    private final Uri mSource;
    private final Uri mTarget;
    private final Class<? extends JXAudioObject> mAudioObjectClass;
    private SourceResolver mSourceResolver;
    private JXMetaFile mJXMetaFile;

    public JXObject(@NonNull Uri source, @NonNull Uri target, @NonNull Class<? extends JXAudioObject> audio) {
        this.mSource = source;
        this.mTarget = target;
        this.mAudioObjectClass = audio;
    }

    protected JXObject(Parcel in) {
        mSource = in.readParcelable(Uri.class.getClassLoader());
        mTarget = in.readParcelable(Uri.class.getClassLoader());
        mAudioObjectClass = (Class<? extends JXAudioObject>) in.readSerializable();
        mJXMetaFile = in.readParcelable(JXMetaFile.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mSource, flags);
        dest.writeParcelable(mTarget, flags);
        dest.writeSerializable(mAudioObjectClass);
        dest.writeParcelable(mJXMetaFile, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<JXObject> CREATOR = new Creator<JXObject>() {
        @Override
        public JXObject createFromParcel(Parcel in) {
            return new JXObject(in);
        }

        @Override
        public JXObject[] newArray(int size) {
            return new JXObject[size];
        }
    };

    public void setSourceResolver(SourceResolver sourceResolver) {
        mSourceResolver = sourceResolver;
    }

    public SourceResolver getSourceResolver() {
        return mSourceResolver;
    }

    @Override
    @NonNull
    public Uri getLibrarySource() {
        return mSource;
    }

    @NonNull
    public Uri getPlayableSource() {
        return mTarget;
    }

    public Class<? extends JXAudioObject> getAudioObjectClass() {
        return mAudioObjectClass;
    }

    public JXMetaFile getJXMetaFile() {
        return mJXMetaFile;
    }

    public void setJXMetaFile(JXMetaFile JXMeta) {
        mJXMetaFile = JXMeta;
    }

    public enum ResourceType {
        _3GP("3gp"),
        MP4("mp4"),
        M4A("m4a"),
        FLAC("flac"),
        MP3("mp3"),
        OGG("ogg"),
        WAV("wav");
        private String mExtension;

        ResourceType(String filesuffix) {
            this.mExtension = filesuffix;
        }

        public static ResourceType getEnumForExtension(String otherName) {
            if (otherName == null)
                return null;

            ResourceType result = null;
            for (ResourceType type : values()) {
                if (type.toString().equals(otherName)) {
                    result = type;
                    break;
                }
            }
            return result;
        }

        public String toString() {
            return mExtension;
        }
    }

    public static JXObject instantiateJXObject(@NonNull Uri idUri, @NonNull Uri targetUri) {
        String extension = getExtension(targetUri.toString().toLowerCase());
        ResourceType resourceType = ResourceType.getEnumForExtension(extension);
        if (ApiHelper.hasJellyBean() && ExoPlayerPlayer.isSupported(resourceType)) {
            return new JXObject(idUri, targetUri, ExoPlayerPlayer.class);
        } else {
            return new JXObject(idUri, targetUri, MediaPlayerPlayer.class);
        }
    }

    private static String getExtension(String file) {
        int i = file.lastIndexOf('.');
        if (i > 0) {
            return file.substring(i + 1);
        } else {
            return "";
        }
    }

}
