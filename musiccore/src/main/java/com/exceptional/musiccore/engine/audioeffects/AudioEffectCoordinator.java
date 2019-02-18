package com.exceptional.musiccore.engine.audioeffects;

import android.content.Context;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.utils.Logy;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by darken on 08.11.2014.
 */
public class AudioEffectCoordinator {

    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "AudioEffectCoordinator";

    public enum Effect {EQUALIZER, BASS_BOOST, REVERB, VIRTUALIZER}

    private SmartBassBoostImpl mBassBoost;
    private SmartEqualizerImpl mEqualizer;
    private SmartReverbImpl mReverb;
    private SmartVirtualizerImpl mVirtualizer;

    public AudioEffectCoordinator(Context context) {
        mBassBoost = new SmartBassBoostImpl(context);
        mEqualizer = new SmartEqualizerImpl(context);
        mReverb = new SmartReverbImpl(context);
        mVirtualizer = new SmartVirtualizerImpl(context);
    }

    public boolean isAvailable(Effect effect) {
        if (effect == Effect.EQUALIZER) {
            return mEqualizer.isAvailable();
        } else if (effect == Effect.BASS_BOOST) {
            return mBassBoost.isAvailable();
        } else if (effect == Effect.REVERB) {
            return mReverb.isAvailable();
        } else if (effect == Effect.VIRTUALIZER) {
            return mVirtualizer.isAvailable();
        }
        return false;
    }

    public SmartEffect getEffect(Effect effect) {
        if (effect == Effect.EQUALIZER) {
            return mEqualizer;
        } else if (effect == Effect.BASS_BOOST) {
            return mBassBoost;
        } else if (effect == Effect.REVERB) {
            return mReverb;
        } else if (effect == Effect.VIRTUALIZER) {
            return mVirtualizer;
        }
        return null;
    }

    public void addSession(int audioSessionID) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Logy.d(TAG, "Adding session:" + audioSessionID);
        if (mEqualizer.isAvailable()) {
            mEqualizer.addSession(audioSessionID);
        }
        if (mBassBoost.isAvailable()) {
            mBassBoost.addSession(audioSessionID);
        }
        if (mReverb.isAvailable()) {
            mReverb.addSession(audioSessionID);
        }
        if (mVirtualizer.isAvailable()) {
            mVirtualizer.addSession(audioSessionID);
        }
    }

    public void releaseSession(int audioSessionID) {
        Logy.d(TAG, "Releasing session:" + audioSessionID);
        if (mEqualizer.isAvailable()) {
            mEqualizer.releaseSession(audioSessionID);
        }
        if (mBassBoost.isAvailable()) {
            mBassBoost.releaseSession(audioSessionID);
        }
        if (mReverb.isAvailable()) {
            mReverb.releaseSession(audioSessionID);
        }
        if (mVirtualizer.isAvailable()) {
            mVirtualizer.releaseSession(audioSessionID);
        }
    }

    public void releaseAll() {
        Logy.d(TAG, "Releasing all sessions");
        mBassBoost.releaseAll();
        mEqualizer.releaseAll();
        mReverb.releaseAll();
        mVirtualizer.releaseAll();
    }

}
