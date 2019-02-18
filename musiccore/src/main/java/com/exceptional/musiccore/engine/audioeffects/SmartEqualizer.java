package com.exceptional.musiccore.engine.audioeffects;

import android.media.audiofx.Equalizer;

import java.util.List;

/**
 * Created by darken on 08.11.2014.
 */
public interface SmartEqualizer extends SmartEffect {

    public void setEqualizerSettings(Equalizer.Settings settings);

    public Equalizer.Settings getEqualizerSettings();

    public void setBandStrength(Band band, float strength);

    public List<Band> getBands();

    public List<SmartEqualizerImpl.Preset> getPresets();

    public SmartEqualizerImpl.Preset getCurrentPreset();

    public void setPreset(SmartEqualizerImpl.Preset preset);

    public static class Band {
        private final short mBandIndex;
        private final int mFrequencyRange[];
        private final int mCenterFrequency;
        private final short mAmplificationRange[];
        private float mCurrentLevel;

        public Band(short bandIndex, int freqRange[], int centerFreq, short ampRange[]) {
            mBandIndex = bandIndex;
            mFrequencyRange = freqRange;
            mCenterFrequency = centerFreq;
            mAmplificationRange = ampRange;
        }

        public short getBandIndex() {
            return mBandIndex;
        }

        public float getCurrentLevel() {
            return mCurrentLevel;
        }

        protected void setCurrentLevel(float newLevel) {
            mCurrentLevel = newLevel;
        }

        public int[] getFrequencyRange() {
            return mFrequencyRange;
        }

        public int getCenterFrequency() {
            return mCenterFrequency;
        }

        public short[] getAmplificationRange() {
            return mAmplificationRange;
        }
    }

    public static class Preset {
        private final String mName;
        private final short mPresetID;
        private final boolean mCustom;

        public Preset(String name, short presetID, boolean customPreset) {
            mName = name;
            mPresetID = presetID;
            mCustom = customPreset;
        }

        public short getPresetID() {
            return mPresetID;
        }

        public String getName() {
            return mName;
        }

        public boolean isCustom() {
            return mCustom;
        }
    }

}
