package com.exceptional.musiccore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayer;
import com.exceptional.musiccore.utils.Logy;

/**
 * Created by darken on 07.02.2016.
 */
public class HeadSetPlugReceiver extends BroadcastReceiver implements JXPlayer.PlayerStateListener {

    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "HeadSetPlugReceiver";
    private final MusicBinder mBinder;
    private boolean mWasPlaying = false;

    public HeadSetPlugReceiver(MusicBinder binder) {
        mBinder = binder;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:
                    Logy.d(TAG, "Headset is unplugged");
                    mWasPlaying = mBinder.getPlayer().getState() == JXPlayer.PlayerState.PLAYING;
                    if (mWasPlaying)
                        mBinder.pause();
                    break;
                case 1:
                    Logy.d(TAG, "Headset is plugged");
                    if (mWasPlaying)
                        mBinder.play();
                    break;
                default:
                    Logy.d(TAG, "Unkown headset state: " + state);
            }
        }
    }

    @Override
    public void onNewPlayerState(JXObject jxObject, JXPlayer.PlayerState state) {

    }
}
