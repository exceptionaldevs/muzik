package com.exceptional.musiccore;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayer;
import com.exceptional.musiccore.engine.queuetasks.JXTasker;
import com.exceptional.musiccore.utils.Logy;

/**
 * Created by darken on 12.01.2016.
 */
public abstract class MusicService<B extends MusicBinder> extends Service implements JXPlayer.PlayerStateListener, AudioManager.OnAudioFocusChangeListener {
    public static final String ACTION_PLAYER_NEXT = "com.apodamusic.player.action.PLAYER_NEXT";
    public static final String ACTION_PLAYER_PREVIOUS = "com.apodamusic.player.action.PLAYER_PREVIOUS";
    public static final String ACTION_PLAYER_PLAY = "com.apodamusic.player.action.PLAYER_PLAY";
    public static final String ACTION_PLAYER_PAUSE = "com.apodamusic.player.action.PLAYER_PAUSE";
    public static final String ACTION_PLAYER_PLAY_PAUSE = "com.apodamusic.player.action.PLAYER_PLAY_PAUSE";
    public static final String ACTION_PLAYER_STOP = "com.apodamusic.player.action.PLAYER_STOP";
    public static final String ACTION_FAVORITE_ON = "com.apodamusic.player.action.FAVORITE_ON";
    public static final String ACTION_FAVORITE_OFF = "com.apodamusic.player.action.FAVORITE_OFF";

    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "MusicService";

    private JXPlayer mPlayer;
    private AudioManager mAudioManager;
    protected final JXTasker mTasker = new JXTasker();
    private B mBinder;
    private boolean mBound;
    private Handler mMainThreadHandler;
    private HeadSetPlugReceiver mHeadSetPlugReceiver;

    @Override
    public void onCreate() {
        Logy.d(TAG, "onCreate");
        super.onCreate();
        mMainThreadHandler = new Handler(getMainLooper());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mPlayer = new JXPlayer(this);
        mPlayer.addPlayerStateListener(this);
        mHeadSetPlugReceiver = new HeadSetPlugReceiver(getBinder());
        IntentFilter headSetPlugIntent = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadSetPlugReceiver, headSetPlugIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Logy.d(TAG, "onStartCommand#ACTION " + action);
            if (action.equals(ACTION_PLAYER_PREVIOUS)) {
                getBinder().previous();
            } else if (action.equals(ACTION_PLAYER_PLAY)) {
                getBinder().play();
            } else if (action.equals(ACTION_PLAYER_PAUSE)) {
                getBinder().pause();
            } else if (action.equals(ACTION_PLAYER_NEXT)) {
                getBinder().next();
            } else if (action.equals(ACTION_PLAYER_STOP)) {
                getBinder().stop(true);
            } else if (action.equals(ACTION_FAVORITE_ON)) {
                getBinder().favoriteOn();
            } else if (action.equals(ACTION_FAVORITE_OFF)) {
                getBinder().favoriteOff();
            } else if (action.equals(ACTION_PLAYER_PLAY_PAUSE)) {
                if (getPlayer().getState() == JXPlayer.PlayerState.PLAYING) {
                    getBinder().pause();
                } else {
                    getBinder().play();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public abstract B makeBinder();

    public boolean isBound() {
        return mBound;
    }

    protected B getBinder() {
        if (mBinder == null)
            mBinder = makeBinder();
        return mBinder;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Logy.d(TAG, "onBind");
        mBound = true;
        return getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Logy.d(TAG, "onRebind");
        mBound = true;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logy.d(TAG, "onUnbind");
        mBound = false;
        return true;
    }

    private boolean mWasPlayingBeforeAudioLoss = false;

    private boolean isPlaying() {
        JXPlayer.PlayerState state = getPlayer().getState();
        return state == JXPlayer.PlayerState.PLAYING || state == JXPlayer.PlayerState.PREPARING;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Logy.d(TAG, "onAudioFocusChange:" + focusChange);
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            mWasPlayingBeforeAudioLoss = isPlaying();
            if (isPlaying())
                getBinder().pause(true);
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            if (mWasPlayingBeforeAudioLoss)
                getBinder().play(true);
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            mWasPlayingBeforeAudioLoss = false;
            getBinder().stop(true);
        }
    }

    @Override
    public void onDestroy() {
        Logy.d(TAG, "onDestroy()");
        unregisterReceiver(mHeadSetPlugReceiver);
        getBinder().stop(true);
        mMainThreadHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public Handler getMainThreadHandler() {
        return mMainThreadHandler;
    }

    protected JXTasker getTasker() {
        return mTasker;
    }

    protected JXPlayer getPlayer() {
        return mPlayer;
    }

    private JXPlayer.PlayerState mPreviousState = JXPlayer.PlayerState.STOPPED;

    @Override
    public void onNewPlayerState(@Nullable final JXObject object, @NonNull final JXPlayer.PlayerState state) {
        Logy.d(TAG, "New player state:" + state.name());
        if (mPreviousState == JXPlayer.PlayerState.STOPPED && state != JXPlayer.PlayerState.STOPPED) {
            int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Logy.d(TAG, "requestAudioFocus: success");
            } else {
                Logy.d(TAG, "requestAudioFocus: error");
            }
        } else if (mPreviousState != JXPlayer.PlayerState.STOPPED && state == JXPlayer.PlayerState.STOPPED) {
            int result = mAudioManager.abandonAudioFocus(this);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Logy.d(TAG, "abandonAudioFocus: success");
            } else {
                Logy.d(TAG, "abandonAudioFocus: error");
            }
        }
        mPreviousState = state;
    }

}
