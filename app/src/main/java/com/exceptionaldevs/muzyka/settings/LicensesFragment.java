package com.exceptionaldevs.muzyka.settings;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.exceptionaldevs.muzyka.R;

/**
 * Created by darken on 22.01.2016.
 */
public class LicensesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.settings_licenses);
    }
}
