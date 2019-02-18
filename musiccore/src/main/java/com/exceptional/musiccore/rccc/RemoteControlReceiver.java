package com.exceptional.musiccore.rccc;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.MusicService;
import com.exceptional.musiccore.utils.Logy;

/**
 * Created by darken on 15.05.14.
 */
public class RemoteControlReceiver extends BroadcastReceiver {

    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "RemoteControlReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            final KeyEvent whatEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (whatEvent == null)
                return;

            // We could register our own broadcast receiver, but currently we do fine by just emulating the notification remote control
            final ComponentName srvComp = new ComponentName(context, MusicService.class);
            Intent forwardingIntent = new Intent();
            forwardingIntent.setComponent(srvComp);
            switch (whatEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    Logy.d(TAG, "KEYCODE_MEDIA_STOP");
                    forwardingIntent.setAction(MusicService.ACTION_PLAYER_STOP);
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    Logy.d(TAG, "KEYCODE_HEADSETHOOK");
                    // TODO
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    Logy.d(TAG, "KEYCODE_MEDIA_PLAY_PAUSE");
                    forwardingIntent.setAction(MusicService.ACTION_PLAYER_PLAY_PAUSE);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    Logy.d(TAG, "KEYCODE_MEDIA_NEXT");
                    forwardingIntent.setAction(MusicService.ACTION_PLAYER_NEXT);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    Logy.d(TAG, "KEYCODE_MEDIA_PREVIOUS");
                    forwardingIntent.setAction(MusicService.ACTION_PLAYER_PREVIOUS);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    Logy.d(TAG, "KEYCODE_MEDIA_PAUSE");
                    forwardingIntent.setAction(MusicService.ACTION_PLAYER_PAUSE);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    Logy.d(TAG, "KEYCODE_MEDIA_PLAY");
                    forwardingIntent.setAction(MusicService.ACTION_PLAYER_PLAY);
                    break;
                case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                    Logy.d(TAG, "KEYCODE_MEDIA_FAST_FORWARD");
                    // TODO
                    break;
                case KeyEvent.KEYCODE_MEDIA_REWIND:
                    Logy.d(TAG, "KEYCODE_MEDIA_REWIND");
                    // TODO
                    break;
            }

            if (whatEvent.getAction() == KeyEvent.ACTION_DOWN) {
                Logy.d(TAG, "ACTION_DOWN");
            } else if (whatEvent.getAction() == KeyEvent.ACTION_UP) {
                Logy.d(TAG, "ACTION_UP");
                context.startService(forwardingIntent);
            } else if (whatEvent.getAction() == KeyEvent.ACTION_MULTIPLE) {
                Logy.d(TAG, "ACTION_MULTIPLE");
            }
        }
    }

}
