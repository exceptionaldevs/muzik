package com.exceptionaldevs.muzyka.ui.widget.recyclerview2.swipe2;


import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;

public interface Swipeable2 {

    void onResetSwipeView();

    /**
     * @param c                 The canvas which RecyclerView is drawing its children
     * @param recyclerView      The RecyclerView to which ItemTouchHelper is attached to
     * @param viewHolder        The ViewHolder which is being interacted by the User or it was
     *                          interacted and simply animating to its original position
     * @param dX                The amount of horizontal displacement caused by user's action
     * @param isCurrentlyActive True if this view is currently being controlled by the user or
     *                          false it is simply animating back to its original state.
     */
    void onSwipeDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, boolean isCurrentlyActive);

    float getSwipeThreshold();
}
