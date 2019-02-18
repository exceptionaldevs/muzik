/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exceptionaldevs.muzyka.content.sheets;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.exceptionaldevs.muzyka.MuzikServiceActivity;
import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.ui.widget.BottomSheet;
import com.exceptionaldevs.muzyka.ui.widget.FABToggle;
import com.exceptionaldevs.muzyka.ui.widget.ObservableGridLayoutManager;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.ListSpacingDecoration;
import com.exceptionaldevs.muzyka.utils.AnimatorListener;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LibBottomSheetActvity extends MuzikServiceActivity {

    @BindView(R.id.bottom_sheet) BottomSheet mBottomSheet;
    @BindView(R.id.bottom_sheet_content) ViewGroup mBottomSheetContent;
    @BindView(R.id.title) TextView mSheetTitle;
    @BindView(R.id.title_spacer) View mTitleSpacer;
    @BindView(R.id.circle_image) ImageView mCircleImage;
    @BindView(R.id.rect_image) ImageView mRectImage;
    @BindView(R.id.recyclerview) RecyclerView mRecyclerView;
    @BindView(R.id.fab_play_pause) FABToggle fab;
    @BindView(R.id.overlay) ImageView mOverlay;

    @BindDimen(R.dimen.z_app_bar) float appBarElevation;
    private float mHeaderTranslationZ;
    private int fabOffset;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_sheet);
        ButterKnife.bind(this);

        setupRecyclerView(mRecyclerView);
        setupBottomSheet(mBottomSheet);

        postponeEnterTransition();
        mSheetTitle.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mSheetTitle.getViewTreeObserver().removeOnPreDrawListener(this);
                calculateFabPosition();
                startPostponedEnterTransition();
                return true;
            }
        });
    }


    private void setupBottomSheet(BottomSheet bottomSheet) {
        bottomSheet.addListener(new BottomSheet.Listener() {
            @Override
            public void onDragDismissed() {
                // After a drag dismiss, finish without the shared element return transition as
                // it no longer makes sense.  Let the launching window know it's a drag dismiss so
                // that it can restore any UI used as an entering shared element
                finish();
            }

            @Override
            public void onDrag(int top) { /* no-op */ }

            @Override
            public void onBottomSheetExpanded(int top) {
                float startRadius = Math.max(mCircleImage.getWidth(), mCircleImage.getHeight());
                float finalRadius = Math.max(mRectImage.getWidth(), mRectImage.getHeight());
                final float scale = finalRadius / startRadius;

                LibBottomSheetActvity.this.onPreCircleExpanded();

                mCircleImage.animate()
                        .translationZ(mRectImage.getElevation())
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(150l)
                        .setInterpolator(AnimationUtils.loadInterpolator(
                                LibBottomSheetActvity.this,
                                android.R.interpolator.fast_out_linear_in))
                        .setListener(new AnimatorListener() {
                            @Override
                            public void onAnimationCancel(Animator animation) {
                                mCircleImage.setVisibility(View.INVISIBLE);// View.GONE starts new layout process, that could lagg
                                mTitleSpacer.setVisibility(View.INVISIBLE);
                                mRectImage.setVisibility(View.VISIBLE);
                                LibBottomSheetActvity.this.onPostCircleExpanded();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mCircleImage.setVisibility(View.INVISIBLE);
                                mTitleSpacer.setVisibility(View.INVISIBLE);
                                mRectImage.setVisibility(View.VISIBLE);
                                LibBottomSheetActvity.this.onPostCircleExpanded();
                            }
                        })
                        .start();
            }
        });
    }

    public void onPreCircleExpanded() {

    }

    public void onPostCircleExpanded() {

    }

    @Override
    public void startPostponedEnterTransition() {
        enterAnimation();
        super.startPostponedEnterTransition();
    }

    protected void setupRecyclerView(RecyclerView recyclerView) {
        ObservableGridLayoutManager layoutManager = new ObservableGridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false);
        layoutManager.setListener(new ObservableGridLayoutManager.OnOverscroll() {
            @Override
            public void onOverscollTop() {
                if (getHeaderTranslationZ() != 0) {
                    setHeaderTranslationZ(0);
                }
            }

            @Override
            public void onOverscrollBottom() {

            }

            @Override
            public void onScroll() {
                if (getHeaderTranslationZ() != appBarElevation) {
                    animateTranslationZ(appBarElevation);
                }
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new ListSpacingDecoration(this, R.dimen.spacing_micro));
    }

    /**
     * Animate in the title, description and author – can't do this in a content transition as they
     * are within the ListView so do it manually.  Also handle the FAB tanslation here so that it
     * plays nicely with #calculateFabPosition
     */
    private void enterAnimation() {
        Interpolator interp = AnimationUtils.loadInterpolator(this, android.R.interpolator
                .fast_out_slow_in);
        int offset = mSheetTitle.getHeight();
//        viewEnterAnimation(mSheetTitle, offset, interp);
//        if (mRecyclerView.getVisibility() == View.VISIBLE) {
//            offset *= 1.5f;
//            viewEnterAnimation(mRecyclerView, offset, interp);
//        }
        // animate the fab without touching the alpha as this is handled in the content transition
//        offset *= 1.5f;
//        float fabTransY = fab.getTranslationY();
//        fab.setTranslationY(fabTransY + offset);
//        fab.animate()
//                .translationY(fabTransY)
//                .setDuration(600)
//                .setInterpolator(interp)
//                .start();
//        offset *= 1.5f;
//        viewEnterAnimation(shotActions, offset, interp);
//        offset *= 1.5f;
//        viewEnterAnimation(playerName, offset, interp);
//        viewEnterAnimation(playerAvatar, offset, interp);
//        viewEnterAnimation(shotTimeAgo, offset, interp);

//        if (isOrientationChange) {
        // we rely on the window enter content transition to show the fab. This isn't run on
        // orientation changes so manually show it.
        Animator showFab = ObjectAnimator.ofPropertyValuesHolder(fab,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f));
        showFab.setStartDelay(300L);
        showFab.setDuration(300L);
        showFab.setInterpolator(AnimationUtils.loadInterpolator(this,
                android.R.interpolator.linear_out_slow_in));
        showFab.start();
//        }
    }

    private void viewEnterAnimation(View view, float offset, Interpolator interp) {
        view.setTranslationY(offset);
        view.setAlpha(0.8f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setInterpolator(interp)
                .setListener(null)
                .start();
    }

    private void calculateFabPosition() {
        // calculate 'natural' position i.e. with full height image. Store it for use when scrolling
        fabOffset = mSheetTitle.getBottom() - (fab.getHeight() / 2);
        fab.setOffset(fabOffset);

        mOverlay.setTranslationY(fabOffset);
        ViewCompat.postInvalidateOnAnimation(mOverlay);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public ImageView getRectImage() {
        return mRectImage;
    }

    public ImageView getCircleImage() {
        return mCircleImage;
    }

    public View getTitleSpacer() {
        return mTitleSpacer;
    }

    public TextView getSheetTitle() {
        return mSheetTitle;
    }

    public ViewGroup getBottomSheetContent() {
        return mBottomSheetContent;
    }

    public BottomSheet getBottomSheet() {
        return mBottomSheet;
    }

    protected void setHeaderTranslationZ(float z) {
        mHeaderTranslationZ = z;
        mSheetTitle.setTranslationZ(z);
        mRectImage.setTranslationZ(z);
        mTitleSpacer.setTranslationZ(z);
    }

    protected float getHeaderTranslationZ() {
        return mHeaderTranslationZ;
    }

    protected void animateTranslationZ(float z) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "HeaderTranslationZ", z);
        animator.setDuration(80l);
        animator.setStartDelay(0);
        animator.setInterpolator(AnimationUtils.loadInterpolator(
                LibBottomSheetActvity.this,
                android.R.interpolator.fast_out_linear_in));
        animator.start();
    }

    protected float getAppBarElevation() {
        return appBarElevation;
    }

    @OnClick(R.id.fab_play_pause)
    protected void onFabClick(View v) {
//        int deltaX = Math.max(mOverlay.getTop(), mBottomSheet.getBottom() - mOverlay.getTop());
//        int deltaY = Math.max(mOverlay.getLeft(), mBottomSheet.getRight() - mOverlay.getLeft());
//        float hypot = (float) Math.hypot(deltaX, deltaY);
//        float scale = hypot / mOverlay.getHeight();
//        mOverlay.setAlpha(1f);
//        mOverlay.animate()
//                .setDuration(80l)
//                .scaleX(scale)
//                .scaleY(scale)
//                .setInterpolator(AnimationUtils.loadInterpolator(
//                        LibBottomSheetActvity.this,
//                        android.R.interpolator.fast_out_linear_in))
//                .setListener(new AnimatorListener(){
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                        mOverlay.setBackgroundColor(getResources().getColor(R.color.colorAccent));
//                    }
//                })
//                .start();

    }
}
