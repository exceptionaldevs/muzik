package com.exceptionaldevs.muzyka.content.sheets.playlist;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.exceptional.musiccore.engine.queuetasks.JXTask;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.playlists.Playlist;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.library.tracks.TrackLoader;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.sheets.ContentSheet;
import com.exceptionaldevs.muzyka.ui.widget.ObservableLinearLayoutManager;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMRecyclerView;
import com.exceptionaldevs.muzyka.utils.Logy;
import com.exceptionaldevs.muzyka.utils.QueueHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebnap on 20.01.16.
 */
public class PlaylistSheet extends ContentSheet<Playlist> implements SDMRecyclerView.OnItemClickListener, SDMRecyclerView.OnItemLongClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSheetTitle().setText(getLibraryItem().getName());
    }

    @Override
    protected void setupRecyclerView(RecyclerView recyclerView) {
        ObservableLinearLayoutManager layoutManager = new ObservableLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setListener(new ObservableLinearLayoutManager.OnOverscroll() {
            @Override
            public void onOverscollTop() {
                if (getHeaderTranslationZ() != 0) {
                    setHeaderTranslationZ(0);
                }
            }

            @Override
            public void onOverscrollBottom() {

            }

            @Override
            public void onScroll() {
                if (getHeaderTranslationZ() != getAppBarElevation()) {
                    animateTranslationZ(getAppBarElevation());
                }
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        int padding = getResources().getDimensionPixelSize(R.dimen.padding_normal);
        recyclerView.setPadding(0, padding, 0, padding);
        ((SDMRecyclerView) recyclerView).setOnItemClickListener(this);
        ((SDMRecyclerView) recyclerView).setOnItemLongClickListener(this);
    }

    @Override
    public Loader<List<? extends LibraryItem>> onCreateLoader(int id, Bundle args) {
        Loader<?> loader = new TrackLoader(this, args);
        return (Loader<List<? extends LibraryItem>>) loader;
    }

    @Override
    public LoaderArgs getLoaderArgs() {
        return new LoaderArgs.Builder().forUri(getLibraryItem().getUriForTracks()).build();
    }

    @Override
    public ContentAdapter initAdapter() {
        return new PlaylistTracksAdapter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportLoaderManager().restartLoader(LOADER_ID_TRACKS, getLoaderArgs().toBundle(), this);
    }

    @Override
    protected void playAll() {
        ContentAdapter<Track, ?> adapter = (ContentAdapter<Track, ?>) getAdapter();
        List<Uri> uris = new ArrayList<>();
        for (Track item : adapter.getData())
            uris.add(item.getUriForTracks());
        QueueHelper.replaceQueue(this, getBinder(), new JXTask.JXTaskCallback() {
            @Override
            public void onTaskDone(JXTask task) {
                if (getBinder() != null && getBinder().getQueueHandler().hasTracks()) {
                    if (getBinder().getQueueHandler().get(0) != null) {
                        getBinder().play(0);
                    } else {
                        Logy.w("PlaylistSheet", "Warning: Race condition, position not available after queue add?");
                    }
                }
            }
        }, uris, uris.size()+" Tracks added to queue", getString(R.string.button_undo));
    }

    @Override
    public boolean onRecyclerItemClick(RecyclerView parent, View view, final int position, long id) {
        ContentAdapter<Track, ?> adapter = (ContentAdapter<Track, ?>) getAdapter();
        List<Uri> uris = new ArrayList<>();
        for (Track item : adapter.getData())
            uris.add(item.getUriForTracks());
        QueueHelper.replaceQueue(this, getBinder(), new JXTask.JXTaskCallback() {
            @Override
            public void onTaskDone(JXTask task) {
                if (getBinder() != null && getBinder().getQueueHandler().hasTracks()) {
                    if (getBinder().getQueueHandler().get(position) != null) {
                        getBinder().play(position);
                    } else {
                        Logy.w("PlaylistSheet", "Warning: Race condition, position not available after queue add?");
                    }
                }
            }
        }, uris, uris.size()+" Tracks added to queue", getString(R.string.button_undo));
        return true;
    }

    @Override
    public boolean onRecyclerItemLongClick(RecyclerView parent, View view, int position, long id) {
        Uri trackUri = getLibraryItem().getTrackUris(getApplicationContext()).get(position);
        List<Uri> resolvedUris = TrackLoader.resolveToTrackUris(getApplicationContext(), new LoaderArgs.Builder().forUri(trackUri).build());
        getLibraryItem().removeTracks(this, resolvedUris);
        getAdapter().notifyItemRemoved(position);
        return false;
    }
}
