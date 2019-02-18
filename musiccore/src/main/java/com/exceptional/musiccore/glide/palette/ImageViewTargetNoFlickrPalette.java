package com.exceptional.musiccore.glide.palette;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;

/**
 * Created by sebnap on 18.11.15.
 */
public class ImageViewTargetNoFlickrPalette extends ImageViewTarget<PaletteBitmap> {
    public ImageViewTargetNoFlickrPalette(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
    }

    @Override
    public void onLoadFailed(Drawable errorDrawable) {
    }

    @Override
    public void onLoadCleared(Drawable placeholder) {
    }

    @Override
    protected void setResource(PaletteBitmap resource) {
        getView().setImageBitmap(resource.bitmap);
    }

}
