package com.exceptionaldevs.muzyka.content.sheets.artist;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.exceptional.musiccore.BinderCustomer;
import com.exceptional.musiccore.MusicBinder;
import com.exceptional.musiccore.engine.queuetasks.JXSnackTask;
import com.exceptional.musiccore.engine.queuetasks.JXTask;
import com.exceptional.musiccore.glide.CoverModelMetaVisitor;
import com.exceptional.musiccore.glide.palette.PaletteBitmap;
import com.exceptional.musiccore.library.albums.Album;
import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.content.ContentAdapter;
import com.exceptionaldevs.muzyka.content.sheets.ContentSheet;
import com.exceptionaldevs.muzyka.content.sheets.album.AlbumSheet;
import com.exceptionaldevs.muzyka.ui.widget.QuickAnimationFactory;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;
import com.exceptionaldevs.muzyka.utils.AnimatorListener;
import com.exceptionaldevs.muzyka.utils.ColorUtils;
import com.exceptionaldevs.muzyka.utils.QueueHelper;
import com.exceptionaldevs.muzyka.utils.ViewUtils;


/**
 * Created by darken on 30.03.14.
 */
public class ArtistAlbumsAdapter extends ContentAdapter<Album, ArtistAlbumsAdapter.ViewHolder> {

    // we need to hold on to an activity ref for the shared element transitions :/
    private final MuzikServiceActivity mHost;
    private final RecyclerView mRecyclerView;


    public ArtistAlbumsAdapter(MuzikServiceActivity hostActivity, RecyclerView recyclerView) {
        super(hostActivity);
        this.mHost = hostActivity;
        this.mRecyclerView = recyclerView;
    }

    @Override
    public void onBindBasicItemView(final ViewHolder holder, final int position) {
        final Album libraryItem = getItem(position);

        holder.mTextView.setText(libraryItem.getAlbumName());

        ViewUtils.setOvalOutlineProvider(holder.mImageView);
        Glide.with(holder.mImageView.getContext())
                .as(PaletteBitmap.class)
                .load(CoverModelMetaVisitor.visitAcceptor(libraryItem, mContext).getModel())
                .thumbnail(Glide.with(holder.mImageView.getContext()).as(PaletteBitmap.class).load(R.drawable.material_colors_stock))
                .into(new ImageViewTarget<PaletteBitmap>(holder.mImageView) {
                    @Override
                    protected void setResource(@Nullable PaletteBitmap resource) {
                        if (resource != null) {
                            holder.mImageView.setImageBitmap(resource.bitmap);
                            int color = getAColor(resource.palette);
                            holder.mView.setBackgroundColor(color);
                            if (ColorUtils.isDark(color)) {
                                holder.mTextView.setTextColor(mHost.getResources().getColor(R.color.text_primary_light));
                            } else {
                                holder.mTextView.setTextColor(mHost.getResources().getColor(R.color.text_primary_dark));
                            }
                            int buttonColor = ColorUtils.generateDarkerOrLighterColor(color);
                            holder.mPlusBtn.setImageTintList(ColorStateList.valueOf(buttonColor));
                            holder.mPlusBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    mHost.registerBinderCustomer(new BinderCustomer<MusicBinder>() {
                                        @Override
                                        public void onBinderAvailable(final MusicBinder binder) {
                                            JXTask.JXTaskCallback task = new JXTask.JXTaskCallback() {
                                                @Override
                                                public void onTaskDone(JXTask task) {
                                                    if (!mRecyclerView.isAttachedToWindow())
                                                        return;
                                                    final JXSnackTask snackTask = (JXSnackTask) task;
                                                    Snackbar.make(mRecyclerView, snackTask.getSnackMessage(), Snackbar.LENGTH_LONG)
                                                            .setAction(snackTask.getSnackActionName(), new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    binder.getTasker().execute(snackTask.getSnackTask(), null);
                                                                }
                                                            })
                                                            .show();
                                                }
                                            };
                                            QueueHelper.addToQueue(v.getContext(), binder, task, libraryItem.getUriForTracks(), "Album added to queue", "Undo");
                                            v.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    QuickAnimationFactory.elasticPop(v);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

        holder.mTransitionCiv.setVisibility(View.INVISIBLE);
        holder.mView.setVisibility(View.VISIBLE);


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mTransitionCiv.setTransitionName(view.getResources().getString(R.string.transition_libitem));
                holder.mTransitionCiv.setImageDrawable(holder.mImageView.getDrawable());

                int deltaY = 0;
                boolean topClipped = false;
                Rect itemViewRect = new Rect();
                int viewTop = mRecyclerView.getLayoutManager().getDecoratedTop(holder.itemView);
                if (viewTop < 0) {
                    topClipped = true;
                    deltaY = -viewTop;
                } else if (holder.mImageView.getGlobalVisibleRect(itemViewRect)) {
                    if (itemViewRect.height() < holder.mImageView.getHeight()) { // item got clipped
                        deltaY = holder.mImageView.getHeight() - itemViewRect.height();
                    }
                }

                AnimatorSet set = new AnimatorSet();
                Animator reveal = createCircularEnterReveal(holder);
                Animator translate = ObjectAnimator.ofFloat(holder.getRootView(), "translationY", topClipped ? deltaY : -deltaY);
                translate.setInterpolator(AnimationUtils.loadInterpolator(
                        mHost,
                        android.R.interpolator.fast_out_linear_in));
                reveal.setStartDelay(80);
                set.setDuration(180);
                set.playTogether(translate, reveal);

                reveal.setInterpolator(AnimationUtils.loadInterpolator(
                        mHost,
                        android.R.interpolator.fast_out_linear_in));
                reveal.addListener(new AnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        holder.mView.setVisibility(View.INVISIBLE);
                        holder.mTransitionCiv.setVisibility(View.VISIBLE);

                        Intent intent = new Intent();
                        intent.setClass(mHost, AlbumSheet.class);
                        intent.putExtra(ArtistSheet.EXTRA_RESTORE_ITEM_POS, position);
                        intent.putExtra(ContentSheet.EXTRA_CONTENT_ITEM, libraryItem);

                        ActivityOptions options =
                                ActivityOptions.makeSceneTransitionAnimation(mHost,
                                        Pair.create((View) holder.mTransitionCiv, mHost.getString(R.string.transition_libitem)));
                        mHost.startActivityForResult(intent, ArtistSheet.ACTIVITY_REQUEST_CODE, options.toBundle());
                    }
                });
                holder.getRootView().setTranslationZ(200);
                set.start();
            }
        });

    }

    private int getAColor(Palette palette) {
        if (palette.getMutedSwatch() != null) {
            return palette.getMutedSwatch().getRgb();
        }

        if (palette.getDarkMutedSwatch() != null) {
            return palette.getDarkMutedSwatch().getRgb();
        }

        if (palette.getDarkVibrantSwatch() != null) {
            return palette.getDarkVibrantSwatch().getRgb();
        }

        if (palette.getLightMutedSwatch() != null) {
            return palette.getLightMutedSwatch().getRgb();
        }

        if (palette.getVibrantSwatch() != null) {
            return palette.getVibrantSwatch().getRgb();
        }

        if (palette.getLightVibrantSwatch() != null) {
            return palette.getLightVibrantSwatch().getRgb();
        }

        return mHost.getResources().getColor(R.color.cardview_light_background);
    }

    Animator createCircularEnterReveal(final ViewHolder holder) {
        int halfwidth = holder.mImageView.getWidth() / 2;
        int halfheight = holder.mImageView.getHeight() / 2;
        float startRadius = (float) Math.hypot(halfwidth, halfheight);
        float endRadius = Math.min(halfwidth, halfheight);

        Animator animator = ViewAnimationUtils.createCircularReveal(holder.mView, halfwidth, halfheight, startRadius, endRadius);
        animator.setDuration(180);
        animator.setInterpolator(AnimationUtils.loadInterpolator(
                mHost,
                android.R.interpolator.fast_out_linear_in));
        return animator;
    }

    Animator createCircularReturnReveal(final ViewHolder holder) {
        int halfwidth = holder.mImageView.getWidth() / 2;
        int halfheight = holder.mImageView.getHeight() / 2;
        float endRadius = (float) Math.hypot(halfwidth, holder.mView.getHeight() - halfheight);
        float startRadius = Math.min(halfwidth, halfheight);

        Animator animator = ViewAnimationUtils.createCircularReveal(holder.mView, halfwidth, halfheight, startRadius, endRadius);
        animator.setDuration(180);
        animator.setInterpolator(AnimationUtils.loadInterpolator(
                mHost,
                android.R.interpolator.fast_out_linear_in));
        return animator;
    }

    @Override
    public ViewHolder onCreateBasicItemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grid_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public static class ViewHolder extends SDMViewHolder {
        public final View mView;
        public final ImageView mImageView;
        public final ImageView mTransitionCiv;
        public final TextView mTextView;
        public final ImageButton mPlusBtn;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.item_image);
            mTransitionCiv = (ImageView) view.findViewById(R.id.item_transition_civ);
            mTextView = (TextView) view.findViewById(R.id.item_primary_text);
            mPlusBtn = (ImageButton) view.findViewById(R.id.item_add_track);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }
}
