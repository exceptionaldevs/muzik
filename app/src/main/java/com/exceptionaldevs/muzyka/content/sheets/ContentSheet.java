package com.exceptionaldevs.muzyka.content.sheets;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.library.LibraryItem;
import com.exceptional.musiccore.library.LoaderArgs;
import com.exceptionaldevs.muzyka.MainActivity;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;
import com.exceptionaldevs.muzyka.utils.ColorUtils;
import com.exceptionaldevs.muzyka.utils.StylePack;
import com.exceptionaldevs.muzyka.utils.StylePackFactory;
import com.exceptionaldevs.muzyka.utils.ViewUtils;

import java.util.List;

/**
 * Created by sebnap on 21.01.16.
 */
public abstract class ContentSheet<T extends LibraryItem> extends LibBottomSheetActvity implements LoaderManager.LoaderCallbacks<List<? extends LibraryItem>> {
    public static final int LOADER_ID_ARTISTS = 1;
    public static final int LOADER_ID_ALBUMS = 2;
    public static final int LOADER_ID_TRACKS = 3;
    public static final int LOADER_ID_PLAYLISTS = 4;
    public static final int LOADER_ID_DEVICE = 5;

    private static final float SCRIM_ADJUSTMENT = 0.075f;
    public static final String EXTRA_CONTENT_ITEM = "com.exceptionaldevs.muzyka.content.sheets:contentitem";
    private T mLibraryItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLibraryItem = getIntent().getParcelableExtra(EXTRA_CONTENT_ITEM);

        postponeEnterTransition();

        Glide.with(this)
                .asBitmap()
                .load(CoverModelMetaVisitor.visitAcceptor(mLibraryItem, this).getModel())
                .thumbnail(Glide.with(this).asBitmap().load(R.drawable.material_colors_stock))
                .apply(RequestOptions.centerCropTransform())
                .listener(imgLoadListener)
                .into(getRectImage());

        Glide.with(this)
                .load(CoverModelMetaVisitor.visitAcceptor(mLibraryItem, this).getModel())
                .apply(RequestOptions.circleCropTransform())
                .into(getCircleImage());
    }

    public ContentAdapter<? extends LibraryItem, ? extends SDMViewHolder> getAdapter() {
        return (ContentAdapter<? extends LibraryItem, ? extends SDMViewHolder>) getRecyclerView().getAdapter();
    }

    public abstract ContentAdapter<T, ? extends SDMViewHolder> initAdapter();


    public abstract LoaderArgs getLoaderArgs();

    public T getLibraryItem() {
        return mLibraryItem;
    }

    @Override
    public abstract Loader<List<? extends LibraryItem>> onCreateLoader(int id, Bundle args);

    @Override
    public void onLoadFinished(Loader<List<? extends LibraryItem>> loader, List<? extends LibraryItem> data) {
        displayData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<? extends LibraryItem>> loader) {
//        if (getAdapter() instanceof JXPlayer.PlayerProgressListener)
//            getJXBinder().removePlayerProgressListener((JXPlayer.PlayerProgressListener) getAdapter());
        getAdapter().notifyDataSetChanged();
        getAdapter().setData(null);
    }

    public void displayData(List<? extends LibraryItem> data) {
        ContentAdapter adapter = getAdapter();
        boolean newAdapter = adapter == null;
        if (newAdapter)
            adapter = initAdapter();

        adapter.setData(data);
        if (newAdapter) {
            getRecyclerView().setAdapter(adapter);
//            if (adapter instanceof JXPlayer.PlayerProgressListener) {
//                // FIXME after app resume crash due to binder being null
//                getJXBinder().addPlayerProgressListener((JXPlayer.PlayerProgressListener) adapter);
//            }
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private RequestListener imgLoadListener = new RequestListener<Bitmap>() {
        @Override
        public boolean onResourceReady(final Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
            startPostponedEnterTransition();
            Palette.from(resource)
                    .generate(new Palette.PaletteAsyncListener() {
                        @Override
                        public void onGenerated(Palette palette) {
                            StylePack stylepack = StylePackFactory.fromPalette(ContentSheet.this, palette);
                            AnimatorSet ab = new AnimatorSet();
                            int color = Color.TRANSPARENT;
                            if (getTitleSpacer().getBackground() instanceof ColorDrawable) {
                                color = ((ColorDrawable) getTitleSpacer().getBackground()).getColor();
                            }

                            if (ColorUtils.isDark(stylepack.getColorPrimary())) {
                                getSheetTitle().setTextColor(getResources().getColor(R.color.text_primary_light));
                            } else {
                                getSheetTitle().setTextColor(getResources().getColor(R.color.text_primary_dark));
                            }

                            ab.playTogether(
                                    ObjectAnimator.ofArgb(getTitleSpacer(), "backgroundColor", color, stylepack.getColorPrimary()),
                                    ObjectAnimator.ofArgb(getSheetTitle(), "backgroundColor", color, stylepack.getColorPrimary()));
                            ab.setDuration(1000);
                            ab.setInterpolator(AnimationUtils
                                    .loadInterpolator(ContentSheet.this, android.R
                                            .interpolator.fast_out_slow_in));
                            ab.start();
                        }
                    });
            return false;
        }

        @Override
        public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
            startPostponedEnterTransition();
            return false;
        }
    };

    @Override
    public void onPreCircleExpanded() {
        Glide.with(this)
                .asBitmap()
                .load(CoverModelMetaVisitor.visitAcceptor(getLibraryItem(), this).getModel())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(final Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        float imageScale = (float) getRectImage().getHeight() / (float) resource.getHeight();
                        final ImageView imageView = getRectImage();
                        float twentyFourDip =
                                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
                        Palette.from(resource)
                                .maximumColorCount(3)
                                .clearFilters()
                                .setRegion(0, 0, resource.getWidth() - 1, (int) (twentyFourDip / imageScale))
                                        // - 1 to work around https://code.google.com/p/android/issues/detail?id=191013
                                .generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        boolean isDark;
                                        @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                                        if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                                            isDark = ColorUtils.isDark(resource, resource.getWidth() / 2, 0);
                                        } else {
                                            isDark = lightness == ColorUtils.IS_DARK;
                                        }

//                            if (!isDark) { // make back icon dark on light images
//                                back.setColorFilter(ContextCompat.getColor(
//                                        DribbbleShot.this, R.color.dark_icon));
//                            }

                                        // color the status bar. Set a complementary dark color on L,
                                        // light or dark color on M (with matching status bar icons)
                                        int statusBarColor = getWindow().getStatusBarColor();
                                        Palette.Swatch topColor = ColorUtils.getMostPopulousSwatch(palette);
                                        if (topColor != null &&
                                                (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                                            statusBarColor = ColorUtils.scrimify(topColor.getRgb(),
                                                    isDark, SCRIM_ADJUSTMENT);
                                            // set a light status bar on M+
                                            if (!isDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                ViewUtils.setLightStatusBar(imageView);
                                            }
                                        }

                                        if (statusBarColor != getWindow().getStatusBarColor()) {
                                            ValueAnimator statusBarColorAnim = ValueAnimator.ofArgb(getWindow
                                                    ().getStatusBarColor(), statusBarColor);
                                            statusBarColorAnim.addUpdateListener(new ValueAnimator
                                                    .AnimatorUpdateListener() {
                                                @Override
                                                public void onAnimationUpdate(ValueAnimator animation) {
                                                    getWindow().setStatusBarColor((int) animation
                                                            .getAnimatedValue());
                                                }
                                            });
                                            statusBarColorAnim.setDuration(1000);
                                            statusBarColorAnim.setInterpolator(AnimationUtils
                                                    .loadInterpolator(ContentSheet.this, android.R
                                                            .interpolator.fast_out_slow_in));
                                            statusBarColorAnim.start();
                                        }
                                    }
                                });
                        return false;
                    }
                })
                .into(getRectImage());
    }

    @Override
    protected void onFabClick(View v) {
        super.onFabClick(v);
        playAll();
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClass(ContentSheet.this, MainActivity.class);
                startActivity(intent);
            }
        }, 200);
    }

    protected abstract void playAll();

}
