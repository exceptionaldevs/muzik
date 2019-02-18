package com.exceptional.musiccore.glide.decoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.request.target.Target;
import com.exceptional.musiccore.glide.GenerateCover;

import java.io.IOException;

/**
 * Created by darken on 24.10.2015.
 * Generates a cover for those that have none.
 */
public class GenerativeCoverDecoder implements ResourceDecoder<GenerateCover, Bitmap> {
    private final Context mContext;
    private final BitmapPool mBitmapPool;
    private static final int secondLayeralpha = (int) Math.ceil(255 * 0.85f); // the higher the number the more the shadow will be translucent

    public GenerativeCoverDecoder(Context context, Glide glide) {
        mContext = context;
        mBitmapPool = glide.getBitmapPool();
    }

    @Override
    public boolean handles(GenerateCover source, Options options) throws IOException {
        return true;
    }

    @Nullable
    @Override
    public Resource<Bitmap> decode(GenerateCover source, int _width, int _height, Options options) throws IOException {
        String letters = source.getLetters();
        // FIXME how large should generated covers be if there is no desired target size
        int width = _width == Target.SIZE_ORIGINAL ? 500 : _width;
        int height = _height == Target.SIZE_ORIGINAL ? 500 : _height;

        Bitmap bitmap = mBitmapPool.getDirty(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        int rgb = source.getBackgroundColor();
        bitmap.eraseColor(rgb);

        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(source.getTextColor());
        textPaint.setTextSize(height * 0.6f);

        Paint textShadowPaint = new Paint(textPaint);
        textShadowPaint.setColor(0xFF212121);

        Rect rect = new Rect();
        textPaint.getTextBounds(letters, 0, 1, rect);
        float textWidth = textPaint.measureText(letters);
        float x = width / 2f - textWidth / 2f;
        float y = height / 2f + rect.height() / 2f;

        float tillBorderX = width - x;
        float tillBorderY = height - y;
        int iterate = (int) Math.ceil(Math.max(tillBorderX, tillBorderY));

        int s = canvas.save();
        for (int i = 0; i < iterate; i++) {
            canvas.translate(1, 1);
            canvas.drawText(letters, x, y, textShadowPaint);
        }
        canvas.restoreToCount(s);
        canvas.drawARGB(secondLayeralpha, Color.red(rgb), Color.green(rgb), Color.blue(rgb));

        //TODO: optimize: draw shadow text for one complete width of the letter and then a 45° rectangle till the end

        canvas.drawText(letters, x, y, textPaint);

        return new BitmapResource(bitmap, mBitmapPool);
    }


}
