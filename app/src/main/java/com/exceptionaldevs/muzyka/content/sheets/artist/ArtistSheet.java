package com.exceptionaldevs.muzyka.content.sheets.artist;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.queuetasks.JXTask;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.albums.Album;
import com.exceptional.musiccore.library.albums.AlbumLoader;
import com.exceptional.musiccore.library.artists.Artist;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.sheets.ContentSheet;
import com.exceptionaldevs.muzyka.utils.Logy;
import com.exceptionaldevs.muzyka.utils.QueueHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebnap on 20.01.16.
 */
public class ArtistSheet extends ContentSheet<Artist> {
    final static int ACTIVITY_REQUEST_CODE = 1;
    public final static String EXTRA_RESTORE_ITEM_POS = "com.exceptionaldevs.muzyka.content.sheets.artist:EXTRA_RESTORE_ITEM_POS";

    ArtistAlbumsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSheetTitle().setText(getLibraryItem().getArtistName());
    }

    @Override
    public Loader<List<? extends LibraryItem>> onCreateLoader(int id, Bundle args) {
        Loader<?> loader = new AlbumLoader(this, args);
        return (Loader<List<? extends LibraryItem>>) loader;
    }

    @Override
    public LoaderArgs getLoaderArgs() {
        return new LoaderArgs.Builder().forUri(getLibraryItem().getUriForAlbums()).build();
    }

    @Override
    public ContentAdapter initAdapter() {
        mAdapter = new ArtistAlbumsAdapter(this, getRecyclerView());
        return mAdapter;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportLoaderManager().restartLoader(LOADER_ID_ALBUMS, getLoaderArgs().toBundle(), this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_CODE && data != null) {
            Integer restoreItemPos = data.getIntExtra(EXTRA_RESTORE_ITEM_POS, -1);
            if (restoreItemPos != -1) {
                mAdapter.notifyItemChanged(restoreItemPos, true);
            }
        }
    }

    @Override
    protected void playAll() {
        ContentAdapter<Album, ?> adapter = (ContentAdapter<Album, ?>) getAdapter();
        List<Uri> uris = new ArrayList<>();
        for (Album item : adapter.getData())
            uris.add(item.getUriForTracks());
        QueueHelper.replaceQueue(this, getBinder(), new JXTask.JXTaskCallback() {
            @Override
            public void onTaskDone(JXTask task) {
                if (getBinder() != null && getBinder().getQueueHandler().hasTracks()) {
                    int position = findFirstTrackWithArtist(getBinder().getQueueHandler().getTracks(), getLibraryItem());
                    if (getBinder().getQueueHandler().get(position) != null) {
                        getBinder().play(position);
                    } else {
                        Logy.w("ArtistSheet", "Warning: Race condition, position not available after queue add?");
                    }
                }
            }
        }, uris, uris.size() + " Albums added to queue", getString(R.string.button_undo));
    }

    private int findFirstTrackWithArtist(List<JXObject> tracks, Artist libraryItem) {
        for (int i = 0; i < Math.max(tracks.size(), 200); i++) {
            if (tracks.get(i).getJXMetaFile().getArtistName().equals(libraryItem.getArtistName())) {
                return i;
            }
        }
        return 0;
    }
}
