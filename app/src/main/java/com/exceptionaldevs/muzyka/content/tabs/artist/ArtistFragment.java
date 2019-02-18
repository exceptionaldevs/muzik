package com.exceptionaldevs.muzyka.content.tabs.artist;


import android.os.Bundle;
import android.support.v4.content.Loader;

import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.artists.Artist;
import com.exceptional.musiccore.library.artists.ArtistLoader;
import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.tabs.ContentFragment;

import java.util.List;

/**
 * Created by sebnap on 20.01.16.
 */
public class ArtistFragment extends ContentFragment<Artist> {

    @Override
    public LoaderArgs getLoaderArgs() {
        return new LoaderArgs.Builder().forUri(Artist.getUriForAllArtists()).build();
    }

    @Override
    public Loader<List<? extends LibraryItem>> onCreateLoader(int id, Bundle args) {
        Loader<?> loader = new ArtistLoader(getActivity(), args);
        return (Loader<List<? extends LibraryItem>>) loader;
    }


    @Override
    public ContentAdapter initAdapter() {
        return new ArtistAdapter((MuzikServiceActivity) getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(LOADER_ID_ARTISTS, getLoaderArgs().toBundle(), this);
    }
}
