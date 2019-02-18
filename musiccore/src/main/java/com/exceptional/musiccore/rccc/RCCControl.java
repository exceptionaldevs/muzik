package com.exceptional.musiccore.rccc;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.MusicService;
import com.exceptional.musiccore.engine.JXPlayer;
import com.exceptional.musiccore.engine.metadata.JXMetaFile;
import com.exceptional.musiccore.engine.metadata.MetaFileOnlyVIsitor;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.utils.Logy;

/**
 * Created by darken on 15.05.14.
 * Be careful with album art: https://code.google.com/p/android/issues/detail?id=74967
 */
public class RCCControl {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "RCCControl";
    private final MusicService mService;
    private JXMetaFile mCurrentMeta;
    private JXPlayer.PlayerState mCurrentPlayingState;
    //FIXME RemoteControlClient deprecated
    private RemoteControlClient mRemote;
    private AudioManager mAudioManager;
    private ComponentName mRemoteControlReceiverCP;

    public RCCControl(MusicService service) {
        mService = service;
        mRemoteControlReceiverCP = new ComponentName(mService.getPackageName(), RemoteControlReceiver.class.getName());
        mAudioManager = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);
    }

    public void start() {
        Logy.d(TAG, "Register...");
        if (mRemote == null) {
            mAudioManager.registerMediaButtonEventReceiver(mRemoteControlReceiverCP);
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mRemoteControlReceiverCP);
            mRemote = new RemoteControlClient(PendingIntent.getBroadcast(mService /*context*/, 0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
            mAudioManager.registerRemoteControlClient(mRemote);
            mRemote.setPlaybackState(
                    RemoteControlClient.PLAYSTATE_STOPPED);
            mRemote.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
                            RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP);
        }
    }

    public void stop() {
        Logy.d(TAG, "unregister...");
        if (mRemote != null) {
            mAudioManager.unregisterRemoteControlClient(mRemote);
            mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlReceiverCP);
            mRemote = null;
        }
    }


    private SimpleTarget<Bitmap> mCoverTarget;

    public void update(JXPlayer.PlayerState state, JXMetaFile meta) {
        if (mRemote == null)
            return;
        mCurrentPlayingState = state;
        mCurrentMeta = meta;

        updatePlayingState();

        if (mCurrentMeta != null) {
            updateMetaData();
            updateAlbumCover(null);

            if (mCoverTarget != null) {
                Glide.with(mService).clear(mCoverTarget);
            }
            // TODO check loaded image size.
            mCoverTarget = new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    updateAlbumCover(resource);
                }
            };

            Glide.with(mService)
                    .asBitmap()
                    .load(CoverModelMetaVisitor.visitAcceptor(mCurrentMeta, mService).getModel())
                    .into(mCoverTarget);
        }
    }

    private void updatePlayingState() {
        if (mCurrentPlayingState == JXPlayer.PlayerState.PLAYING) {
            mRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        } else if (mCurrentPlayingState == JXPlayer.PlayerState.PAUSED) {
            mRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        } else if (mCurrentPlayingState == JXPlayer.PlayerState.STOPPED) {
            mRemote.setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
        }
    }

    private void updateMetaData() {
        BluetoothMetaData btMeta = mCurrentMeta.accept(new BluetoothMetaDataVisitor());
        mRemote.editMetadata(false)
                .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, btMeta.getArtist())
                .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, btMeta.getAlbum())
                .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, btMeta.getTitle())
                .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, btMeta.getDuration())
                .apply();
    }

    private void updateAlbumCover(Bitmap cover) {
        if (mRemote != null) {
            if (cover != null)
                cover = cover.copy(cover.getConfig(), cover.isMutable());
            mRemote.editMetadata(false)
                    .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, cover)
                    .apply();
        }
    }

    private static class BluetoothMetaData {
        private int mId;
        private String mArtist;
        private String mAlbum;
        private String mTitle;
        private int mPlaylistSize;
        private int mDuration;
        private int mCurrentPosition;

        public int getId() {
            return mId;
        }

        public void setId(int id) {
            mId = id;
        }

        public String getArtist() {
            return mArtist;
        }

        public void setArtist(String artist) {
            mArtist = artist;
        }

        public String getAlbum() {
            return mAlbum;
        }

        public void setAlbum(String album) {
            mAlbum = album;
        }

        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
            mTitle = title;
        }

        public int getPlaylistSize() {
            return mPlaylistSize;
        }

        public void setPlaylistSize(int playlistSize) {
            mPlaylistSize = playlistSize;
        }

        public int getCurrentPosition() {
            return mCurrentPosition;
        }

        public void setCurrentPosition(int currentPosition) {
            mCurrentPosition = currentPosition;
        }

        public int getDuration() {
            return mDuration;
        }

        public void setDuration(int duration) {
            mDuration = duration;
        }
    }

    private static class BluetoothMetaDataVisitor extends MetaFileOnlyVIsitor<BluetoothMetaData> {
        @Override
        public BluetoothMetaData visit(JXMetaFile jxMetaFile) {
            BluetoothMetaData meta = new BluetoothMetaData();
            meta.setTitle(jxMetaFile.getTrackName());
            meta.setArtist(jxMetaFile.getArtistName());
            meta.setAlbum(jxMetaFile.getAlbumName());
            meta.setDuration((int) jxMetaFile.getTrackDuration());
            return meta;
        }

    }
}
