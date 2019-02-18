package com.exceptional.musiccore.engine.metadata;

/**
 * Created by darken on 20.03.14.
 */
public class DefaultMetaData {
    private String mArtist;
    private String mTitle;
    private String mAlbum;

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }
}