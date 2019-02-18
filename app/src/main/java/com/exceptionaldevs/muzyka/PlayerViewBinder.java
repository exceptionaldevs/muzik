package com.exceptionaldevs.muzyka;

import android.app.Activity;
import android.view.View;

import com.bumptech.glide.Glide;
import com.exceptional.musiccore.BinderCustomer;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayer;
import com.exceptional.musiccore.engine.PlayerOptions;
import com.exceptional.musiccore.engine.metadata.JXMetaFile;
import com.exceptional.musiccore.engine.queue.Queue;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptionaldevs.muzyka.ui.widget.Miniplayer;
import com.exceptionaldevs.muzyka.ui.widget.PlayPauseButton;
import com.exceptionaldevs.muzyka.ui.widget.RepeatButton;
import com.exceptionaldevs.muzyka.ui.widget.SeekArc;
import com.exceptionaldevs.muzyka.ui.widget.ShuffleButton;
import com.exceptionaldevs.muzyka.utils.Logy;


public class PlayerViewBinder implements BinderCustomer<MusicBinder>, JXPlayer.PlayerStateListener, JXPlayer.PlayerProgressListener, Queue.JXQueueListener {
    private static final String TAG = "PlayerViewBinder";
    private final Miniplayer mMiniPlayer;
    private QueueRotationCall mQueueRotationCall;
    private OnFortuneClickListener mOnFortuneClick;
    private MusicBinder mBinder;
    private JXMetaFile boundTrack;
    private boolean mUpdateProgress = true;

    public PlayerViewBinder(Miniplayer miniPlayer) {
        mMiniPlayer = miniPlayer;
    }

    public PlayerViewBinder(Miniplayer miniPlayer, QueueRotationCall queueRotationCall) {
        mMiniPlayer = miniPlayer;
        mQueueRotationCall = queueRotationCall;
    }

    public OnFortuneClickListener getOnFortuneClick() {
        return mOnFortuneClick;
    }

    public void setOnFortuneClick(OnFortuneClickListener onFortuneClick) {
        mOnFortuneClick = onFortuneClick;
    }

    @Override
    public void onBinderAvailable(final MusicBinder binder) {
        mBinder = binder;
        binder.addPlayerStateListener(this);
        binder.addPlayerProgressListener(this);
        binder.getQueueHandler().addListener(this);

        mMiniPlayer.setClickListener(new Miniplayer.OnClickPlayButtons() {
            @Override
            public void onClickPlay(View v) {
                binder.togglePlayback();
            }

            @Override
            public void onClickFortune(View v) {
                if (getOnFortuneClick() != null) {
                    getOnFortuneClick().onClickFortune(v);
                }
            }

            @Override
            public void onClickForward(View v) {
                binder.next();
            }

            @Override
            public void onClickRewind(View v) {
                binder.previous();
            }

            @Override
            public void onClickRepeat(View v, RepeatButton.RepeatState rs) {
                if (rs == RepeatButton.RepeatState.NONE) {
                    binder.getQueueHandler().setRepeatMode(PlayerOptions.RepeatMode.NONE);
                } else if (rs == RepeatButton.RepeatState.ONE) {
                    binder.getQueueHandler().setRepeatMode(PlayerOptions.RepeatMode.TRACK);
                } else if (rs == RepeatButton.RepeatState.ALL) {
                    binder.getQueueHandler().setRepeatMode(PlayerOptions.RepeatMode.PLAYLIST);
                }
            }

            @Override
            public void onClickShuffle(View v, ShuffleButton.ShuffleState rs) {
                if (rs == ShuffleButton.ShuffleState.OFF) {
                    Logy.d(TAG, "Shuffle: NONE");
                    binder.getQueueHandler().setShuffleMode(PlayerOptions.ShuffleMode.NONE);
                } else if (rs == ShuffleButton.ShuffleState.ON) {
                    Logy.d(TAG, "Shuffle: RANDOM_TRACK");
                    binder.getQueueHandler().setShuffleMode(PlayerOptions.ShuffleMode.RANDOM_TRACK);
                }
            }
        });
        mMiniPlayer.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, long progress, boolean fromUser) {
                if (fromUser) {
                    float perc = (float) progress / seekArc.getMax();
                    binder.seek(perc);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                mUpdateProgress = false;
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                mUpdateProgress = true;
            }
        });
    }

    public void destroy() {
        if (mBinder != null) {
            mBinder.removePlayerStateListener(this);
            mBinder.removePlayerProgressListener(this);
            mBinder.getQueueHandler().removeListener(this);
        }
    }

    @Override
    public void onNewPlayerState(JXObject jxObject, JXPlayer.PlayerState state) {
        bindMiniplayerToCurrentState();
    }

    @Override
    public void onPlayerProgressChanged(JXObject jxObject, int playlistPosition, final long playbackPosition, long bufferPosition, final long maxDuration, boolean isBusy) {
        mMiniPlayer.post(new Runnable() {
            @Override
            public void run() {
                if (mUpdateProgress) {
                    mMiniPlayer.setProgress(playbackPosition);
                    if (mMiniPlayer.getMax() != maxDuration) {
                        mMiniPlayer.setMax(maxDuration);
                    }
                }
            }
        });
    }

    @Override
    public void onPlaylistDataChanged(Queue queue) {

    }

    /**
     * gets the state data from the binder/service
     * and updates all view related stuff
     *
     * @return true if binder was available and view updated, false if binder wasn't available
     */
    public boolean bindMiniplayerToCurrentState() {
        if (mBinder != null) {
            final RepeatButton.RepeatState rs = translateRepeatState(mBinder.getQueueHandler().getRepeatMode());
            final ShuffleButton.ShuffleState ss =
                    mBinder.getQueueHandler().getShuffleMode() == PlayerOptions.ShuffleMode.RANDOM_TRACK ?
                            ShuffleButton.ShuffleState.ON : ShuffleButton.ShuffleState.OFF;

            final boolean update;
            final JXMetaFile jxmf;
            final String artist;
            final String title;
            final String album;
            final boolean switchToFortuneWheel;
            JXPlayer player = mBinder.getPlayer();
            if (player.getCurrentTrack() != null) {
                jxmf = player.getCurrentTrack().getJXMetaFile();
                switchToFortuneWheel = false;
                if (player.getCurrentTrack().getJXMetaFile() != boundTrack) {
                    // bound track differs, update fields
                    artist = jxmf.getArtistName();
                    title = jxmf.getTrackName();
                    album = jxmf.getAlbumName();
                    update = true;
                    boundTrack = jxmf;
                } else {
                    // bound track is the same, do nothing on related fields
                    update = false;
                    artist = "";
                    title = "";
                    album = "";
                }
            } else if (!mBinder.getQueueHandler().isEmpty()) {
                //no current track, but queue is not empty, load first track
                jxmf = mBinder.getQueueHandler().get(0).getJXMetaFile();
                artist = jxmf.getArtistName();
                title = jxmf.getTrackName();
                album = jxmf.getAlbumName();
                update = true;
                boundTrack = jxmf;
                switchToFortuneWheel = false;
            } else {
                // no current track, reset/null fields
                artist = "";
                title = "";
                album = "";
                jxmf = null;
                update = true;
                boundTrack = null;
                switchToFortuneWheel = true;
            }

            final long duration = jxmf != null ? mBinder.getPlayer().getDuration() : 100;
            final long position = jxmf != null ? mBinder.getPlayer().getPlayPosition() : 0;
            final boolean isPlaying = mBinder.getPlayer().getState() == JXPlayer.PlayerState.PLAYING;
            final PlayPauseButton.PlayState ps = !isPlaying ? PlayPauseButton.PlayState.PLAY : PlayPauseButton.PlayState.PAUSE;

            mMiniPlayer.post(new Runnable() {
                @Override
                public void run() {
                    Activity host = (Activity) mMiniPlayer.getContext();
                    if (host.isDestroyed())
                        return;
                    if (jxmf != null && update) { // if current track and differs: update, if no current track do nothing
                        Glide.with(host)
                                .load(CoverModelMetaVisitor.visitAcceptor(jxmf, host).getModel())
                                .into(mMiniPlayer.getCoverView());
                    }

                    if (switchToFortuneWheel && !mMiniPlayer.isFortuneMode()) {
                        mMiniPlayer.switchToFortuneWheelIcon(true);
                        mMiniPlayer.getCoverView().setRotation(0);
                        Glide.with(host)
                                .load(R.drawable.material_colors_stock)
                                .into(mMiniPlayer.getCoverView());
                    } else if (mMiniPlayer.isFortuneMode()) {
                        mMiniPlayer.switchToFortuneWheelIcon(false);
                    }

                    if (update) {
                        mMiniPlayer.setTextFields(artist, title, album);
                    }

                    mMiniPlayer.getSeekArc().setMax(duration);
                    mMiniPlayer.getSeekArc().setProgress(position);

                    if (mQueueRotationCall != null) {
                        if (mQueueRotationCall.shouldQueueRotationCall()) {
                            mQueueRotationCall.queueRotationCall(new Runnable() {
                                @Override
                                public void run() {
                                    mMiniPlayer.getHeartView().setPlaying(isPlaying);
                                }
                            });
                        } else {
                            mMiniPlayer.getHeartView().setPlaying(isPlaying);
                        }
                    } else {
                        mMiniPlayer.getHeartView().setPlaying(isPlaying);
                    }

                    mMiniPlayer.marqueeText(isPlaying);

                    mMiniPlayer.setRepeatState(rs);
                    mMiniPlayer.setShuffleState(ss);
                    mMiniPlayer.setPlayState(ps);
                }
            });
            return true;
        }

        return false;
    }

    private RepeatButton.RepeatState translateRepeatState(PlayerOptions.RepeatMode rm) {
        if (rm == PlayerOptions.RepeatMode.PLAYLIST)
            return RepeatButton.RepeatState.ALL;
        if (rm == PlayerOptions.RepeatMode.TRACK)
            return RepeatButton.RepeatState.ONE;
        return RepeatButton.RepeatState.NONE;
    }


}