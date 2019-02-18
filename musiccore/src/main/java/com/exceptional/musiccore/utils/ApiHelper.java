package com.exceptional.musiccore.utils;

import android.os.Build;

/**
 * Helps to check API available on this device.
 * Android 5.0                  21 	LOLLIPOP
 * Android 4.4W                 20 	KITKAT_WATCH
 * Android 4.4 	                19 	KITKAT
 * Android 4.3 	                18 	JELLY_BEAN_MR2
 * Android 4.2, 4.2.2 	        17 	JELLY_BEAN_MR1
 * Android 4.1, 4.1.1 	        16 	JELLY_BEAN
 * Android 4.0.3, 4.0.4 	    15 	ICE_CREAM_SANDWICH_MR1
 * Android 4.0, 4.0.1, 4.0.2    14 	ICE_CREAM_SANDWICH
 */
public class ApiHelper {
    public static final String[] FUCKED_RENDERSCRIPT_VERSIONS = new String[]{
            "4.1.2", "4.2.2"
    };

    /**
     * @param versions i.e. 4.1.2, 4.2.2
     * @return true if this is one of those versions
     */
    public static boolean hasVersion(String[] versions) {
        for (String version : versions)
            if (Build.VERSION.RELEASE.equals(version))
                return true;
        return false;
    }

    /**
     * Has at least ICS (Vanilla) (API14+)
     * ICS API 14 was a very buggy release and almost everyone has the bugfix ICS (API15)
     *
     * @return
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * Has at least ICS (API15+)
     *
     * @return
     */
    public static boolean hasICSMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
    }

    /**
     * Has at least JellyBean (API16+)
     *
     * @return
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * Has at least JellyBean MR1 (API17+)
     *
     * @return
     */
    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Has at least JellyBean MR2 (API18+)
     *
     * @return
     */
    public static boolean hasJellyBeanMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    }

    /**
     * Has at least KitKat (API19+)
     *
     * @return
     */
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * Has at least Lollipop (API21+)
     *
     * @return
     */
    public static boolean hasLolliPop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
