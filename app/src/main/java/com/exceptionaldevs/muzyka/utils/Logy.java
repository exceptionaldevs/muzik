package com.exceptionaldevs.muzyka.utils;

import android.util.Log;

public class Logy {
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int NORMAL = 3;
    public static final int QUIET = 4;
    public static final int SILENT = 5;

    //    public static int sLogLevel = Buildonfig.DEBUG ? VERBOSE : NORMAL;
    public static int sLogLevel = DEBUG;

    public static void v(String c, String s) {
        if (sLogLevel <= VERBOSE)
            Log.v(c, s);
    }

    public static void d(String c, String s) {
        if (sLogLevel <= DEBUG)
            Log.d(c, s);
    }

    public static void i(String c, String s) {
        if (sLogLevel <= NORMAL)
            Log.i(c, s);
    }

    public static void w(String c, String s) {
        if (sLogLevel <= QUIET)
            Log.w(c, s);
    }

    public static void e(String c, String s) {
        if (sLogLevel <= QUIET)
            Log.e(c, s);
    }

    public static boolean isDebug() {
        return sLogLevel < NORMAL;
    }
}
