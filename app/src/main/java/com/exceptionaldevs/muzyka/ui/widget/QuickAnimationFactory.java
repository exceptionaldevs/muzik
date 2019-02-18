package com.exceptionaldevs.muzyka.ui.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.exceptionaldevs.muzyka.R;

/**
 * Created by darken on 05.11.2015.
 */
public class QuickAnimationFactory {

//    public static void makeAnimatedListTick(Context context, StylePack stylePack, ImageView target) {
//        int color = Color.GRAY;
//        if (stylePack != null)
//            color = stylePack.getColorPrimary();
//        Drawable selector = context.getResources().getDrawable(R.drawable.ic_check_white_18dp);
//        int size = target.getWidth();
//        if (size == 0)
//            size = 144;
//        Drawable tick = new ColorFramedCircleDrawable(selector, size, color);
//        target.setImageDrawable(tick);
//        target.setScaleX(0.9f);
//        target.setScaleY(0.9f);
//        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(target,
//                PropertyValuesHolder.ofFloat("scaleX", 1.0f),
//                PropertyValuesHolder.ofFloat("scaleY", 1.0f));
//        scaleDown.setDuration(100);
//        scaleDown.start();
//    }

    public static void elasticPop(View target) {
        ObjectAnimator popIn = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat("scaleX", 0.9f),
                PropertyValuesHolder.ofFloat("scaleY", 0.9f));
        popIn.setDuration(100);
        ObjectAnimator popOut = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f));
        popOut.setDuration(100);
        ObjectAnimator popBack = ObjectAnimator.ofPropertyValuesHolder(target,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f));
        popBack.setDuration(100);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(popOut, popIn, popBack);
        animatorSet.start();
    }

    public static void wiggle(ImageButton playlistButton) {
        Animation shake = AnimationUtils.loadAnimation(playlistButton.getContext(), R.anim.circle_wiggle);
        playlistButton.startAnimation(shake);
    }
}
