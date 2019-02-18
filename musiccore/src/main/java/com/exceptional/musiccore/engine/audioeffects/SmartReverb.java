package com.exceptional.musiccore.engine.audioeffects;

import android.media.audiofx.PresetReverb;

import java.util.List;

/**
 * Created by darken on 08.11.2014.
 */
public interface SmartReverb extends SmartEffect {

    public void setReverbSettings(PresetReverb.Settings settings);

    public PresetReverb.Settings getReverbSettings();

    public void setReverb(SmartReverbImpl.Preset reverbPreset);

    public SmartReverbImpl.Preset getReverb();

    public List<SmartReverbImpl.Preset> getPresets();
}
