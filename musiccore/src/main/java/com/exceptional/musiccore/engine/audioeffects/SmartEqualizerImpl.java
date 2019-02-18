package com.exceptional.musiccore.engine.audioeffects;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.os.Build;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.utils.ApiHelper;
import com.exceptional.musiccore.utils.Logy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by darken on 08.11.2014.
 */
public class SmartEqualizerImpl extends SmartEffectImpl<Equalizer> implements SmartEqualizer {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "SmartEqualizer";
    private List<Preset> mPresets;
    private List<Band> mBands;
    private Preset mCurrentPreset;
    private Preset mUserDefinedPreset = new Preset("User defined", (short) 0, true);

    protected SmartEqualizerImpl(Context context) {
        super(context, Equalizer.class);
    }

    @Override
    public void setEqualizerSettings(Equalizer.Settings settings) {

    }

    @Override
    public Equalizer.Settings getEqualizerSettings() {
        return null;
    }

    @Override
    public void onSetupNewEffect(Equalizer effect) {
        for (Band band : getBands()) {
            _setBandLevel(effect, band);
        }
    }

    @Override
    public List<Band> getBands() {
        if (mBands == null) {
            mBands = new ArrayList<>();
            for (short i = 0; i < getSomeEffect().getNumberOfBands(); i++) {
                int centerFreq = getSomeEffect().getCenterFreq(i);
                int freqRange[] = getSomeEffect().getBandFreqRange(i);
                short ampRange[] = getSomeEffect().getBandLevelRange();
                Band band = new Band(i, freqRange, centerFreq, ampRange);
                mBands.add(band);
            }
        }
        return new ArrayList<>(mBands);
    }

    @Override
    public void setBandStrength(Band band, float strengthLevel) {
        setPreset(mUserDefinedPreset);
        int bandIndex = mBands.indexOf(band);
        Logy.d(TAG, "Setting level (" + strengthLevel + ") for band [" + band + "]");
        if (bandIndex < 0)
            return;

        if (strengthLevel > 1)
            strengthLevel = 1;
        if (strengthLevel < -1)
            strengthLevel = -1;

        band.setCurrentLevel(strengthLevel);
        for (Equalizer e : getEffects())
            _setBandLevel(e, band);
    }

    private void _setBandLevel(Equalizer eq, Band band) {
        short[] ampRange = band.getAmplificationRange();
        if (band.getCurrentLevel() > 0) {
            eq.setBandLevel(band.getBandIndex(), (short) (Math.abs(band.getCurrentLevel()) * ampRange[1]));
        } else {
            eq.setBandLevel(band.getBandIndex(), (short) (Math.abs(band.getCurrentLevel()) * ampRange[0]));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected UUID onSupplyEffectTypeUUID() {
        if (ApiHelper.hasJellyBeanMR2()) {
            return Equalizer.EFFECT_TYPE_EQUALIZER;
        } else {
            try {
                Field effectTypeField = AudioEffect.class.getDeclaredField("EFFECT_TYPE_EQUALIZER");
                return (UUID) effectTypeField.get(null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public List<Preset> getPresets() {
        if (mPresets == null) {
            mPresets = new ArrayList<Preset>();
            for (short i = 0; i < getSomeEffect().getNumberOfPresets(); i++) {
                String presetName = getSomeEffect().getPresetName(i);
                mPresets.add(new Preset(presetName, i, false));
            }
            mPresets.add(mUserDefinedPreset);
        }
        return mPresets;
    }

    @Override
    public Preset getCurrentPreset() {
        return mCurrentPreset;
    }

    @Override
    public void setPreset(Preset preset) {
        mCurrentPreset = preset;
        if (!preset.isCustom()) {
            if (getEffects().isEmpty()) {
                getTempEffect().usePreset(preset.getPresetID());
            } else {
                for (Equalizer effect : getEffects()) {
                    effect.usePreset(preset.getPresetID());
                }
            }
            for (Band band : getBands()) {
                float strengthLevel = 0;
                short bandLevel = getSomeEffect().getBandLevel(band.getBandIndex());
                if (bandLevel < 0) {
                    strengthLevel = (float) bandLevel / band.getAmplificationRange()[0];
                } else if (bandLevel > 0) {
                    strengthLevel = (float) bandLevel / band.getAmplificationRange()[1];
                }
                band.setCurrentLevel(strengthLevel);
            }
        }
    }

}
