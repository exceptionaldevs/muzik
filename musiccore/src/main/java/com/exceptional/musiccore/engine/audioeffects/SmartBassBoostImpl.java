package com.exceptional.musiccore.engine.audioeffects;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.Build;

import com.exceptional.musiccore.utils.ApiHelper;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Created by darken on 08.11.2014.
 */
public class SmartBassBoostImpl extends SmartEffectImpl<BassBoost> implements SmartBassBoost {

    private final static short INTERNAL_MAX_STRENGTH = 1000;
    private final static short INTERNAL_MIN_STRENGTH = 0;
    private float mCurrentStrengthLevel = 0;

    public SmartBassBoostImpl(Context context) {
        super(context, BassBoost.class);
    }

    @Override
    public void setBassBoostSettings(BassBoost.Settings settings) {

    }

    @Override
    public BassBoost.Settings getBassBoostSettings() {
        return null;
    }

    @Override
    public void onSetupNewEffect(BassBoost effect) {
        SmartBassBoostImpl._setStrength(effect, mCurrentStrengthLevel);
    }

    /**
     * The current bass boost strength.
     * Depending on device support, this may only return 0.0f or 1.0f with no values in between.
     *
     * @return 0.0f MIN - 1.0f MAX
     */
    @Override
    public float getStrength() {
        short strength = getSomeEffect().getRoundedStrength();
        return (float) strength / INTERNAL_MAX_STRENGTH;
    }

    @Override
    public void setBassBoostStrength(float strengthLevel) {
        if (strengthLevel > 1) {
            strengthLevel = 1;
        } else if (strengthLevel < 0) {
            strengthLevel = 0;
        }
        mCurrentStrengthLevel = strengthLevel;
        for (BassBoost e : getEffects()) {
            SmartBassBoostImpl._setStrength(e, strengthLevel);
        }
    }

    private static void _setStrength(BassBoost effect, float strength) {
        short newStrength;
        if (strength > 0) {
            if (strength > 1)
                strength = 1;
            newStrength = (short) (strength * INTERNAL_MAX_STRENGTH);
        } else {
            newStrength = INTERNAL_MIN_STRENGTH;
        }
        effect.setStrength(newStrength);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected UUID onSupplyEffectTypeUUID() {
        if (ApiHelper.hasJellyBeanMR2()) {
            return Equalizer.EFFECT_TYPE_BASS_BOOST;
        } else {
            try {
                Field effectTypeField = AudioEffect.class.getDeclaredField("EFFECT_TYPE_BASS_BOOST");
                return (UUID) effectTypeField.get(null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
