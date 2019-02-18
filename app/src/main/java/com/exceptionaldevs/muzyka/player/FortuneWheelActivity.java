package com.exceptionaldevs.muzyka.player;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.exceptional.musiccore.BinderCustomer;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.engine.queue.Queue;
import com.exceptional.musiccore.engine.queuetasks.JXTask;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.glide.palette.PaletteBitmap;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.library.tracks.TrackLoader;
import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.tabs.ContentFragment;
import com.exceptionaldevs.muzyka.ui.widget.CircleImageView;
import com.exceptionaldevs.muzyka.ui.widget.ElasticDragDismissFrameLayout;
import com.exceptionaldevs.muzyka.ui.widget.FABToggle;
import com.exceptionaldevs.muzyka.ui.widget.fortunewheel.FortuneWheel;
import com.exceptionaldevs.muzyka.ui.widget.fortunewheel.PiePieceDrawable;
import com.exceptionaldevs.muzyka.ui.widget.fortunewheel.PieView;
import com.exceptionaldevs.muzyka.utils.AnimatorListener;
import com.exceptionaldevs.muzyka.utils.ColorUtils;
import com.exceptionaldevs.muzyka.utils.QueueHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class FortuneWheelActivity extends MuzikServiceActivity implements
        LoaderManager.LoaderCallbacks<List<Track>>, JXTask.JXTaskCallback {
    static final String STATE_WINNER_TRACK = "winner_track";
    @BindView(R.id.draggable_frame) ElasticDragDismissFrameLayout draggableFrame;
    @BindView(R.id.fortunewheel) FortuneWheel mFortuneWheel;
    @BindView(R.id.fortune_artist) TextView mArtistTextView;
    @BindView(R.id.fortune_track) TextView mTrackTextView;
    @BindView(R.id.again) TextView mAgain;
    FABToggle mFortuneWheelFab;

    private List<Track> mAllTracks;
    private List<Track> mRandomTracks;
    private Track mWinnerTrack;
    private List<Integer> mRandomTrackPos;
    private boolean isDismissing;
    private AnimatorSet slowDismissAnimation;
    private Animator fortuneFabAnimator;
    private boolean mAgainClicked;
    private boolean mTrackPlays;

    private AnimatorSet createDismissAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        int dismissHeight = getResources().getDimensionPixelSize(R.dimen.fab_size);
        Animator a1 = ObjectAnimator.ofPropertyValuesHolder(mArtistTextView,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dismissHeight));
        Animator a2 = ObjectAnimator.ofPropertyValuesHolder(mTrackTextView,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dismissHeight));
        Animator a3 = ObjectAnimator.ofPropertyValuesHolder(mFortuneWheel,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, dismissHeight));
        animatorSet.playTogether(a1, a2, a3);
        animatorSet.setStartDelay(200);
        animatorSet.setDuration(1500l);
        animatorSet.setInterpolator(new AccelerateInterpolator(1.5f));
        return animatorSet;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fortunewheel);
        ButterKnife.bind(this);

        resetTextFields();

        if (savedInstanceState != null) {
            mWinnerTrack = savedInstanceState.getParcelable(STATE_WINNER_TRACK);
        }

        postponeEnterTransition();
        mFortuneWheel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mFortuneWheel.getViewTreeObserver().removeOnPreDrawListener(this);
                mFortuneWheel.performAnimation();
                startPostponedEnterTransition();
                return true;
            }
        });
        mFortuneWheelFab = (FABToggle) mFortuneWheel.findViewById(R.id.fab);

        mFortuneWheel.setDotsListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Code this very defensively, animation callbacks are racecondition hell
                if (getBinder() != null && mRandomTracks != null && mWinnerTrack != null) {
                    List<Uri> uris = new ArrayList<>();
                    for (int i = 0; i < mRandomTracks.size(); i++) {
                        if (i == 0) {
                            uris.add(mWinnerTrack.getUriForTracks()); // workaround to handle activity lifecycle
                        } else {
                            uris.add(mRandomTracks.get(i).getUriForTracks());
                        }
                    }
                    QueueHelper.replaceQueue(FortuneWheelActivity.this, getBinder(), FortuneWheelActivity.this, uris, "Random Tracks added to queue", getString(R.string.button_undo));
                    mTrackTextView.setText(mWinnerTrack.getTitle());
                    mArtistTextView.setText(mWinnerTrack.getArtistName());

                    viewEnterAnimation(mTrackTextView, 20);
                    viewEnterAnimation(mArtistTextView, 27);
                    viewEnterAnimation(mAgain, -10);
                } else {
                    resetTextFields();
                }
            }
        });

        mFortuneWheel.setAllListener(new AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                slowDismissAnimation.start();
            }
        });

        draggableFrame.addListener(
                new ElasticDragDismissFrameLayout.SystemChromeFader(getWindow()) {
                    @Override
                    public void onDragDismissed() {
                        finishAfterTransition();
                    }

                    @Override
                    public void onDrag(float elasticOffset, float elasticOffsetPixels, float rawOffset, float rawOffsetPixels) {

                    }

                });

        registerBinderCustomer(new BinderCustomer<MusicBinder>() {
            @Override
            public void onBinderAvailable(MusicBinder binder) {
                getSupportLoaderManager()
                        .restartLoader(ContentFragment.LOADER_ID_TRACKS, getLoaderArgs().toBundle(), FortuneWheelActivity.this);
            }
        });

        slowDismissAnimation = createDismissAnimation();
        slowDismissAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAgainClicked = false;
                fortuneFabAnimator.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mAgainClicked) {
                    dismiss();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mFortuneWheel.animate().translationY(0).setDuration(200).start();
                mArtistTextView.animate().translationY(0).setDuration(200).start();
                mTrackTextView.animate().translationY(0).setDuration(200).start();
                fortuneFabAnimator.cancel();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        fortuneFabAnimator = ObjectAnimator.ofPropertyValuesHolder(mFortuneWheelFab,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f));
        fortuneFabAnimator.setStartDelay(600L);
        fortuneFabAnimator.setDuration(200L);
        fortuneFabAnimator.setInterpolator(AnimationUtils.loadInterpolator(this,
                android.R.interpolator.linear_out_slow_in));
        fortuneFabAnimator.addListener(new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFortuneWheelFab.setVisibility(View.VISIBLE);
            }
        });
    }

    private void viewEnterAnimation(View view, float offset) {
        view.setTranslationY(offset);
        view.setAlpha(0.3f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(1000)
                .setInterpolator(AnimationUtils.loadInterpolator(this,
                        android.R.interpolator.linear_out_slow_in))
                .setListener(null)
                .start();
    }

    private void resetTextFields() {
        if (!mArtistTextView.isLaidOut()) {
            mArtistTextView.setAlpha(0);
            mTrackTextView.setAlpha(0);
        } else {
            mArtistTextView.animate()
                    .alpha(0)
                    .setDuration(300)
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.linear_out_slow_in))
                    .setListener(null)
                    .start();
            mTrackTextView.animate()
                    .alpha(0)
                    .setDuration(300)
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.linear_out_slow_in))
                    .setListener(null)
                    .start();
        }
    }

    public LoaderArgs getLoaderArgs() {
        return new LoaderArgs.Builder().forUri(Track.getBaseUri()).build();
    }

    public Queue getPlaylistHandler() {
        return getBinder().getQueueHandler();
    }

    @Override
    public Loader<List<Track>> onCreateLoader(int id, Bundle args) {
        Loader<List<Track>> loader = new TrackLoader(this, args);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<Track>> loader, List<Track> data) {
        mAllTracks = data;
        if (mWinnerTrack == null && !isDismissing) {
            fillRandomTracks();
        }
    }

    private void fillRandomTracks() {
        mRandomTracks = new ArrayList<>();
        mRandomTrackPos = new ArrayList<>();
        if (mAllTracks.size() == 0) {
            finish();
            return;
        }
        if (mAllTracks.size() == 1) {
            getBinder().play(mAllTracks.get(0).getJXObject());
            finish();
            return;
        }
        if (mAllTracks.size() <= 10) {
            mRandomTracks.addAll(mAllTracks);
        }
        Random random = new Random();
        while (mRandomTracks.size() < 10) {
            int randomPos = (int) (random.nextFloat() * mAllTracks.size());
            if (mRandomTrackPos.indexOf(randomPos) == -1) {
                mRandomTrackPos.add(randomPos);
                mRandomTracks.add(mAllTracks.get(randomPos));
            }
        }
        mWinnerTrack = mRandomTracks.get(0);
        setupFortuneWheel();
    }

    private void setupFortuneWheel() {
        mFortuneWheel.setCallback(new FortuneWheel.FortuneWheelCallback() {
            @Override
            public void onBindDrawable(final PieView pieView, final PiePieceDrawable drawable, int width, int height, int position) {
                drawable.setColor(getRandomGrey());
                Glide.with(FortuneWheelActivity.this)
                        .asBitmap()
                        .load(CoverModelMetaVisitor
                                .visitAcceptor(position == 0 ? mWinnerTrack : mRandomTracks.get(position), FortuneWheelActivity.this)
                                .getModel())
                        .into(new SimpleTarget<Bitmap>(width, height) {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                drawable.setImageBitmap(resource);
                                pieView.invalidate();
                            }
                        });
            }

            @Override
            public void onBindWinnerView(final CircleImageView mWinnerView) {
                Glide.with(FortuneWheelActivity.this)
                        .as(PaletteBitmap.class)
                        .load(CoverModelMetaVisitor.visitAcceptor(mWinnerTrack, FortuneWheelActivity.this).getModel())
                        .into(new ImageViewTarget<PaletteBitmap>(mWinnerView) {
                            @Override
                            protected void setResource(@Nullable PaletteBitmap resource) {
                                if (resource != null) {
                                    mWinnerView.setImageBitmap(resource.bitmap);
                                    SparseArray<Integer> color = get4Colors(resource.palette);
                                    mFortuneWheel.setColors(color.get(0), color.get(1), color.get(2), color.get(3));
                                }
                            }
                        });
            }
        });
        mFortuneWheel.setSize(mRandomTracks.size());
    }

    @Override
    public void onLoaderReset(Loader<List<Track>> loader) {

    }

    private SparseArray<Integer> get4Colors(Palette palette) {
        SparseArray<Integer> colors = new SparseArray<>(4);

        // strategy 1: get the vibrant swatches if available
        if (palette.getVibrantSwatch() != null) {
            // candidate for A1
            colors.put(0, palette.getVibrantSwatch().getRgb());
        }
        if (palette.getDarkVibrantSwatch() != null) {
            // candidate for A2
            colors.put(1, palette.getDarkVibrantSwatch().getRgb());
        }
        if (palette.getLightVibrantSwatch() != null) {
            // candidate for B1
            colors.put(2, palette.getLightVibrantSwatch().getRgb());
        }
        if (palette.getMutedSwatch() != null) {
            // candidate for B2
            colors.put(3, palette.getMutedSwatch().getRgb());
        }
        if (colors.size() == 4) {
            return colors;
        }

        //strategy 2: fill the array with colors we have in the palette
        if (palette.getSwatches().size() > colors.size()) {
            // we have more colors
            int howMuchMore = palette.getSwatches().size() - colors.size();
            // fill the array with what we have
            for (int i = 0; i < Math.min(colors.size() + howMuchMore, 4); i++) {
                if (colors.get(i) == null) {
                    colors.setValueAt(i, findColorWeDontUse(colors, palette));
                }
            }
            if (colors.size() == 4) {
                return colors;
            }
        }

        //strategy 3: the pic is pretty boring, try to do the best with what we got
        for (int i = 0; i < 4; i++) {
            if (i % 2 == 0) {
                if (colors.get(i) != null && colors.get(i + 1) == null) {
                    int color = ColorUtils.generateDarkerOrLighterColor(colors.get(i));
                    colors.put(i + 1, color);
                }
            } else {
                if (colors.get(i) != null && colors.get(i - 1) == null) {
                    int color = ColorUtils.generateDarkerOrLighterColor(colors.get(i));
                    colors.put(i - 1, color);
                }
            }
        }

        if (colors.size() == 4) {
            return colors;
        }

        //strategy 4: fuck it, put some default values in there
        if (colors.get(0) == null && colors.get(1) == null) {
            colors.put(0, getResources().getColor(R.color.material_blue_grey_300));
            colors.put(1, getResources().getColor(R.color.material_blue_grey_500));
        }
        if (colors.get(2) == null && colors.get(3) == null) {
            colors.put(2, getResources().getColor(R.color.material_red_500));
            colors.put(3, getResources().getColor(R.color.colorAccent));
        }
        return colors;
    }

    private int findColorWeDontUse(SparseArray<Integer> colors, Palette palette) {
        for (Palette.Swatch s : palette.getSwatches()) {
            int color = s.getRgb();
            if (colors.indexOfValue(color) < 0) {
                // we dont have this color yet
                return color;
            }
        }
        //shouldnt happen
        return Color.BLACK;
    }

    @OnClick(R.id.close_btn)
    void onCloseClick(View v) {
        dismiss();
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @OnClick(R.id.again)
    void onAgainClick(View v) {
        if (mAllTracks != null && !mFortuneWheel.isAnimationRunning() && !isDismissing) {
            mAgainClicked = true;
            slowDismissAnimation.cancel();
            mWinnerTrack = null;
            fillRandomTracks();
            resetTextFields();
            mFortuneWheel.performAnimation();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putParcelable(STATE_WINNER_TRACK, mWinnerTrack);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public void dismiss() {
        isDismissing = true;
        setResult(mTrackPlays ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        if (mFortuneWheel.isAnimationRunning()) {
            mFortuneWheel.getCurrentAnimation().cancel();
            mFortuneWheel.findViewById(R.id.fab).setTransitionName(null);
        }
        finishAfterTransition();
    }

    private int getRandomGrey() {
        Random r = new Random();
        switch (r.nextInt(5)) {
            case 0:
                return getResources().getColor(R.color.material_blue_grey_500);
            case 1:
                return getResources().getColor(R.color.material_blue_grey_600);
            case 2:
                return getResources().getColor(R.color.material_blue_grey_700);
            case 3:
                return getResources().getColor(R.color.material_blue_grey_800);
            case 4:
                return getResources().getColor(R.color.material_blue_grey_900);
        }
        return getResources().getColor(R.color.material_blue_grey_500);
    }

    @Override
    public void onTaskDone(JXTask task) {
        if (getBinder() != null && getBinder().getQueueHandler().hasTracks()) {
            getBinder().play(0);
            mTrackPlays = true;
        }
    }

    @Override
    public void onDestroy() {
        slowDismissAnimation.cancel();
        slowDismissAnimation.removeAllListeners();
        fortuneFabAnimator.cancel();
        fortuneFabAnimator.removeAllListeners();
        if (mFortuneWheel.isAnimationRunning()) {
            mFortuneWheel.getCurrentAnimation().cancel();
            mFortuneWheel.getCurrentAnimation().removeAllListeners();
        }
        super.onDestroy();

    }
}
