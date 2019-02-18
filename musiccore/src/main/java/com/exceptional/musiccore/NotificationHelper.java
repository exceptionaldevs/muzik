package com.exceptional.musiccore;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.NotificationTarget;
import com.exceptional.musiccore.engine.JXPlayer;
import com.exceptional.musiccore.engine.JXPlayer.PlayerState;
import com.exceptional.musiccore.engine.metadata.DefaultMetaData;
import com.exceptional.musiccore.engine.metadata.DefaultMetaDataVisitor;
import com.exceptional.musiccore.engine.metadata.JXMetaFile;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.library.favorites.FavoritesHelper;
import com.exceptional.musiccore.utils.ApiHelper;

public abstract class NotificationHelper {
    public static final String INTENT_ACTION_NOTIFICATIONCLICK = "notification_click";
    public static final String INTENT_ACTION_COVERCLICK = "cover_click";

    private final static int NOTIFICATION_ID = 69;
    private final static int NOTIFICATION_PRIORITY = 1; // -2 to 2
    private final int mSrcPlayBlackIconDrawable;
    private final int mSrcPauseBlackIconDrawable;

    private int mSrcPlayWhiteIconDrawable;
    private int mSrcPauseWhiteIconDrawable;

    private MusicService<?> mService;
    private final LayoutConfig mLayoutConfig;
    private final NotificationCompat.Builder mNotificationBuilder;
    private final PendingIntent mPiStop;
    private final PendingIntent mPiNext;
    private final PendingIntent mPiPause;
    private final PendingIntent mPiPlay;
    private final PendingIntent mPiPrevious;
    private final PendingIntent mPiGoToQueue;
    private final PendingIntent mPiFavOn;
    private final PendingIntent mPiFavOff;

    public static class LayoutConfig {
        @LayoutRes public int layoutSmall;
        @LayoutRes public int layoutBig;
        @DrawableRes public int smallIcon;
        @DrawableRes public int drawablePlayDark;
        @DrawableRes public int drawablePauseDark;
        @DrawableRes public int drawablePlayLight;
        @DrawableRes public int drawablePauseLight;
        @DrawableRes public int drawableFavour;
        @DrawableRes public int drawableUnfavour;
        @ColorRes public int textColorPrimaryDark;
        @ColorRes public int textColorSecondaryDark;
        @ColorRes public int textColorPrimaryLight;
        @ColorRes public int textColorSecondaryLight;
    }

    public NotificationHelper(MusicService<?> service, Class<? extends MusicServiceActivity> mainActivityClass, LayoutConfig layoutConfig) {
        this.mService = service;
        mLayoutConfig = layoutConfig;

        final ComponentName srvComp = new ComponentName(getContext(), service.getClass());

        Intent previous = new Intent(MusicService.ACTION_PLAYER_PREVIOUS);
        previous.setComponent(srvComp);
        mPiPrevious = PendingIntent.getService(getContext(), 1, previous, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent play = new Intent(MusicService.ACTION_PLAYER_PLAY);
        play.setComponent(srvComp);
        mPiPlay = PendingIntent.getService(getContext(), 2, play, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pause = new Intent(MusicService.ACTION_PLAYER_PAUSE);
        pause.setComponent(srvComp);
        mPiPause = PendingIntent.getService(getContext(), 2, pause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent next = new Intent(MusicService.ACTION_PLAYER_NEXT);
        next.setComponent(srvComp);
        mPiNext = PendingIntent.getService(getContext(), 3, next, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stop = new Intent(MusicService.ACTION_PLAYER_STOP);
        stop.setComponent(srvComp);
        mPiStop = PendingIntent.getService(getContext(), 4, stop, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent fav_on = new Intent(MusicService.ACTION_FAVORITE_ON);
        fav_on.setComponent(srvComp);
        mPiFavOn = PendingIntent.getService(getContext(), 5, fav_on, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent fav_off = new Intent(MusicService.ACTION_FAVORITE_OFF);
        fav_off.setComponent(srvComp);
        mPiFavOff = PendingIntent.getService(getContext(), 6, fav_off, PendingIntent.FLAG_UPDATE_CURRENT);

        final Intent goToQueueIntent = new Intent();
        goToQueueIntent.setClass(getContext(), mainActivityClass);
        goToQueueIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        goToQueueIntent.putExtra("task", INTENT_ACTION_COVERCLICK);
        mPiGoToQueue = PendingIntent.getActivity(getContext(), 5, goToQueueIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Intent launchIntent = new Intent();
        launchIntent.setClass(getContext(), mainActivityClass);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        launchIntent.putExtra("task", INTENT_ACTION_NOTIFICATIONCLICK);
        final PendingIntent piLaunchIntent = PendingIntent.getActivity(getContext(), 6, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mNotificationBuilder = new NotificationCompat.Builder(getContext())
                .setPriority(NOTIFICATION_PRIORITY)
                .setSmallIcon(mLayoutConfig.smallIcon)
                .setContentIntent(piLaunchIntent)
                .setDeleteIntent(mPiStop);

        mSrcPlayWhiteIconDrawable = mLayoutConfig.drawablePlayLight;
        mSrcPauseWhiteIconDrawable = mLayoutConfig.drawablePauseLight;
//        if (ApiHelper.hasLolliPop()) {
//            mSrcPlayWhiteIconDrawable = R.drawable.ic_play_arrow_white_36dp;
//            mSrcPauseWhiteIconDrawable = R.drawable.ic_pause_white_36dp;
//        }
        mSrcPlayBlackIconDrawable = mLayoutConfig.drawablePlayDark;
        mSrcPauseBlackIconDrawable = mLayoutConfig.drawablePauseDark;
    }

    private Context getContext() {
        return mService.getApplicationContext();
    }

    private RemoteViews makeSmallNotification() {
        RemoteViews smallNotification = new RemoteViews(getContext().getPackageName(), mLayoutConfig.layoutSmall);
        smallNotification.setOnClickPendingIntent(R.id.notification_play, mPiPlay);
        if (ApiHelper.hasLolliPop()) {
            smallNotification.setImageViewResource(R.id.notification_play, mSrcPlayBlackIconDrawable);
        } else {
            smallNotification.setImageViewResource(R.id.notification_play, mSrcPlayWhiteIconDrawable);
        }
        smallNotification.setOnClickPendingIntent(R.id.notification_next, mPiNext);
        smallNotification.setOnClickPendingIntent(R.id.notification_cover, mPiGoToQueue);
        return smallNotification;
    }

    private RemoteViews makeBigNotification() {
        RemoteViews bigNotification = new RemoteViews(getContext().getPackageName(), mLayoutConfig.layoutBig);
        bigNotification.setOnClickPendingIntent(R.id.notification_previous, mPiPrevious);

        bigNotification.setOnClickPendingIntent(R.id.notification_play, mPiPlay);
        bigNotification.setImageViewResource(R.id.notification_play, mSrcPlayWhiteIconDrawable);

        bigNotification.setOnClickPendingIntent(R.id.notification_next, mPiNext);
        bigNotification.setOnClickPendingIntent(R.id.notification_stop, mPiStop);

        bigNotification.setOnClickPendingIntent(R.id.notification_cover, mPiGoToQueue);
        return bigNotification;
    }


    private void updateAlbumCover(@NonNull Notification notification, @NonNull JXMetaFile meta, @NonNull RemoteViews smallNotification, @Nullable RemoteViews bigNotification) {
        // Workaround for this: https://code.google.com/p/android/issues/detail?id=74967
//        if (cover != null) {
//            cover = cover.copy(Bitmap.Config.ARGB_8888, false);
//        }

        if (bigNotification != null) {
            Glide.with(getContext())
                    .asBitmap()
                    .load(CoverModelMetaVisitor.visitAcceptor(meta, getContext()).getModel())
                    .apply(RequestOptions.centerCropTransform())
                    .into(new NotificationTarget(getContext(), R.id.notification_cover, bigNotification, notification, NOTIFICATION_ID));
        }
        Glide.with(getContext())
                .asBitmap()
                .load(CoverModelMetaVisitor.visitAcceptor(meta, getContext()).getModel())
                .into(new NotificationTarget(getContext(), R.id.notification_cover, smallNotification, notification, NOTIFICATION_ID));

    }

    private void updateMetaData(@NonNull JXMetaFile meta, @NonNull RemoteViews smallNotification, @Nullable RemoteViews bigNotification) {
        DefaultMetaData notMeta = meta.accept(new DefaultMetaDataVisitor());
        smallNotification.setTextViewText(R.id.notification_title, notMeta.getTitle());
        smallNotification.setTextViewText(R.id.notification_artist, notMeta.getArtist());
        if (ApiHelper.hasLolliPop()) {
            smallNotification.setTextColor(R.id.notification_title, ContextCompat.getColor(getContext(), mLayoutConfig.textColorPrimaryDark));
            smallNotification.setTextColor(R.id.notification_artist, ContextCompat.getColor(getContext(), mLayoutConfig.textColorSecondaryDark));
        } else {
            smallNotification.setTextColor(R.id.notification_title, ContextCompat.getColor(getContext(), mLayoutConfig.textColorPrimaryLight));
            smallNotification.setTextColor(R.id.notification_artist, ContextCompat.getColor(getContext(), mLayoutConfig.textColorSecondaryLight));
        }

        if (bigNotification != null) {
            bigNotification.setTextViewText(R.id.notification_title, notMeta.getTitle());
            bigNotification.setTextViewText(R.id.notification_artist, notMeta.getArtist());
            bigNotification.setTextViewText(R.id.notification_album, notMeta.getAlbum());
        }
    }

    private void updateFavorite(JXMetaFile meta, boolean favorite, RemoteViews smallNotification, RemoteViews bigNotification) {
        if (bigNotification != null) {
            if (favorite) {
                // favored
                bigNotification.setOnClickPendingIntent(R.id.notification_heart, mPiFavOff);
                bigNotification.setImageViewResource(R.id.notification_heart, mLayoutConfig.drawableFavour);
            } else {
                // not favored
                bigNotification.setOnClickPendingIntent(R.id.notification_heart, mPiFavOn);
                bigNotification.setImageViewResource(R.id.notification_heart, mLayoutConfig.drawableUnfavour);
            }
        }
    }

    private void updatePlayerState(PlayerState state, RemoteViews smallNotification, RemoteViews bigNotification) {
        if (state == PlayerState.PLAYING) {
            smallNotification.setOnClickPendingIntent(R.id.notification_play, mPiPause);
            if (ApiHelper.hasLolliPop()) {
                smallNotification.setImageViewResource(R.id.notification_play, mSrcPauseBlackIconDrawable);
            } else {
                smallNotification.setImageViewResource(R.id.notification_play, mSrcPauseWhiteIconDrawable);
            }
        } else {
            smallNotification.setOnClickPendingIntent(R.id.notification_play, mPiPlay);
            if (ApiHelper.hasLolliPop()) {
                smallNotification.setImageViewResource(R.id.notification_play, mSrcPlayBlackIconDrawable);
            } else {
                smallNotification.setImageViewResource(R.id.notification_play, mSrcPlayWhiteIconDrawable);
            }
        }

        if (bigNotification != null) {
            if (state == PlayerState.PLAYING) {
                bigNotification.setOnClickPendingIntent(R.id.notification_play, mPiPause);
                bigNotification.setImageViewResource(R.id.notification_play, mSrcPauseWhiteIconDrawable);
            } else {
                bigNotification.setOnClickPendingIntent(R.id.notification_play, mPiPlay);
                bigNotification.setImageViewResource(R.id.notification_play, mSrcPlayWhiteIconDrawable);
            }
        }
    }

    private PlayerState mPreviousState = PlayerState.STOPPED;


    public void update(final JXPlayer.PlayerState state, final JXMetaFile meta) {
        if (meta == null || state == PlayerState.STOPPED) {
            mService.stopForeground(true);
            // If the notification is removed anyways, we don't need to do shit.
            return;
        } else if (state == PlayerState.PAUSED) {
            mService.stopForeground(false);
            // On pause we have to update the buttons.
            NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, makeNotification(state, meta));
        } else if (state == PlayerState.PLAYING && mPreviousState != PlayerState.PLAYING) {
            mService.startForeground(NOTIFICATION_ID, makeNotification(state, meta));
        } else {
            NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, makeNotification(state, meta));
        }

        mPreviousState = state;
    }


    private Notification makeNotification(PlayerState state, JXMetaFile meta) {
        RemoteViews smallNotification = makeSmallNotification();
        RemoteViews bigNotification = null;
        Notification notification = mNotificationBuilder.build();

        if (ApiHelper.hasLolliPop())
            bigNotification = makeBigNotification();

        updateMetaData(meta, smallNotification, bigNotification);
        updateAlbumCover(notification, meta, smallNotification, bigNotification);

        //TODO: make fav call async?
        updateFavorite(meta, FavoritesHelper.isFavorite(mService, meta.getLibrarySource()), smallNotification, bigNotification);
        updatePlayerState(state, smallNotification, bigNotification);

        notification.contentView = smallNotification;

        //TODO: we should figure out why exactly the notification is bad and then relax this, theoretically hasJellyBean() should work.
        if (ApiHelper.hasLolliPop())
            notification.bigContentView = bigNotification;
        if (ApiHelper.hasLolliPop())
            notification.visibility = Notification.VISIBILITY_PUBLIC;
        return notification;
    }


}
