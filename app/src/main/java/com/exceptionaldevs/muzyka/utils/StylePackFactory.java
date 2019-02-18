package com.exceptionaldevs.muzyka.utils;

import android.content.Context;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.util.Pair;
import com.exceptionaldevs.muzyka.R;

/**
 * Created by sebnap on 22.11.15.
 */
public class StylePackFactory {
    public static String VIBRANT = "Vibrant";
    public static String VIBRANT_DARK = "Vibrant_Dark";
    public static String VIBRANT_LIGHT = "Vibrant_Light";
    public static String MUTED = "Muted";
    public static String MUTED_DARK = "Muted_Dark";
    public static String MUTED_LIGHT = "Muted_Light";

    public static StylePack fromPalette(StylePack fromStylepack, Palette palette) {
        StylePack stylepack = new StylePack(fromStylepack);
        Pair<String, Palette.Swatch> primarySwatch = generatePrimaryColor(palette);

        if (primarySwatch.second != null) {
            String primaryUsed = primarySwatch.first;
            stylepack.setIsFromPalette(true);
            stylepack.setColorPrimary(primarySwatch.second.getRgb());

//          The different text colors roughly match up to the material design standard styles of the same name.
//          The title text color will be more translucent as the text is larger and thus requires less color contrast.
//          The body text color will be more opaque as text is smaller and thus requires more contrast from color.
            stylepack.setTextColorPrimary(primarySwatch.second.getTitleTextColor());
            stylepack.setTextColorSecondary(primarySwatch.second.getBodyTextColor());

            boolean foundPrimaryDark = false;
            if (primaryUsed != VIBRANT_DARK || primaryUsed != MUTED_DARK) {
                Pair<String, Palette.Swatch> primaryDarkSwatch = generatePrimaryDarkColor(palette);
                if (primaryDarkSwatch.second != null) {
                    stylepack.setColorPrimaryDark(primaryDarkSwatch.second.getRgb());
                    stylepack.setTintColor(primaryDarkSwatch.second.getRgb());
                    foundPrimaryDark = true;
                }
            }
            if (!foundPrimaryDark) {
                final int darkerColor = generateDarkerColor(primarySwatch.second);
                stylepack.setColorPrimaryDark(darkerColor);
                stylepack.setTintColor(darkerColor);
            }
        }

        stylepack.setAccentColor(AccentColorFactory.fromColor(stylepack.getColorPrimary()));

        stylepack.setLabel("StylePackFactory:fromPalette");
        return stylepack;
    }

    public static StylePack fromPalette(Context context, Palette palette) {
        return fromPalette(getDefaultStylePack(context), palette);
    }

    public static StylePack getDefaultStylePack(Context context) {
        return new StylePack(context, R.style.StylePackBlueGrey);
    }

    public static Pair<String, Palette.Swatch> generatePrimaryColor(Palette palette) {
        Palette.Swatch swatch;
        swatch = palette.getVibrantSwatch();
        String used = VIBRANT;
        if (swatch == null) {
            swatch = palette.getDarkVibrantSwatch();
            used = VIBRANT_DARK;
        }
        if (swatch == null) {
            swatch = palette.getMutedSwatch();
            used = MUTED;
        }
        if (swatch == null) {
            swatch = palette.getDarkMutedSwatch();
            used = MUTED_DARK;
        }
        return new Pair<>(used, swatch);
    }

    public static Pair<String, Palette.Swatch> generatePrimaryDarkColor(Palette palette) {
        Palette.Swatch swatch;
        swatch = palette.getDarkVibrantSwatch();
        String used = VIBRANT_DARK;
        if (swatch == null) {
            swatch = palette.getDarkMutedSwatch();
            used = MUTED_DARK;
        }
        return new Pair<>(used, swatch);
    }

    public static int generateDarkerColor(Palette.Swatch primary) {
        float[] hsl = primary.getHsl();
        hsl[2] = Utils.constrainFloatInRange(hsl[2] - 0.2f, 0, 1);
        return ColorUtils.HSLToColor(hsl);
    }
}
