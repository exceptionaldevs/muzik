package com.exceptionaldevs.muzyka.settings.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMRecyclerView;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.drag.DragCallback;
import com.exceptionaldevs.muzyka.utils.PreferenceTools;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by darken on 22.01.2016.
 */
public class TabOrderFragment extends Fragment implements SDMRecyclerView.OnItemClickListener, DragCallback {
    public static final String PREF_KEY_TAB_ORDER = "main.tabs.order";
    private TabAdapter adapter;
    @BindView(R.id.recyclerview) SDMRecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_tabordersettings_layout, container, false);
        ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        adapter = new TabAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setOnItemClickListener(this);
        recyclerView.setOnItemDragListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.tab_settings_title);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.tab_settings_summary);
        List<TabIdentifier> activeTabs = loadActiveTabs(getContext());
        adapter.setActiveTabs(activeTabs);
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    public static List<TabIdentifier> loadActiveTabs(@NonNull final Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> tabStrings = PreferenceTools.getStringArrayPref(prefs, PREF_KEY_TAB_ORDER);

        final List<TabIdentifier> tabIdentifierList = new ArrayList<>();
        if (tabStrings == null) {
            tabIdentifierList.add(TabIdentifier.ARTISTS);
            tabIdentifierList.add(TabIdentifier.ALBUMS);
            tabIdentifierList.add(TabIdentifier.PLAYLISTS);
            tabIdentifierList.add(TabIdentifier.TRACKS);
        } else {
            for (String tab : tabStrings)
                tabIdentifierList.add(TabIdentifier.valueOf(tab));
        }
        return tabIdentifierList;
    }

    private void saveTabs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        List<String> tabStrings = new ArrayList<>();
        for (TabIdentifier tabIdentifier : adapter.getActiveTabs())
            tabStrings.add(tabIdentifier.name());
        PreferenceTools.setStringArrayPref(prefs, PREF_KEY_TAB_ORDER, tabStrings);
    }

    @Override
    public boolean onRecyclerItemClick(RecyclerView parent, View view, int position, long id) {
        adapter.toggleTabIdentifier(position);
        saveTabs();
        return false;
    }

    @Override
    public boolean canDrag(int position) {
        return adapter.isActive(position);
    }

    @Override
    public boolean onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder fromHolder, RecyclerView.ViewHolder toHolder) {
        int adapterFrom = fromHolder.getAdapterPosition();
        int adapterTo = toHolder.getAdapterPosition();
        if (adapterFrom == adapterTo || adapterTo == 0)
            return false;
        Log.e("log", "from: " + adapterFrom + ", to: " + adapterTo);
        if (adapter.isActive(adapterFrom) && adapter.isActive(adapterTo)) {
            adapter.swapItems(adapterFrom, adapterTo);
            saveTabs();
            return true;
        } else if (adapter.isActive(adapterFrom)) {
            adapter.toggleTabIdentifier(adapterFrom);
            saveTabs();
            return false;
        } else {
            return false;
        }
    }

    @Override
    public void onDragStateChanged(@DRAGSTATE int dragState) {

    }


}
