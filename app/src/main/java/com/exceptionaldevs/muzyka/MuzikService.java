package com.exceptionaldevs.muzyka;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.exceptional.musiccore.MusicService;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayer;

/**
 * Created by darken on 16.01.2016.
 */
public class MuzikService extends MusicService<MuzikBinder> {
    private MuzikNotificationTool mNotificationTool;

    @Override
    public MuzikBinder makeBinder() {
        return new MuzikBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationTool = new MuzikNotificationTool(this);
    }

    @Override
    public void onNewPlayerState(@Nullable final JXObject object, @NonNull final JXPlayer.PlayerState state) {
        super.onNewPlayerState(object, state);
        getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                mNotificationTool.update(state, object != null ? object.getJXMetaFile() : null);
            }
        });
    }

    @Override
    public void onDestroy() {
        mNotificationTool.update(JXPlayer.PlayerState.STOPPED, null);
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        getBinder().stop(true);
        super.onTaskRemoved(rootIntent);
    }
}
