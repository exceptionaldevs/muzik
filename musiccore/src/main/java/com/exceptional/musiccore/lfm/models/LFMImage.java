package com.exceptional.musiccore.lfm.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by sebnap on 01.12.15.
 */
public class LFMImage {
    public static String SIZE_SMALL = "small";
    public static String SIZE_MEDIUM = "medium";
    public static String SIZE_LARGE = "large";
    public static String SIZE_EXTRALARGE = "extralarge";
    public static String SIZE_MEGA = "mega";

    @SerializedName("#text")
    String url;

    String size;

    public String getUrl() {
        return url;
    }

    public String getSize() {
        return size;
    }
}