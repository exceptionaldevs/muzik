package com.exceptional.musiccore.engine.audioeffects;

import android.content.Context;
import android.media.audiofx.AudioEffect;

import com.exceptional.musiccore.BuildConfig;
import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.utils.Logy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by darken on 08.11.2014.
 */
public abstract class SmartEffectImpl<T extends AudioEffect> implements SmartEffect {
    protected static final int TEMP_AUDIOSESSION = 77;
    protected static final int TEMP_PRIORITY = -1;
    private static final int DEFAULT_PRIORITY = 0;
    private int mPriority = DEFAULT_PRIORITY;
    private final List<T> mEffects = new ArrayList<>();
    private final Context mContext;
    private final Class<T> mEffectClass;
    private T mTempEffect;
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "SmartEffect";
    private Boolean mAvailable;
    private Map<Integer, T> mSessionMap = new HashMap<>();
    private boolean mEnabled = false;
    private Integer mEffectEnableState;

    protected SmartEffectImpl(Context context, Class<T> effectClass) {
        mContext = context;
        mEffectClass = effectClass;
    }

    protected Context getContext() {
        return mContext;
    }

    protected int getPriority() {
        return mPriority;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
        for (T effect : mEffects) {
            mEffectEnableState = effect.setEnabled(enabled);
        }
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    protected void addSession(int audioSessionID) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (hasSession(audioSessionID)) {
            Logy.d(TAG, "Session already exists:" + audioSessionID);
            return;
        }
        Logy.d(TAG, "Adding session:" + audioSessionID);
//        if (BuildConfig.DEBUG && mSessionMap.containsKey(audioSessionID)) {
//            throw new RuntimeException("Trying to add existing ID(" + audioSessionID + ")??");
//        }
        T newEffect = makeEffect(DEFAULT_PRIORITY, audioSessionID);
        onSetupNewEffect(newEffect);
        newEffect.setEnabled(isEnabled());
        mSessionMap.put(audioSessionID, newEffect);
        mEffects.add(newEffect);
        if (mTempEffect != null && !mEffects.isEmpty()) {
            mTempEffect.release();
            mTempEffect = null;
        }
    }

    protected boolean hasSession(int audioSessionID) {
        return mSessionMap.containsKey(audioSessionID);
    }

    protected abstract void onSetupNewEffect(T effect);

    protected List<T> getEffects() {
        return mEffects;
    }

    protected T getSomeEffect() {
        if (mEffects.isEmpty()) {
            return getTempEffect();
        } else {
            return mEffects.get(0);
        }
    }

    protected void releaseSession(int audioSessionID) {
        Logy.d(TAG, "Releasing session:" + audioSessionID);
        T toRelease = mSessionMap.get(audioSessionID);
        if (toRelease != null) {
            mEffects.remove(toRelease);
            toRelease.release();
        }
    }

    @Override
    public void releaseAll() {
        for (T effect : mEffects) {
            effect.release();
        }
        if (mTempEffect != null)
            mTempEffect.release();
    }

    protected T getTempEffect() {
        if (mTempEffect == null) {
            try {
                mTempEffect = makeEffect(TEMP_PRIORITY, TEMP_AUDIOSESSION);
            } catch (Exception e) {
                e.printStackTrace();
                Logy.e(TAG, "Couldn't instantiate TEMPEFFECT, audiosessionID issue?");
                if (BuildConfig.DEBUG) {
                    throw new RuntimeException("Couldn't instantiate TEMPEFFECT, audiosessionID issue?");
                } else {
                    try {
                        mTempEffect = makeEffect(TEMP_PRIORITY, 0);
                    } catch (Exception lastStraw) {
                        lastStraw.printStackTrace();
                    }
                }
            }
        }
        return mTempEffect;
    }

    private T makeEffect(int priority, int audioSession) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return mEffectClass.getConstructor(int.class, int.class).newInstance(priority, audioSession);
    }

    @Override
    public boolean isAvailable() {
        if (mAvailable != null) {
            return mAvailable;
        } else {
            UUID targetEffectType;
            if (!getEffects().isEmpty()) {
                targetEffectType = getEffects().get(0).getDescriptor().type;
            } else {
                targetEffectType = onSupplyEffectTypeUUID();
                if (targetEffectType == null) {
                    AudioEffect tempEffect = getTempEffect();
                    if (tempEffect != null) {
                        targetEffectType = tempEffect.getDescriptor().type;
                    }
                }
            }

            AudioEffect.Descriptor[] availableEffects = AudioEffect.queryEffects();
            if (availableEffects != null) {
                for (AudioEffect.Descriptor ef : availableEffects) {
                    if (ef.type.equals(targetEffectType)) {
                        mAvailable = true;
                        break;
                    }
                }
            }
        }
        if (mAvailable == null)
            mAvailable = false;
        return mAvailable;
    }

    protected abstract UUID onSupplyEffectTypeUUID();

}
