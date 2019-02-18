package com.exceptionaldevs.muzyka.content.tabs.tracks;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exceptional.musiccore.BinderCustomer;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.engine.queuetasks.JXTask;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.library.tracks.TrackLoader;
import com.exceptionaldevs.muzyka.MainActivity;
import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.tabs.ContentFragment;
import com.exceptionaldevs.muzyka.ui.widget.ObservableLinearLayoutManager;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.MultiItemSelector;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMRecyclerView;
import com.exceptionaldevs.muzyka.utils.Logy;
import com.exceptionaldevs.muzyka.utils.QueueHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sebnap on 20.01.16.
 */
public class TrackFragment extends ContentFragment<Track> implements SDMRecyclerView.OnItemClickListener,
        SDMRecyclerView.OnItemLongClickListener {

    MusicBinder mBinder;


    @Override
    public Loader<List<? extends LibraryItem>> onCreateLoader(int id, Bundle args) {
        Loader<?> loader = new TrackLoader(getActivity(), args);
        return (Loader<List<? extends LibraryItem>>) loader;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        v.setBackgroundColor(getResources().getColor(R.color.background_light));

        ((MainActivity) getActivity()).registerBinderCustomer(new BinderCustomer<MusicBinder>() {
            @Override
            public void onBinderAvailable(MusicBinder binder) {
                mBinder = binder;
            }
        });
        return v;
    }

    @Override
    protected void setupRecyclerView(RecyclerView recyclerView) {
        ObservableLinearLayoutManager layoutManager = new ObservableLinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        ((SDMRecyclerView) recyclerView).setOnItemClickListener(this);
        ((SDMRecyclerView) recyclerView).setOnItemLongClickListener(this);
        ((SDMRecyclerView) recyclerView).setChoiceMode(MultiItemSelector.ChoiceMode.MULTIPLE);
    }

    @Override
    public LoaderArgs getLoaderArgs() {
        return new LoaderArgs.Builder().forUri(Track.getBaseUri()).build();
    }

    @Override
    public ContentAdapter initAdapter() {
        return new TrackAdapter((MuzikServiceActivity) getActivity());
    }

    public MusicBinder getBinder() {
        return mBinder;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().restartLoader(LOADER_ID_TRACKS, getLoaderArgs().toBundle(), this);
    }

    @Override
    public boolean onRecyclerItemClick(RecyclerView parent, View view, final int position, long id) {
        ContentAdapter<Track, ?> adapter = getAdapter();
        List<Uri> uris = new ArrayList<>();
        for (Track item : adapter.getData())
            uris.add(item.getUriForTracks());
        QueueHelper.replaceQueue(getContext(), getBinder(), new JXTask.JXTaskCallback() {
            @Override
            public void onTaskDone(JXTask task) {
                if (getBinder() != null && getBinder().getQueueHandler().hasTracks()) {
                    if (getBinder().getQueueHandler().get(position) != null) {
                        getBinder().play(position);
                    } else {
                        Logy.w("TrackFragment", "Warning: Race condition, position not available after queue add?");
                    }
                }
            }
        }, uris, "All Tracks added to queue", getString(R.string.button_undo));
        return true;
    }

    @Override
    public boolean onRecyclerItemLongClick(RecyclerView parent, View view, int position, long id) {
        return false;
    }
}
