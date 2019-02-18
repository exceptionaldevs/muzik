package com.exceptional.musiccore.library;

import com.exceptional.musiccore.engine.metadata.JXMetaFile;
import com.exceptional.musiccore.engine.metadata.MetaFileOnlyVIsitor;
import com.exceptional.musiccore.library.tracks.Track;

/**
 * Created by sebnapi on 18.03.14.
 * <p/>
 * Visitor for PlaylistItems
 */
public class TrackItemMetaDataVisitor extends MetaFileOnlyVIsitor<Track> {

    private Track mItem;

    public TrackItemMetaDataVisitor(Track item) {
        mItem = item;
    }

    @Override
    public Track visit(JXMetaFile jxMetaFile) {
        mItem.setTitle(jxMetaFile.getTrackName());
        mItem.setArtist(jxMetaFile.getArtistName());
        mItem.setAlbumName(jxMetaFile.getAlbumName());
        mItem.setCoverSource(jxMetaFile.getCoverSource());
        mItem.setSize(jxMetaFile.getSize());
        mItem.setDuration(jxMetaFile.getTrackDuration());
        return mItem;
    }

}
