package com.exceptional.musiccore.glide;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.exceptional.musiccore.MusicCoreApplication;

import java.io.IOException;

/**
 * Created by darken on 24.10.2015.
 */
public class CoverUtil {
    public static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "CoverUtil";

    public static boolean quickCheckCoverExistenceInProvider(Context context, Uri coverSource) {
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(coverSource, "r");
            if (pfd != null) {
                pfd.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int targetWidth, int targetHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > targetHeight || width > targetWidth) {
            // Get ratios
            final int heightRatio = Math.round((float) height / (float) targetHeight);
            final int widthRatio = Math.round((float) width / (float) targetWidth);

            // Compare heigth with width so we get an image that fits the height and weidth requirements
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

}
