package com.exceptionaldevs.muzyka.content.tabs.album;


import android.os.Bundle;
import android.support.v4.content.Loader;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.albums.Album;
import com.exceptional.musiccore.library.albums.AlbumLoader;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.tabs.ContentFragment;

import java.util.List;

/**
 * Created by sebnap on 20.01.16.
 */
public class AlbumFragment extends ContentFragment<Album> {

    @Override
    public Loader<List<? extends LibraryItem>> onCreateLoader(int id, Bundle args) {
        Loader<?> loader = new AlbumLoader(getActivity(), args);
        return (Loader<List<? extends LibraryItem>>) loader;
    }

    @Override
    public LoaderArgs getLoaderArgs() {
        return new LoaderArgs.Builder().forUri(Album.getUriForAllAlbums()).build();
    }

    @Override
    public ContentAdapter initAdapter() {
        return new AlbumAdapter(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(LOADER_ID_ALBUMS, getLoaderArgs().toBundle(), this);
    }
}
