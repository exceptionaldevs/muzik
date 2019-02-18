package com.exceptional.musiccore.engine.metadata;


import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.albums.Album;
import com.exceptional.musiccore.library.artists.Artist;
import com.exceptional.musiccore.library.device.DirectoryItem;
import com.exceptional.musiccore.library.device.FileItem;
import com.exceptional.musiccore.library.playlists.Playlist;
import com.exceptional.musiccore.library.tracks.Track;

/**
 * Created by sebnap on 05.12.15.
 */
public abstract class MetaFileOnlyVIsitor<T> implements JXMetaVisitor<T> {

    @Override
    public T visit(Artist libraryItem) {
        return null;
    }

    @Override
    public T visit(Album libraryItem) {
        return null;
    }

    @Override
    public T visit(Playlist libraryItem) {
        return null;
    }

    @Override
    public T visit(Track libraryItem) {
        return null;
    }

    @Override
    public T visit(LibraryItem libraryItem) {
        return null;
    }

    @Override
    public T visit(FileItem libraryItem) {
        return null;
    }

    @Override
    public T visit(DirectoryItem libraryItem) {
        return null;
    }
}
