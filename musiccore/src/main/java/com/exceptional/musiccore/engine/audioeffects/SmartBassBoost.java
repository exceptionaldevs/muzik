package com.exceptional.musiccore.engine.audioeffects;

import android.media.audiofx.BassBoost;

/**
 * Created by darken on 08.11.2014.
 */
public interface SmartBassBoost extends SmartEffect {

    public void setBassBoostSettings(BassBoost.Settings settings);

    public BassBoost.Settings getBassBoostSettings();

    public void setBassBoostStrength(float strength);

    public float getStrength();

}
