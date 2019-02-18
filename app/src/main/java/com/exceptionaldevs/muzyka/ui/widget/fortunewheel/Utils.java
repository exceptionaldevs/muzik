package com.exceptionaldevs.muzyka.ui.widget.fortunewheel;

/**
 * Created by Miroslaw Stanek on 21.12.2015.
 */
public class Utils {
    public static double mapValueFromRangeToRange(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
        return toLow + ((value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow));
    }

    public static double clamp(double value, double low, double high) {
        return Math.min(Math.max(value, low), high);
    }

    public static int clamp(int value, int low, int high) {
        return Math.min(Math.max(value, low), high);
    }
}
