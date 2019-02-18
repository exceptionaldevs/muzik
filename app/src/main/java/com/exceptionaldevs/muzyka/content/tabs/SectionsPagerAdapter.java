package com.exceptionaldevs.muzyka.content.tabs;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.exceptionaldevs.muzyka.content.tabs.album.AlbumFragment;
import com.exceptionaldevs.muzyka.content.tabs.artist.ArtistFragment;
import com.exceptionaldevs.muzyka.content.tabs.playlist.PlaylistFragment;
import com.exceptionaldevs.muzyka.content.tabs.tracks.TrackFragment;
import com.exceptionaldevs.muzyka.settings.tabs.TabIdentifier;
import com.exceptionaldevs.muzyka.settings.tabs.TabOrderFragment;

import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
    private final List<TabIdentifier> mTabList = new ArrayList<>();
    private final Context mContext;
    private final SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;

    public SectionsPagerAdapter(final Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        updateTabs();
        // Shh Bby Is Ok
        // https://android.googlesource.com/platform/frameworks/base/+/android-4.4_r1/core/java/android/app/SharedPreferencesImpl.java#72
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently
        mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updateTabs();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    public void updateTabs() {
        mTabList.clear();
        mTabList.addAll(TabOrderFragment.loadActiveTabs(mContext));
        notifyDataSetChanged();
    }

    private static Fragment getFragmentForIdentifier(TabIdentifier identifier) {
        switch (identifier) {
            case ARTISTS:
                return new ArtistFragment();
            case ALBUMS:
                return new AlbumFragment();
            case TRACKS:
                return new TrackFragment();
            case PLAYLISTS:
                return new PlaylistFragment();
            default:
                return null;
        }
    }

    @Override
    public Fragment getItem(int position) {
        return getFragmentForIdentifier(mTabList.get(position));
    }

    @Override
    public int getCount() {
        return mTabList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(mTabList.get(position).titleRes);
    }
}