package com.exceptional.musiccore.engine.audioeffects;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.os.Build;

import com.exceptional.musiccore.R;
import com.exceptional.musiccore.utils.ApiHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by darken on 08.11.2014.
 */
public class SmartReverbImpl extends SmartEffectImpl<PresetReverb> implements SmartReverb {

    private final List<Preset> mPresets = new ArrayList<Preset>();
    private Preset mCurrentPreset;

    public SmartReverbImpl(Context context) {
        super(context, PresetReverb.class);
        mPresets.add(new Preset(Preset.Type.LARGEHALL, context.getString(R.string.reverb_large_hall)));
        mPresets.add(new Preset(Preset.Type.MEDIUMHALL, context.getString(R.string.reverb_medium_hall)));
        mPresets.add(new Preset(Preset.Type.LARGERROOM, context.getString(R.string.reverb_large_room)));
        mPresets.add(new Preset(Preset.Type.SMALLROOM, context.getString(R.string.reverb_small_room)));
        mPresets.add(new Preset(Preset.Type.PLATE, context.getString(R.string.reverb_plate)));
        mPresets.add(new Preset(Preset.Type.NONE, context.getString(R.string.effect_type_none)));
        mCurrentPreset = mPresets.get(0);
    }

    @Override
    public void setReverbSettings(PresetReverb.Settings settings) {

    }

    @Override
    public PresetReverb.Settings getReverbSettings() {
        return null;
    }

    @Override
    public void onSetupNewEffect(PresetReverb effect) {
        _setReverb(effect, mCurrentPreset);
    }

    @Override
    public void setReverb(Preset reverbPreset) {
        mCurrentPreset = reverbPreset;
        for (PresetReverb e : getEffects()) {
            _setReverb(e, reverbPreset);
        }
    }

    private void _setReverb(PresetReverb effect, Preset preset) {
        if (preset.getType() != Preset.Type.CUSTOM) {
            short presetCode = getPresetForType(preset.getType());
            if (presetCode != -1) {
                effect.setPreset(presetCode);
            }
        } else {
            // TODO
        }
    }

    @Override
    public Preset getReverb() {
        return mCurrentPreset;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected UUID onSupplyEffectTypeUUID() {
        if (ApiHelper.hasJellyBeanMR2()) {
            return Equalizer.EFFECT_TYPE_PRESET_REVERB;
        } else {
            try {
                Field effectTypeField = AudioEffect.class.getDeclaredField("EFFECT_TYPE_PRESET_REVERB");
                return (UUID) effectTypeField.get(null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override
    public List<Preset> getPresets() {
        return mPresets;
    }

    public static short getPresetForType(Preset.Type type) {
        if (type == Preset.Type.LARGEHALL) {
            return PresetReverb.PRESET_LARGEHALL;
        } else if (type == Preset.Type.LARGERROOM) {
            return PresetReverb.PRESET_LARGEROOM;
        } else if (type == Preset.Type.MEDIUMHALL) {
            return PresetReverb.PRESET_MEDIUMHALL;
        } else if (type == Preset.Type.PLATE) {
            return PresetReverb.PRESET_PLATE;
        } else if (type == Preset.Type.SMALLROOM) {
            return PresetReverb.PRESET_SMALLROOM;
        } else if (type == Preset.Type.NONE) {
            return PresetReverb.PRESET_NONE;
        }
        return -1;
    }

    public static class Preset {
        private final String mName;
        private final Type mType;

        enum Type {LARGEHALL, LARGERROOM, MEDIUMHALL, PLATE, SMALLROOM, NONE, CUSTOM;}

        public Preset(Type type, String name) {
            mName = name;
            mType = type;
        }

        public String getName() {
            return mName;
        }

        public Type getType() {
            return mType;
        }
    }
}
