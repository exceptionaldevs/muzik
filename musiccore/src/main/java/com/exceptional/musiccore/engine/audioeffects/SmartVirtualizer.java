package com.exceptional.musiccore.engine.audioeffects;

import android.media.audiofx.Virtualizer;

/**
 * Created by darken on 08.11.2014.
 */
public interface SmartVirtualizer extends SmartEffect {

    public void setVirtualizerSettings(Virtualizer.Settings settings);

    public Virtualizer.Settings getVirtualizerSettings();

    public void setVirtualizerStrength(float strength);

    public float getStrength();
}
