package com.exceptional.musiccore.glide.palette;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

public class PaletteBitmapTranscoder implements ResourceTranscoder<Bitmap, PaletteBitmap> {
    private final BitmapPool mBitmapPool;
    private final int mNumColors;

    public PaletteBitmapTranscoder(Context context, Glide glide) {
        this(context, 16, glide);
    }

    /**
     * @param numColors maximum number of swatches to generate (may be less)
     * @see android.support.v7.graphics.Palette#generate(Bitmap, int)
     */
    public PaletteBitmapTranscoder(Context context, int numColors, Glide glide) {
        this.mBitmapPool = glide.getBitmapPool();
        this.mNumColors = numColors;
    }

    @Nullable
    @Override
    public Resource<PaletteBitmap> transcode(@NonNull Resource<Bitmap> toTranscode, @NonNull Options options) {
        Palette palette = Palette.from(toTranscode.get()).generate();
        PaletteBitmap result = new PaletteBitmap(toTranscode.get(), palette);
        return new PaletteBitmapResource(result, mBitmapPool);
    }
}