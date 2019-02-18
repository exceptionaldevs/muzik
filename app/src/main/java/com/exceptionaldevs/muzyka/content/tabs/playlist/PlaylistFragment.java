package com.exceptionaldevs.muzyka.content.tabs.playlist;


import android.os.Bundle;
import android.support.v4.content.Loader;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.playlists.Playlist;
import com.exceptional.musiccore.library.playlists.PlaylistsLoader;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.tabs.ContentFragment;

import java.util.List;

/**
 * Created by sebnap on 20.01.16.
 */
public class PlaylistFragment extends ContentFragment<Playlist> {

    @Override
    public Loader<List<? extends LibraryItem>> onCreateLoader(int id, Bundle args) {
        Loader<?> loader = new PlaylistsLoader(getActivity(), args) {
            @Override
            public int supplyDefaultPlaylistIcon() {
                return R.drawable.playlist_stock_cover;
            }
        };
        return (Loader<List<? extends LibraryItem>>) loader;
    }

    @Override
    public LoaderArgs getLoaderArgs() {
        return new LoaderArgs.Builder().forUri(Playlist.getUriForAllPlaylists()).build();
    }

    @Override
    public ContentAdapter initAdapter() {
        return new PlaylistAdapter(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(LOADER_ID_PLAYLISTS, getLoaderArgs().toBundle(), this);
    }
}
