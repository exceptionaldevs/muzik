package com.exceptional.musiccore.engine.exoplayer;

import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.SampleSource;

/**
 * Created by darken on 20.11.2014.
 */
public class CustomAudioTrackRenderer extends MediaCodecAudioTrackRenderer {

    private int mAudioSessionId = -1;

    public CustomAudioTrackRenderer(SampleSource source) {
        super(source);
    }

    @Override
    protected void onAudioSessionId(int audioSessionId) {
        mAudioSessionId = audioSessionId;
        super.onAudioSessionId(audioSessionId);
    }

    public int getAudioSessionId() {
        return mAudioSessionId;
    }
}
