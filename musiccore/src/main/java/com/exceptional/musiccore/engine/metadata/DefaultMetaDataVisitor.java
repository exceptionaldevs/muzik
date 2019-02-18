package com.exceptional.musiccore.engine.metadata;

/**
 * Created by darken on 20.03.14.
 */
public class DefaultMetaDataVisitor extends MetaFileOnlyVIsitor<DefaultMetaData> {

    @Override
    public DefaultMetaData visit(JXMetaFile jxMetaFile) {
        DefaultMetaData meta = new DefaultMetaData();
        meta.setTitle(jxMetaFile.getTrackName());
        meta.setArtist(jxMetaFile.getArtistName());
        meta.setAlbum(jxMetaFile.getAlbumName());
        return meta;
    }

}