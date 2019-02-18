package com.exceptional.musiccore.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.exceptional.musiccore.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by sebnapi on 22.03.14.
 */
public class Utils {
    public static final float MATH_PI_2 = (float) (Math.PI / 2);
    public static final float MATH_2PI = (float) (Math.PI * 2);
    public static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    public static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    public static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();


    public static float constrainFloatInRange(final float value, final float minValue, final float maxValue) {
        return Math.min(maxValue, Math.max(minValue, value));
    }

    public static int constrainIntInRange(final int value, final int minValue, final int maxValue) {
        return Math.min(maxValue, Math.max(minValue, value));
    }


    public static void getPolar(float x, float y, PointF res) {
        res.x = (float) (y < 0 ? MATH_PI_2 + Math.PI : MATH_PI_2);
        res.y = (float) Math.sqrt(x * x + y * y);
        if (x != 0) {
            res.x = (float) Math.atan2(y, x);
            if (res.x < 0) {
                res.x = (MATH_2PI + res.x);
            }
        }
    }

    // scaleFrom = 0.9
    // value = 0.91
    // output = 0.1
    public static float getScaledTo1(float scaleFrom, float value) {
        return (value - scaleFrom) * 1f / (1f - scaleFrom);
    }


    public static String getAndLogPackageSHASignature(Context ctx) {
        PackageInfo info;
        String shaSignature = null;
        try {

            info = ctx.getPackageManager().getPackageInfo(
                    "com.apodamusic.player", PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                shaSignature = convertToHexString(md.digest());
                Log.e("*** Hash key ***", shaSignature);
            }

        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }

        return shaSignature;
    }

    public static String convertToHexString(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }


    /**
     * rgb is an int (with alpha ignored) 0xrrggbb
     * hsl is an array of 3 floats:
     * [0] is hue, 0..360
     * [1] is saturation, 0..1
     * [2] is lightness, 0..1
     */
    public static void rgbToHsl(int rgb, float[] hsl) {
        float r = ((0x00ff0000 & rgb) >> 16) / 255.f;
        float g = ((0x0000ff00 & rgb) >> 8) / 255.f;
        float b = ((0x000000ff & rgb)) / 255.f;
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float c = max - min;

        float h_ = 0.f;
        if (c == 0) {
            h_ = 0;
        } else if (max == r) {
            h_ = (float) (g - b) / c;
            if (h_ < 0) h_ += 6.f;
        } else if (max == g) {
            h_ = (float) (b - r) / c + 2.f;
        } else if (max == b) {
            h_ = (float) (r - g) / c + 4.f;
        }
        float h = 60.f * h_;

        float l = (max + min) * 0.5f;

        float s;
        if (c == 0) {
            s = 0.f;
        } else {
            s = c / (1 - Math.abs(2.f * l - 1.f));
        }

        hsl[0] = h;
        hsl[1] = s;
        hsl[2] = l;
    }

    /**
     * rgb is an int (with alpha ignored) 0xrrggbb
     * hsl is an array of 3 floats:
     * [0] is hue, 0..360
     * [1] is saturation, 0..1
     * [2] is lightness, 0..1
     */
    public static int hslToRgb(int alpha, float[] hsl) {
        float h = hsl[0];
        float s = hsl[1];
        float l = hsl[2];

        float c = (1 - Math.abs(2.f * l - 1.f)) * s;
        float h_ = h / 60.f;
        float h_mod2 = h_;
        if (h_mod2 >= 4.f) h_mod2 -= 4.f;
        else if (h_mod2 >= 2.f) h_mod2 -= 2.f;

        float x = c * (1 - Math.abs(h_mod2 - 1));
        float r_, g_, b_;
        if (h_ < 1) {
            r_ = c;
            g_ = x;
            b_ = 0;
        } else if (h_ < 2) {
            r_ = x;
            g_ = c;
            b_ = 0;
        } else if (h_ < 3) {
            r_ = 0;
            g_ = c;
            b_ = x;
        } else if (h_ < 4) {
            r_ = 0;
            g_ = x;
            b_ = c;
        } else if (h_ < 5) {
            r_ = x;
            g_ = 0;
            b_ = c;
        } else {
            r_ = c;
            g_ = 0;
            b_ = x;
        }

        float m = l - (0.5f * c);
        int r = (int) ((r_ + m) * (255.f) + 0.5f);
        int g = (int) ((g_ + m) * (255.f) + 0.5f);
        int b = (int) ((b_ + m) * (255.f) + 0.5f);
        return alpha << 24 | r << 16 | g << 8 | b;
    }

    public int[] rgb2lab(int R, int G, int B) {
        //http://www.brucelindbloom.com

        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;
        float eps = 216.f / 24389.f;
        float k = 24389.f / 27.f;

        float Xr = 0.964221f;  // reference white D50
        float Yr = 1.0f;
        float Zr = 0.825211f;

        // RGB to XYZ
        r = R / 255.f; //R 0..1
        g = G / 255.f; //G 0..1
        b = B / 255.f; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r / 12;
        else
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

        if (g <= 0.04045)
            g = g / 12;
        else
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

        if (b <= 0.04045)
            b = b / 12;
        else
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);


        X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
        Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
        Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

        // XYZ to Lab
        xr = X / Xr;
        yr = Y / Yr;
        zr = Z / Zr;

        if (xr > eps)
            fx = (float) Math.pow(xr, 1 / 3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if (yr > eps)
            fy = (float) Math.pow(yr, 1 / 3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if (zr > eps)
            fz = (float) Math.pow(zr, 1 / 3.);
        else
            fz = (float) ((k * zr + 16.) / 116);

        Ls = (116 * fy) - 16;
        as = 500 * (fx - fy);
        bs = 200 * (fy - fz);

        int[] lab = new int[3];

        lab[0] = (int) (2.55 * Ls + .5);
        lab[1] = (int) (as + .5);
        lab[2] = (int) (bs + .5);
        return lab;
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * metrics.density;

    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return px / metrics.density;
    }

    /**
     * Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}.
     */
    public static float lerp(float startValue, float endValue, float fraction) {
        return startValue + (fraction * (endValue - startValue));
    }

    public static int lerp(int startValue, int endValue, float fraction) {
        return startValue + Math.round(fraction * (endValue - startValue));
    }

    public static class AnimationListenerAdapter implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    public static int getActionbarSize(Context context) {
        int[] abSzAttr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            abSzAttr = new int[]{android.R.attr.actionBarSize};
        } else {
            abSzAttr = new int[]{R.attr.actionBarSize};
        }
        TypedArray a = context.obtainStyledAttributes(abSzAttr);
        return a.getDimensionPixelSize(0, -1);
    }

    public static void copyInputStreamToFile(InputStream in, File file) throws IOException {
        OutputStream out = new FileOutputStream(file);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        in.close();
    }
}
