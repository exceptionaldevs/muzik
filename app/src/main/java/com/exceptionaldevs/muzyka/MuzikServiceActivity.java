package com.exceptionaldevs.muzyka;

import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.MusicService;
import com.exceptional.musiccore.MusicServiceActivity;

/**
 * Created by sebnap on 23.01.16.
 */
public class MuzikServiceActivity extends MusicServiceActivity<MusicBinder> {
    @Override
    public Class<? extends MusicService> supplyServiceClass() {
        return MuzikService.class;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
