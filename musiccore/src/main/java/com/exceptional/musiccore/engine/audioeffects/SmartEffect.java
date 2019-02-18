package com.exceptional.musiccore.engine.audioeffects;

/**
 * Created by darken on 11.12.2014.
 */
public interface SmartEffect {
    public void setEnabled(boolean enabled);

    public boolean isEnabled();

    public void releaseAll();

    public boolean isAvailable();
}
