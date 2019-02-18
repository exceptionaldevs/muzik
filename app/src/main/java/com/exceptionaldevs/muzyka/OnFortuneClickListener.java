package com.exceptionaldevs.muzyka;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by sebnap on 31.01.16.
 */
public interface OnFortuneClickListener {
    void onClickFortune(View v);

    boolean onTouchFortune(View v, MotionEvent event);

}
