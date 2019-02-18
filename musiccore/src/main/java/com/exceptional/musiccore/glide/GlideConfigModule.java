package com.exceptional.musiccore.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.exceptional.musiccore.glide.decoder.GenerativeCoverDecoder;
import com.exceptional.musiccore.glide.lfm.LFMImageLoader;
import com.exceptional.musiccore.glide.lfm.LFMRequest;
import com.exceptional.musiccore.glide.palette.PaletteBitmap;
import com.exceptional.musiccore.glide.palette.PaletteBitmapTranscoder;

import java.io.InputStream;

/**
 * Created by darken on 23/10/15.
 * Global Glide configuration.
 * Gets automatically loaded through an entry in the AndroidManifest.xml
 */
public class GlideConfigModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDefaultRequestOptions(RequestOptions
                .diskCacheStrategyOf(DiskCacheStrategy.NONE)
                .priority(Priority.LOW));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(GenerateCover.class, GenerateCover.class, new GenerateCoverModelLoader.Factory())
                .append(LFMRequest.class, InputStream.class, new LFMImageLoader.Factory(context))
                .append(GenerateCover.class, Bitmap.class, new GenerativeCoverDecoder(context, glide))
                .register(Bitmap.class, PaletteBitmap.class, new PaletteBitmapTranscoder(context, glide));
    }

}
