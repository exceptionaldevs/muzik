package com.exceptional.musiccore.engine.audioeffects;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Virtualizer;
import android.os.Build;

import com.exceptional.musiccore.utils.ApiHelper;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Created by darken on 08.11.2014.
 */
public class SmartVirtualizerImpl extends SmartEffectImpl<Virtualizer> implements SmartVirtualizer {
    private final static short INTERNAL_MAX_STRENGTH = 1000;
    private final static short INTERNAL_MIN_STRENGTH = 0;
    private float mCurrentStrengthLevel = 0;

    public SmartVirtualizerImpl(Context context) {
        super(context, Virtualizer.class);
    }

    @Override
    public void setVirtualizerSettings(Virtualizer.Settings settings) {

    }

    @Override
    public void onSetupNewEffect(Virtualizer effect) {
        SmartVirtualizerImpl._setStrength(effect, mCurrentStrengthLevel);
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
    public void setVirtualizerStrength(float strengthLevel) {
        if (strengthLevel > 1) {
            strengthLevel = 1;
        } else if (strengthLevel < 0) {
            strengthLevel = 0;
        }

        mCurrentStrengthLevel = strengthLevel;
        for (Virtualizer e : getEffects()) {
            SmartVirtualizerImpl._setStrength(e, strengthLevel);
        }
    }

    private static void _setStrength(Virtualizer effect, float strength) {
        short toSetStrength;
        if (strength > 0) {
            toSetStrength = (short) (strength * INTERNAL_MAX_STRENGTH);
        } else {
            toSetStrength = INTERNAL_MIN_STRENGTH;
        }
        effect.setStrength(toSetStrength);
    }

    @Override
    public Virtualizer.Settings getVirtualizerSettings() {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected UUID onSupplyEffectTypeUUID() {
        if (ApiHelper.hasJellyBeanMR2()) {
            return Equalizer.EFFECT_TYPE_VIRTUALIZER;
        } else {
            try {
                Field effectTypeField = AudioEffect.class.getDeclaredField("EFFECT_TYPE_VIRTUALIZER");
                return (UUID) effectTypeField.get(null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
