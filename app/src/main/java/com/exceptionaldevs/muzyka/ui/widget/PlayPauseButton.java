/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exceptionaldevs.muzyka.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

/**
 * A {@link Checkable} {@link ImageButton} which has a minimum offset i.e. translation Y.
 */
public class PlayPauseButton extends FABToggle {
    public PlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public enum PlayState { // Pause means button checked, see xml
        PLAY, PAUSE
    }

    public PlayState getPlayState() {
        return isChecked() ? PlayState.PAUSE : PlayState.PLAY;
    }

    public void setPlayState(PlayState playState) {
        if (playState == PlayState.PAUSE) {
            setChecked(true);
        } else {
            setChecked(false);
        }
    }
}
