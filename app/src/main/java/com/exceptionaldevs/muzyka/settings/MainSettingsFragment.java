package com.exceptionaldevs.muzyka.settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.exceptionaldevs.muzyka.R;

/**
 * Created by darken on 21.01.2016.
 */
public class MainSettingsFragment extends PreferenceFragmentCompat {
    public static final String PREF_KEY_DRAGGABLE = "main.player.queue.draggable";

    @Override
    public void onCreatePreferences(Bundle savedInstance, String rootKey) {
        addPreferencesFromResource(R.xml.settings_main);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getFragment() != null) {
            Fragment fragment = Fragment.instantiate(getContext(), preference.getFragment());
            getFragmentManager().beginTransaction()
                    .replace(R.id.content, fragment, preference.getFragment())
                    .addToBackStack(null)
                    .commit();
        }
        return super.onPreferenceTreeClick(preference);
    }
}
