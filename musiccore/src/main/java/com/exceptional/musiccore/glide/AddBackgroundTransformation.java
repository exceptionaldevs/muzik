package com.exceptional.musiccore.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;


public class AddBackgroundTransformation extends BitmapTransformation {
    private final String mID;
    private final String mIDBytes;
    @ColorInt
    private final int mBackgroundColor;


    public AddBackgroundTransformation(Context context, @ColorInt int backgroundColor) {
        super();
        mBackgroundColor = backgroundColor;
        mID = getClass().getName() + ":" + backgroundColor;
        try {
            mIDBytes = new String(mID.getBytes(STRING_CHARSET_NAME));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("This shouldn't happen");
        }
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
//        Bitmap scaledContent = Bitmap.createScaledBitmap(toTransform, outWidth, outHeight, false);
        Bitmap composite = Bitmap.createBitmap(outWidth, outHeight, toTransform.getConfig());
        Canvas composer = new Canvas(composite);
        composer.drawColor(mBackgroundColor);
        int middleX = outWidth / 2;
        int middleY = outHeight / 2;

        composer.drawBitmap(toTransform,
                middleX - toTransform.getWidth() / 2,
                middleY - toTransform.getHeight() / 2,
                null);
        return composite;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(mIDBytes.getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AddBackgroundTransformation))
            return false;

        AddBackgroundTransformation foo = (AddBackgroundTransformation) o;
        if (mBackgroundColor != foo.mBackgroundColor)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + Integer.valueOf(mBackgroundColor).hashCode();
        return hash;
    }

}
