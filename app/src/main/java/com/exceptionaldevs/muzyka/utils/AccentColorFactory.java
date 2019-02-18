package com.exceptionaldevs.muzyka.utils;

import android.content.Context;
import android.graphics.Color;
import com.exceptionaldevs.muzyka.R;

import java.util.ArrayList;

/**
 * Created by sebnap on 23.11.15.
 */
public class AccentColorFactory {

    private static ColorNode material_red_500;
    private static ColorNode material_pink_500;
    private static ColorNode material_purple_500;
    private static ColorNode material_deep_purple_500;
    private static ColorNode material_indigo_500;
    private static ColorNode material_blue_500;
    private static ColorNode material_light_blue_500;
    private static ColorNode material_cyan_500;
    private static ColorNode material_teal_500;
    private static ColorNode material_green_500;
    private static ColorNode material_light_green_500;
    private static ColorNode material_lime_500;
    private static ColorNode material_yellow_500;
    private static ColorNode material_amber_500;
    private static ColorNode material_orange_500;
    private static ColorNode material_brown_500;
    private static ColorNode material_grey_500;
    private static ColorNode material_blue_grey_500;

    private static ColorNode material_red_500_accent;
    private static ColorNode material_pink_500_accent;
    private static ColorNode material_purple_500_accent;
    private static ColorNode material_deep_purple_500_accent;
    private static ColorNode material_indigo_500_accent;
    private static ColorNode material_blue_500_accent;
    private static ColorNode material_light_blue_500_accent;
    private static ColorNode material_cyan_500_accent;
    private static ColorNode material_teal_500_accent;
    private static ColorNode material_green_500_accent;
    private static ColorNode material_light_green_500_accent;
    private static ColorNode material_lime_500_accent;
    private static ColorNode material_yellow_500_accent;
    private static ColorNode material_amber_500_accent;
    private static ColorNode material_orange_500_accent;
    private static ColorNode material_brown_500_accent;
    private static ColorNode material_grey_500_accent;
    private static ColorNode material_blue_grey_500_accent;
    private static ArrayList<ColorNode> mColors;
    private static ArrayList<ColorNode> mAccentColors;

    public static class ColorNode {

        private final int red, grn, blu;

        private float[] hsv;

        ColorNode(int rgb) {
            this.red = Color.red(rgb);
            this.grn = Color.green(rgb);
            this.blu = Color.blue(rgb);
        }

        ColorNode(int red, int grn, int blu) {
            this.red = red;
            this.grn = grn;
            this.blu = blu;
        }

        public int getRgb() {
            return Color.rgb(red, grn, blu);
        }

        public float[] getHsv() {
            if (hsv == null) {
                hsv = new float[3];
                Color.RGBToHSV(red, grn, blu, hsv);
            }
            return hsv;
        }

        int distance2(ColorNode node) {
            return distance2(node.red, node.grn, node.blu);
        }

        int distance2(int color) {
            return distance2(Color.red(color), Color.green(color), Color.blue(color));
        }

        int distance2(int red, int grn, int blu) {
            // returns the squared distance between (red, grn, blu)
            // and this this color
            int dr = this.red - red;
            int dg = this.grn - grn;
            int db = this.blu - blu;
            return dr * dr + dg * dg + db * db;
        }

        public String toString() {
            return new StringBuilder(getClass().getSimpleName())
                    .append(" #").append(Integer.toHexString(getRgb()))
                    .toString();
        }
    }

    public static void initAccentColorFactory(Context context) {
        material_red_500 = new ColorNode(context.getResources().getColor(R.color.material_red_500));
        material_pink_500 = new ColorNode(context.getResources().getColor(R.color.material_pink_500));
        material_purple_500 = new ColorNode(context.getResources().getColor(R.color.material_purple_500));
        material_deep_purple_500 = new ColorNode(context.getResources().getColor(R.color.material_deep_purple_500));
        material_indigo_500 = new ColorNode(context.getResources().getColor(R.color.material_indigo_500));
        material_blue_500 = new ColorNode(context.getResources().getColor(R.color.material_blue_500));
        material_light_blue_500 = new ColorNode(context.getResources().getColor(R.color.material_light_blue_500));
        material_cyan_500 = new ColorNode(context.getResources().getColor(R.color.material_cyan_500));
        material_teal_500 = new ColorNode(context.getResources().getColor(R.color.material_teal_500));
        material_green_500 = new ColorNode(context.getResources().getColor(R.color.material_green_500));
        material_light_green_500 = new ColorNode(context.getResources().getColor(R.color.material_light_green_500));
        material_lime_500 = new ColorNode(context.getResources().getColor(R.color.material_lime_500));
        material_yellow_500 = new ColorNode(context.getResources().getColor(R.color.material_yellow_500));
        material_amber_500 = new ColorNode(context.getResources().getColor(R.color.material_amber_500));
        material_orange_500 = new ColorNode(context.getResources().getColor(R.color.material_orange_500));
        material_brown_500 = new ColorNode(context.getResources().getColor(R.color.material_brown_500));
        material_grey_500 = new ColorNode(context.getResources().getColor(R.color.material_grey_500));
        material_blue_grey_500 = new ColorNode(context.getResources().getColor(R.color.material_blue_grey_500));

        material_red_500_accent = new ColorNode(context.getResources().getColor(R.color.material_yellow_A400));
        material_pink_500_accent = new ColorNode(context.getResources().getColor(R.color.material_yellow_A400));
        material_purple_500_accent = new ColorNode(context.getResources().getColor(R.color.material_cyan_A400));
        material_deep_purple_500_accent = new ColorNode(context.getResources().getColor(R.color.material_cyan_A400));
        material_indigo_500_accent = new ColorNode(context.getResources().getColor(R.color.material_pink_A400));
        material_blue_500_accent = new ColorNode(context.getResources().getColor(R.color.material_pink_A400));
        material_light_blue_500_accent = new ColorNode(context.getResources().getColor(R.color.material_pink_A400));
        material_cyan_500_accent = new ColorNode(context.getResources().getColor(R.color.material_pink_A400));
        material_teal_500_accent = new ColorNode(context.getResources().getColor(R.color.material_deep_orange_A400));
        material_green_500_accent = new ColorNode(context.getResources().getColor(R.color.material_orange_A400));
        material_light_green_500_accent = new ColorNode(context.getResources().getColor(R.color.material_amber_A400));
        material_lime_500_accent = new ColorNode(context.getResources().getColor(R.color.material_light_blue_A400));
        material_yellow_500_accent = new ColorNode(context.getResources().getColor(R.color.material_red_A400));
        material_amber_500_accent = new ColorNode(context.getResources().getColor(R.color.material_red_A400));
        material_orange_500_accent = new ColorNode(context.getResources().getColor(R.color.material_light_blue_A400));
        material_brown_500_accent = new ColorNode(context.getResources().getColor(R.color.material_yellow_A400));
        material_grey_500_accent = new ColorNode(context.getResources().getColor(R.color.material_pink_A400));
        material_blue_grey_500_accent = new ColorNode(context.getResources().getColor(R.color.material_pink_A400));

        mColors = new ArrayList<ColorNode>();
        mColors.add(material_red_500);
        mColors.add(material_pink_500);
        mColors.add(material_purple_500);
        mColors.add(material_deep_purple_500);
        mColors.add(material_indigo_500);
        mColors.add(material_blue_500);
        mColors.add(material_light_blue_500);
        mColors.add(material_cyan_500);
        mColors.add(material_teal_500);
        mColors.add(material_green_500);
        mColors.add(material_light_green_500);
        mColors.add(material_lime_500);
        mColors.add(material_yellow_500);
        mColors.add(material_amber_500);
        mColors.add(material_orange_500);
        mColors.add(material_brown_500);
        mColors.add(material_grey_500);
        mColors.add(material_blue_grey_500);

        mAccentColors = new ArrayList<ColorNode>();
        mAccentColors.add(material_red_500_accent);
        mAccentColors.add(material_pink_500_accent);
        mAccentColors.add(material_purple_500_accent);
        mAccentColors.add(material_deep_purple_500_accent);
        mAccentColors.add(material_indigo_500_accent);
        mAccentColors.add(material_blue_500_accent);
        mAccentColors.add(material_light_blue_500_accent);
        mAccentColors.add(material_cyan_500_accent);
        mAccentColors.add(material_teal_500_accent);
        mAccentColors.add(material_green_500_accent);
        mAccentColors.add(material_light_green_500_accent);
        mAccentColors.add(material_lime_500_accent);
        mAccentColors.add(material_yellow_500_accent);
        mAccentColors.add(material_amber_500_accent);
        mAccentColors.add(material_orange_500_accent);
        mAccentColors.add(material_brown_500_accent);
        mAccentColors.add(material_grey_500_accent);
        mAccentColors.add(material_blue_grey_500_accent);
    }

    public static int fromColor(int color) {
        if(mColors == null){
            throw new RuntimeException("Did you initialize AccentColorFactory?"); //         AccentColorFactory.initAccentColorFactory(this);
        }

        if(mColors.size() != mAccentColors.size()){
            throw new RuntimeException("Did u mess up the Factory?");
        }

        int min_distance = Integer.MAX_VALUE;
        int min_position = 0;
        for (int i = 0; i < mColors.size(); i++) {
            int distance = mColors.get(i).distance2(color);
            if (distance < min_distance) {
                min_distance = distance;
                min_position = i;
            }
        }
        return mAccentColors.get(min_position).getRgb();
    }

}
