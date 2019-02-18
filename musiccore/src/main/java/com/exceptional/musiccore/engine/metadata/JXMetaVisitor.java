package com.exceptional.musiccore.engine.metadata;


import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.albums.Album;
import com.exceptional.musiccore.library.artists.Artist;
import com.exceptional.musiccore.library.device.DirectoryItem;
import com.exceptional.musiccore.library.device.FileItem;
import com.exceptional.musiccore.library.playlists.Playlist;
import com.exceptional.musiccore.library.tracks.Track;

/**
 * Created by sebnapi on 18.03.14.
 * <p>
 * If new Metadata-Types will be added they need to be added to the Visitor
 * Interface, which will lead to new Implementations on the Visitors so they
 * can handle new types (compare VisitorPattern)
 */
public interface JXMetaVisitor<T> {
    T visit(JXMetaFile jxMetaFile);

    // since our LibraryItems contain also some kind
    // of meta data from which we can resolve things like:
    // headertitles, coverdata, and so on
    T visit(Artist libraryItem);

    T visit(Album libraryItem);

    T visit(Playlist libraryItem);

    T visit(Track libraryItem);

    T visit(LibraryItem libraryItem);

    T visit(FileItem libraryItem);

    T visit(DirectoryItem libraryItem);
}
