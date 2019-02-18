package com.exceptional.musiccore.glide;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;

/**
 * Created by sebnap on 06.12.15.
 */
public class GenerateCover implements Key {
    private String mLetters = "";
    private int mBackgroundColor = 0xff607d8b;
    private int mTextColor = 0xFFFFFFFF;

    public GenerateCover() {
    }

    public GenerateCover(String letters, int backgroundColor) {
        setLetterToFirst(letters);
        mBackgroundColor = backgroundColor;
    }

    public void setLetters(String letters) {
        mLetters = letters;
    }

    public void setLetterToFirst(String letters) {
        if (letters != null && !letters.isEmpty()) {
            mLetters = letters.substring(0, 1);
        }
    }

    public String getLetters() {
        return mLetters;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public static int colorFromHash(int hash) {
        return ANDROID_COLORSETS[(Math.abs(hash) % ANDROID_COLORSETS.length)];
    }

    private final static int ANDROID_COLORSETS[] = {
            0xfff44336,
            0xffe91e63,
            0xff9c27b0,
            0xff673ab7,
            0xff3f51b5,
            0xff2196f3,
            0xff03a9f4,
            0xff00bcd4,
            0xff009688,
            0xff4caf50,
            0xff8bc34a,
            0xffff9800,
            0xffff5722,
            0xff795548,
            0xff607d8b,
    };

    @Override
    public String toString() {
        return "GenerateCover{"
                + "mLetters=" + mLetters
                + "mBackgroundColor=" + mBackgroundColor
                + "mTextColor=" + mTextColor
                + '}';

    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(toString().getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GenerateCover))
            return false;

        GenerateCover foo = (GenerateCover) o;
        if (!mLetters.equals(foo.mLetters))
            return false;
        if (mBackgroundColor != foo.mBackgroundColor)
            return false;
        if (mTextColor != foo.mTextColor)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + mLetters.hashCode();
        hash = hash * 31 + mBackgroundColor;
        hash = hash * 31 + mTextColor;
        return hash;
    }
}
