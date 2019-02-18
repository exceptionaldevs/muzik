package com.exceptionaldevs.muzyka.content.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.ui.widget.SplashLayout;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sebnap on 19.01.16.
 */
public abstract class ContentFragment<T extends LibraryItem> extends Fragment implements LoaderManager.LoaderCallbacks<List<? extends LibraryItem>> {
    public static final int LOADER_ID_ARTISTS = 1;
    public static final int LOADER_ID_ALBUMS = 2;
    public static final int LOADER_ID_TRACKS = 3;
    public static final int LOADER_ID_PLAYLISTS = 4;
    public static final int LOADER_ID_DEVICE = 5;

    @BindView(R.id.splash_layout) SplashLayout mSplash;
    @BindView(R.id.recyclerview) RecyclerView mRecyclerView;

    public ContentAdapter<T, ? extends SDMViewHolder> getAdapter() {
        return (ContentAdapter<T, ? extends SDMViewHolder>) getRecyclerView().getAdapter();
    }

    public abstract LoaderArgs getLoaderArgs();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        setupRecyclerView(mRecyclerView);
        return view;
    }

    protected void setupRecyclerView(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = new GridLayoutManager(recyclerView.getContext(), 3, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @Override
    public abstract Loader<List<? extends LibraryItem>> onCreateLoader(int id, Bundle args);

    @Override
    public void onLoadFinished(Loader<List<? extends LibraryItem>> loader, List<? extends LibraryItem> data) {
        displayData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<? extends LibraryItem>> loader) {
//        if (getAdapter() instanceof JXPlayer.PlayerProgressListener)
//            getJXBinder().removePlayerProgressListener((JXPlayer.PlayerProgressListener) getAdapter());
        getAdapter().notifyDataSetChanged();
        getAdapter().setData(null);
    }

    public abstract ContentAdapter<T, ?> initAdapter();

    public void displayData(List<? extends LibraryItem> data) {
        ContentAdapter<T, ?> adapter = getAdapter();
        boolean newAdapter = adapter == null;
        if (newAdapter)
            adapter = initAdapter();


        adapter.setData((List<T>) data);
        if (newAdapter) {
            getRecyclerView().setAdapter(adapter);
//            if (adapter instanceof JXPlayer.PlayerProgressListener) {
//                // FIXME after app resume crash due to binder being null
//                getJXBinder().addPlayerProgressListener((JXPlayer.PlayerProgressListener) adapter);
//            }
        } else {
            adapter.notifyDataSetChanged();
        }

        if (!(data.size() > 0)) {
            mSplash.setVisibility(View.VISIBLE);
        } else {
            mSplash.setVisibility(View.GONE);
        }
    }

}
