package com.exceptionaldevs.muzyka.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.exceptionaldevs.muzyka.R;

import butterknife.ButterKnife;

/**
 * Created by darken on 21.01.2016.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_container);
        ButterKnife.bind(this);
        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new MainSettingsFragment())
                .commit();
    }
}
