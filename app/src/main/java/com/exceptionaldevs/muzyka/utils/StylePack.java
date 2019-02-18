package com.exceptionaldevs.muzyka.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import com.exceptionaldevs.muzyka.R;


/**
 * Created by darken on 05.11.2014.
 */
public class StylePack implements Parcelable {
    public static final String ARG_KEY = "stylepack";
    private String mLabel;
    private int mMainIconResource;
    private int mColorPrimary;
    private int mColorPrimaryDark;
    private int mTextColorPrimary;
    private int mTextColorSecondary;
    private int mTintColor;
    private int mAccentColor;
    private boolean mIsFromPalette;

    public StylePack() {

    }

    public StylePack(StylePack stylePack) {
        mLabel = stylePack.getLabel();
        mMainIconResource = stylePack.getMainIconResource();
        mColorPrimary = stylePack.getColorPrimary();
        mColorPrimaryDark = stylePack.getColorPrimaryDark();
        mTextColorPrimary = stylePack.getTextColorPrimary();
        mTextColorSecondary = stylePack.getTextColorSecondary();
        mAccentColor = stylePack.getAccentColor();
        mTintColor = stylePack.getTintColor();
        mIsFromPalette = stylePack.isFromPalette();
    }

    public StylePack(Context context, int stylePackResource) {
        int[] attrs = {
                R.attr.stylePackLabel,
                R.attr.stylePackIconMain,
                R.attr.stylePackColorPrimary,
                R.attr.stylePackColorPrimaryDark,
                R.attr.stylePackTextColorPrimary,
                R.attr.stylePackTextColorSecondary,
                R.attr.stylePackTintColor,
                R.attr.stylePackAccentColor};
        TypedArray typedArray = context.obtainStyledAttributes(stylePackResource, attrs);

        mLabel = typedArray.getString(R.styleable.StylePackResource_stylePackLabel);
        mMainIconResource = typedArray.getResourceId(R.styleable.StylePackResource_stylePackIconMain, -1);
        mColorPrimary = typedArray.getColor(R.styleable.StylePackResource_stylePackColorPrimary, -1);
        mColorPrimaryDark = typedArray.getColor(R.styleable.StylePackResource_stylePackColorPrimaryDark, -1);
        mTextColorPrimary = typedArray.getColor(R.styleable.StylePackResource_stylePackTextColorPrimary, -1);
        mTextColorSecondary = typedArray.getColor(R.styleable.StylePackResource_stylePackTextColorSecondary, -1);
        mTintColor = typedArray.getColor(R.styleable.StylePackResource_stylePackTintColor, -1);
        mAccentColor = typedArray.getColor(R.styleable.StylePackResource_stylePackAccentColor, -1);

        mIsFromPalette = false;
        typedArray.recycle();
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public int getMainIconResource() {
        return mMainIconResource;
    }

    public void setMainIconResource(int mainIconResource) {
        mMainIconResource = mainIconResource;
    }

    public int getColorPrimary() {
        return mColorPrimary;
    }

    public void setColorPrimary(int colorPrimary) {
        mColorPrimary = colorPrimary;
    }

    public int getColorPrimaryDark() {
        return mColorPrimaryDark;
    }

    public void setColorPrimaryDark(int colorPrimaryDark) {
        mColorPrimaryDark = colorPrimaryDark;
    }

    public int getTextColorPrimary() {
        return mTextColorPrimary;
    }

    public void setTextColorPrimary(int textColorPrimary) {
        mTextColorPrimary = textColorPrimary;
    }

    public int getTextColorSecondary() {
        return mTextColorSecondary;
    }

    public void setTextColorSecondary(int textColorSecondary) {
        mTextColorSecondary = textColorSecondary;
    }

    public boolean isFromPalette() {
        return mIsFromPalette;
    }

    public void setIsFromPalette(boolean isFromPalette) {
        this.mIsFromPalette = isFromPalette;
    }

    @ColorInt
    public int getTintColor() {
        return mTintColor;
    }

    public void setTintColor(@ColorInt int tintColor) {
        mTintColor = tintColor;
    }

    public int getAccentColor() {
        return mAccentColor;
    }

    public void setAccentColor(int accentColor) {
        mAccentColor = accentColor;
    }

    protected StylePack(Parcel in) throws ClassNotFoundException {
        mLabel = in.readString();
        mMainIconResource = in.readInt();
        mColorPrimary = in.readInt();
        mColorPrimaryDark = in.readInt();
        mTextColorPrimary = in.readInt();
        mTextColorSecondary = in.readInt();
        mTintColor = in.readInt();
        mAccentColor = in.readInt();
        mIsFromPalette = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mLabel);
        out.writeInt(mMainIconResource);
        out.writeInt(mColorPrimary);
        out.writeInt(mColorPrimaryDark);
        out.writeInt(mTextColorPrimary);
        out.writeInt(mTextColorSecondary);
        out.writeInt(mTintColor);
        out.writeInt(mAccentColor);
        out.writeInt(mIsFromPalette ? 1 : 0);
    }

    public static final Parcelable.Creator<StylePack> CREATOR = new Parcelable.Creator<StylePack>() {
        public StylePack createFromParcel(Parcel in) {
            try {
                return new StylePack(in);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        public StylePack[] newArray(int size) {
            return new StylePack[size];
        }
    };


}
