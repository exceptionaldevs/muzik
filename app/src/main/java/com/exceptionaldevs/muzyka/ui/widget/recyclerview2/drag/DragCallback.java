package com.exceptionaldevs.muzyka.ui.widget.recyclerview2.drag;

import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by darken on 30.08.2015.
 */
public interface DragCallback {
    boolean canDrag(int position);

    /**
     * @param recyclerView
     * @param viewHolder
     * @param target
     * @return true if this was OK
     */
    boolean onDragged(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);


    @IntDef({DRAG_STATE_IDLE, DRAG_STATE_DRAGGING})
    @Retention(RetentionPolicy.SOURCE)
    @interface DRAGSTATE {
    }

    int DRAG_STATE_IDLE = 0;
    int DRAG_STATE_DRAGGING = 1;

    void onDragStateChanged(@DRAGSTATE int dragState);
}
