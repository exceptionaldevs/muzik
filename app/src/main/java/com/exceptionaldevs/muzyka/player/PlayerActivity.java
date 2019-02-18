package com.exceptionaldevs.muzyka.player;

import android.animation.Animator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;

import com.exceptional.musiccore.BinderCustomer;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXPlayer;
import com.exceptional.musiccore.engine.queue.Queue;
import com.exceptional.musiccore.engine.queuetasks.JXSnackTask;
import com.exceptional.musiccore.engine.queuetasks.JXTask;
import com.exceptional.musiccore.library.favorites.FavoritesHelper;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.OnFortuneClickListener;
import com.exceptionaldevs.muzyka.PlayerViewBinder;
import com.exceptionaldevs.muzyka.QueueRotationCall;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.AddToPlaylistDialog;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.settings.SettingsActivity;
import com.exceptionaldevs.muzyka.ui.widget.ElasticDragDismissFrameLayout;
import com.exceptionaldevs.muzyka.ui.widget.Miniplayer;
import com.exceptionaldevs.muzyka.ui.widget.ObservableLinearLayoutManager;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.MultiItemSelector;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMRecyclerView;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.drag.DragCallback;
import com.exceptionaldevs.muzyka.utils.AnimatorListener;
import com.exceptionaldevs.muzyka.utils.QueueHelper;

import java.util.List;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayerActivity extends MuzikServiceActivity implements
        QueueRotationCall,
        LoaderManager.LoaderCallbacks<List<Track>>,
        DragCallback,
        SDMRecyclerView.OnItemClickListener,
        SDMRecyclerView.OnItemLongClickListener, JXTask.JXTaskCallback {
    private static final int REQUEST_FORTUNEWHEEL = 1337;
    public static final String EXTRA_COVER_ROTATION = "EXTRA_COVER_ROTATION";
    @BindView(R.id.likeTrack) Button mLikeTrack;
    @BindView(R.id.createPlaylist) Button mCreatePlaylist;
    @BindView(R.id.clearQueue) Button mClearQueue;
    @BindView(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.player) Miniplayer mMiniplayer;
    @BindView(R.id.showPlaylistBtn) ImageButton mExitPlaylistButton;
    @BindDimen(R.dimen.z_app_bar) float appBarElevation;
    Interpolator fastOutSlowIn;
    private PlayerViewBinder mPlayerViewBinder;
    private int mDragState;
    private boolean mStoppedRotation;
    private Runnable mQueuedRotationCall;
    private float coverRotation;
    private JXPlayer.PlayerStateListener mFavoritesPlayerStateListener;
    private Queue.JXQueueListener mQueueListener;

    public ContentAdapter<Track, ? extends SDMViewHolder> getAdapter() {
        return (ContentAdapter<Track, ? extends SDMViewHolder>) getRecyclerView().getAdapter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);

        coverRotation = getIntent().getFloatExtra(EXTRA_COVER_ROTATION, 0);
        mMiniplayer.post(new Runnable() {
            @Override
            public void run() {
                mMiniplayer.getCoverView().setRotation(coverRotation);
            }
        });

        fastOutSlowIn = AnimationUtils.loadInterpolator(PlayerActivity.this, android.R.interpolator.fast_out_slow_in);

        setupRecyclerView(mRecyclerView);

        mExitPlaylistButton.setImageResource(R.drawable.ic_close_blue_grey_800_24dp);
        mExitPlaylistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int midX = mExitPlaylistButton.getWidth() / 2;
                int midY = mExitPlaylistButton.getHeight() / 2;
                mExitPlaylistButton.setPivotX(midX);
                mExitPlaylistButton.setPivotY(midY);
                mExitPlaylistButton.animate()
                        .rotation(360)
                        .scaleX(0.3f)
                        .scaleY(0.3f)
                        .setInterpolator(fastOutSlowIn);
                finishAfterTransition();

            }
        });

        mMiniplayer.marqueeText(true);

        postponeEnterTransition();
        mMiniplayer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mMiniplayer.getViewTreeObserver().removeOnPreDrawListener(this);
                mExitPlaylistButton.animate()
                        .setDuration(650)
                        .rotation(360)
                        .setInterpolator(fastOutSlowIn);

                startPostponedEnterTransition();
                return true;
            }
        });

        draggableFrame.addListener(
                new ElasticDragDismissFrameLayout.SystemChromeFader(getWindow()) {
                    boolean switched = false;

                    @Override
                    public void onDragDismissed() {
                        finishAfterTransition();
                    }

                    @Override
                    public void onDrag(float elasticOffset, float elasticOffsetPixels, float rawOffset, float rawOffsetPixels) {
                        final float interpolatedRawOffset = fastOutSlowIn.getInterpolation(rawOffset);
                        mExitPlaylistButton.setRotation(359 * interpolatedRawOffset);
                        final int midX = mExitPlaylistButton.getWidth() / 2;
                        final int midY = mExitPlaylistButton.getHeight() / 2;
                        mExitPlaylistButton.setPivotX(midX * interpolatedRawOffset);
                        mExitPlaylistButton.setPivotY(midY + midY * interpolatedRawOffset);

                        switchResource(mExitPlaylistButton, rawOffset);

                        final float perc = (1 - rawOffset);
                        final float scale = perc < 0.5 ? -2 * perc + 1 : 2 * (perc - 0.5f);

                        mExitPlaylistButton.setScaleX(scale);
                        mExitPlaylistButton.setScaleY(scale);
                    }

                    private void switchResource(ImageButton exitPlaylistBtn, float offset) {
                        if (offset <= 0.5f && !switched) {
                            exitPlaylistBtn.setImageResource(R.drawable.ic_close_blue_grey_800_24dp);
                            switched = true;
                        }
                        if (offset > 0.5f && switched) {
                            exitPlaylistBtn.setImageResource(R.drawable.ic_playlist);
                            switched = false;
                        }
                    }
                });

        mPlayerViewBinder = new PlayerViewBinder(mMiniplayer, this);
        mPlayerViewBinder.setOnFortuneClick(new OnFortuneClickListener() {
            @Override
            public void onClickFortune(View v) {
                mMiniplayer.getHeartView().getPlayPauseFab()
                        .setTransitionName(getResources().getString(R.string.transition_fortunewheel_fab));
                mMiniplayer.getHeartView().getCoverView()
                        .setTransitionName(getResources().getString(R.string.transition_fortunewheel_wheel));
                Pair<View, String> p1 = Pair.create((View) mMiniplayer.getHeartView().getCoverView(),
                        getResources().getString(R.string.transition_fortunewheel_wheel));
                Pair<View, String> p2 = Pair.create((View) mMiniplayer.getHeartView().getPlayPauseFab(),
                        getResources().getString(R.string.transition_fortunewheel_fab));

                // return transition screws up if we dont stop and start the rotation manually
                mStoppedRotation = true;
                mMiniplayer.getHeartView().setPlaying(false);
                startActivityForResult(new Intent(PlayerActivity.this, FortuneWheelActivity.class), REQUEST_FORTUNEWHEEL,
                        ActivityOptions.makeSceneTransitionAnimation(PlayerActivity.this, p1, p2).toBundle());
            }

            @Override
            public boolean onTouchFortune(View v, MotionEvent event) {
                return false;
            }
        });
        registerBinderCustomer(mPlayerViewBinder);

        registerBinderCustomer(new BinderCustomer<MusicBinder>() {
            @Override
            public void onBinderAvailable(MusicBinder binder) {
                // TODO crashed at some point because not attached to activity? race condition between app start and immediately finish?
                getSupportLoaderManager().initLoader(200, null, PlayerActivity.this);
            }
        });
        mMiniplayer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mMiniplayer.getViewTreeObserver().removeOnPreDrawListener(this);
                boolean result = mPlayerViewBinder.bindMiniplayerToCurrentState();
                if (result == false) {
                    registerBinderCustomer(new BinderCustomer<MusicBinder>() {
                        @Override
                        public void onBinderAvailable(MusicBinder binder) {
                            mPlayerViewBinder.bindMiniplayerToCurrentState();
                        }
                    });
                }
                return true; // yeah, please draw everything
            }
        });

        enableOrDisableThisView(mLikeTrack, false);
        registerBinderCustomer(new BinderCustomer<MusicBinder>() {
            @Override
            public void onBinderAvailable(MusicBinder binder) {
                mFavoritesPlayerStateListener = new JXPlayer.PlayerStateListener() {
                    @Override
                    public void onNewPlayerState(final JXObject jxObject, JXPlayer.PlayerState state) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                enableOrDisableThisView(mLikeTrack, jxObject != null);
                                mLikeTrack.setCompoundDrawablesWithIntrinsicBounds(0, FavoritesHelper.isFavorite(getApplicationContext(), jxObject) ? R.drawable.ic_favorite_blue_grey_400_24dp : R.drawable.ic_favorite_border_blue_grey_400_24dp, 0, 0);
                            }
                        });
                    }
                };
                binder.addPlayerStateListener(mFavoritesPlayerStateListener);
                enableOrDisableThisView(mCreatePlaylist, !getBinder().getQueueHandler().isEmpty());
                enableOrDisableThisView(mClearQueue, !getBinder().getQueueHandler().isEmpty());
                mQueueListener = new Queue.JXQueueListener() {
                    @Override
                    public void onPlaylistDataChanged(final Queue queue) {
                        mCreatePlaylist.post(new Runnable() {
                            @Override
                            public void run() {
                                enableOrDisableThisView(mCreatePlaylist, !queue.isEmpty());
                                enableOrDisableThisView(mClearQueue, !queue.isEmpty());
                            }
                        });
                    }
                };
                binder.getQueueHandler().addListener(mQueueListener);
            }
        });
    }

    private void enableOrDisableThisView(final View view, boolean enabled) {
        if (((View) view.getParent()).isLaidOut()) {
            view.setEnabled(enabled);
            view.animate()
                    .alpha(enabled ? 1f : 0.7f)
                    .setDuration(800l)
                    .setInterpolator(fastOutSlowIn)
                    .setListener(new AnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.animate().setListener(null);
                        }
                    });
        } else {
            view.setEnabled(enabled);
            view.setAlpha(enabled ? 1f : 0.7f);
        }

    }

    @Override
    public void onDestroy() {
        if (getBinder() != null) {
            if (mFavoritesPlayerStateListener != null) {
                getBinder().removePlayerStateListener(mFavoritesPlayerStateListener);
                mFavoritesPlayerStateListener = null;
            }
            if (mQueueListener != null) {
                getBinder().getQueueHandler().removeListener(mQueueListener);
                mQueueListener = null;
            }
        }
        mPlayerViewBinder.destroy();
        super.onDestroy();
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        ObservableLinearLayoutManager layoutManager = new ObservableLinearLayoutManager(this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        ((SDMRecyclerView) recyclerView).setOnItemClickListener(this);
        ((SDMRecyclerView) recyclerView).setOnItemLongClickListener(this);
        ((SDMRecyclerView) recyclerView).setOnItemDragListener(this);
        ((SDMRecyclerView) recyclerView).setChoiceMode(MultiItemSelector.ChoiceMode.MULTIPLE);
    }

    private void animateMiniplayerTranslationZ(float z) {
        mMiniplayer.animate()
                .translationZ(z)
                .setStartDelay(0L)
                .setDuration(80L)
                .setInterpolator(fastOutSlowIn)
                .start();
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public Queue getPlaylistHandler() {
        return getBinder().getQueueHandler();
    }

    @Override
    public Loader<List<Track>> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case 200: {
                return new PlayerQueueLoader(this, getPlaylistHandler());
            }
            default: {
                throw new RuntimeException("How did we get here?");
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Track>> loader, List<Track> data) {
        switch (loader.getId()) {
            case 200: {
                if (mDragState == DRAG_STATE_IDLE)
                    displayData(data);
                break;
            }
            default: {
                throw new RuntimeException("How did we get here?");
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Track>> loader) {
        switch (loader.getId()) {
            case 200: {
                PlayerPlaylistAdapter adapter = (PlayerPlaylistAdapter) getRecyclerView().getAdapter();
                if (adapter != null) {
                    getBinder().removePlayerProgressListener(adapter);
                    adapter.setData(null);
                    adapter.notifyDataSetChanged();
                }
                break;
            }
            default: {
                throw new RuntimeException("How did we get here?");
            }
        }
    }

    public void displayData(List<Track> data) {
        PlayerPlaylistAdapter adapter = (PlayerPlaylistAdapter) getRecyclerView().getAdapter();
        boolean newAdapter = adapter == null;
        if (newAdapter)
//            adapter = new PlayerPlaylistAdapter(this, getRecyclerView().getMultiItemSelector());
            adapter = new PlayerPlaylistAdapter(this, null);

        adapter.setData(data);
        if (newAdapter) {
            getRecyclerView().setAdapter(adapter);
            getBinder().addPlayerProgressListener(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
//        if (getRecyclerView().getActionMode() != null)
//            getRecyclerView().getActionMode().invalidate();

//        if(!(data.size() > 0)){
//            mSplash.setVisibility(View.VISIBLE);
//        }else{
//            mSplash.setVisibility(View.GONE);
//        }

    }

    @Override
    public boolean canDrag(int position) {
        return true;
    }

    @Override
    public boolean onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int adapterFrom = viewHolder.getAdapterPosition();
        int adapterTo = target.getAdapterPosition();

        getAdapter().swapItems(adapterFrom, adapterTo);
        getAdapter().notifyItemMoved(adapterFrom, adapterTo);

        getPlaylistHandler().move(adapterFrom, adapterTo, false);
        return true;
    }

    @DRAGSTATE
    private int mPreviousDragState = DRAG_STATE_IDLE;

    @Override
    public void onDragStateChanged(@DRAGSTATE int dragState) {
        if (dragState == DRAG_STATE_IDLE && mPreviousDragState != DRAG_STATE_IDLE) {
            getPlaylistHandler().publicNotifyDataChanged();
        }
        mPreviousDragState = dragState;
    }

    @Override
    public boolean onRecyclerItemClick(RecyclerView parent, View view, int position, long id) {
        if (getBinder() != null) {
            if (getBinder().getQueueHandler().get(position) != null) {
                getBinder().play(position);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onRecyclerItemLongClick(RecyclerView parent, View view, int position, long id) {
        if (getBinder() != null)
            getBinder().getQueueHandler().remove(position);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayerViewBinder != null) {
            mPlayerViewBinder.bindMiniplayerToCurrentState();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FORTUNEWHEEL) {
            // cosmetic only, return transition screws up if we dont stop and start the rotation manually
            // we dont need to save this in the instance state bundle, as this should at most just look bad
            mStoppedRotation = false;
            if (mQueuedRotationCall != null) {
                mQueuedRotationCall.run();
            }
            mQueuedRotationCall = null;
        }
    }


    @Override
    public boolean shouldQueueRotationCall() {
        return mStoppedRotation;
    }

    @Override
    public void queueRotationCall(Runnable run) {
        mQueuedRotationCall = run;
    }

    @OnClick(R.id.startFortuneWheel)
    void onClickFortune(View v) {
        mMiniplayer.getHeartView().getPlayPauseFab()
                .setTransitionName(getResources().getString(R.string.transition_fortunewheel_fab));
        mMiniplayer.getHeartView().getCoverView()
                .setTransitionName(getResources().getString(R.string.transition_fortunewheel_wheel));
        Pair<View, String> p1 = Pair.create((View) mMiniplayer.getHeartView().getCoverView(),
                getResources().getString(R.string.transition_fortunewheel_wheel));
        Pair<View, String> p2 = Pair.create((View) mMiniplayer.getHeartView().getPlayPauseFab(),
                getResources().getString(R.string.transition_fortunewheel_fab));

        // return transition screws up if we dont stop and start the rotation manually
        mStoppedRotation = true;
        mMiniplayer.getHeartView().setPlaying(false);
        startActivityForResult(new Intent(PlayerActivity.this, FortuneWheelActivity.class), REQUEST_FORTUNEWHEEL,
                ActivityOptions.makeSceneTransitionAnimation(PlayerActivity.this, p1, p2).toBundle());
    }

    @OnClick(R.id.likeTrack)
    void onClickLikeTrack(View v) {
        JXObject current = getBinder().getPlayer().getCurrentTrack();
        if (current == null)
            return;
        if (FavoritesHelper.isFavorite(getApplicationContext(), current)) {
            getBinder().favoriteOff();
            mLikeTrack.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_border_blue_grey_400_24dp, 0, 0);
        } else {
            getBinder().favoriteOn();
            mLikeTrack.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_blue_grey_400_24dp, 0, 0);
        }
    }

    @OnClick(R.id.createPlaylist)
    void onClickCreatePlaylist(View v) {
        AddToPlaylistDialog atpDialog = AddToPlaylistDialog.instantiateFromTracks(getAdapter().getData());
        atpDialog.show(getSupportFragmentManager(), AddToPlaylistDialog.class.getSimpleName());
    }

    @OnClick(R.id.clearQueue)
    void onClickClearQueue(View v) {
        if (getBinder() != null) {
            QueueHelper.clearQueue(getBinder(), this, "Queue cleared.", getString(R.string.button_undo));
        }
    }

    @Override
    public void onTaskDone(final JXTask task) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (task instanceof JXSnackTask) {
                    if (isFinishing() || getBinder() == null)
                        return;
                    final JXSnackTask snackTask = (JXSnackTask) task;
                    Snackbar.make(findViewById(R.id.content), snackTask.getSnackMessage(), Snackbar.LENGTH_LONG)
                            .setAction(snackTask.getSnackActionName(), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getBinder().getTasker().execute(snackTask.getSnackTask(), null);
                                }
                            })
                            .show();
                }
            }
        });
    }

    @OnClick(R.id.showSettingsBtn)
    void onClickSettings(View v) {
        startActivity(new Intent(PlayerActivity.this, SettingsActivity.class));
    }


}
