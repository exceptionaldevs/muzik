package com.exceptionaldevs.muzyka.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.exceptionaldevs.muzyka.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sebnap on 06.11.15.
 */
public class SplashLayout extends FrameLayout {

    @BindView(R.id.splash_background) ImageView mBackground;
    @BindView(R.id.splash_icon) ImageView mIcon;
    @BindView(R.id.splash_caption) TextView mCaption;

    public SplashLayout(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public SplashLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public SplashLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SplashLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(getContext(), R.layout.view_splash_container, this);
        ButterKnife.bind(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SplashLayout, defStyleAttr, 0);
        Drawable background = a.getDrawable(R.styleable.SplashLayout_splash_background);
        if (background != null) {
            mBackground.setImageDrawable(background);
        }
        Drawable icon = a.getDrawable(R.styleable.SplashLayout_splash_icon);
        if (icon != null) {
            Drawable tintedDrawable = DrawableCompat.wrap(icon);
            DrawableCompat.setTint(tintedDrawable.mutate(), Color.DKGRAY);
            mIcon.setImageDrawable(tintedDrawable);
            mIcon.setImageDrawable(icon);
        }

        String caption = a.getString(R.styleable.SplashLayout_splash_caption);
        if (caption != null) {
            mCaption.setText(caption);
        }
        a.recycle();
    }

    public void setCaption(String caption, boolean animate) {
        mCaption.setText(caption);
        if (animate) {
            Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.circle_wiggle);
            mIcon.startAnimation(shake);
        } else {
            mIcon.animate().cancel();
        }
    }

    public void tryHide(boolean animate) {
        if (getVisibility() == View.VISIBLE) {
            if (animate) {
                animate()
                        .alpha(0f)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                setVisibility(INVISIBLE);
                            }
                        })
                        .start();
            } else {
                setVisibility(INVISIBLE);
            }
        }
    }

    public void tryShow(boolean animate) {
        if (getVisibility() == View.INVISIBLE) {
            if (animate) {
                animate()
                        .alpha(1f)
                        .withStartAction(new Runnable() {
                            @Override
                            public void run() {
                                setVisibility(VISIBLE);
                            }
                        })
                        .start();
            } else {
                setVisibility(VISIBLE);
            }
        }
    }

}
