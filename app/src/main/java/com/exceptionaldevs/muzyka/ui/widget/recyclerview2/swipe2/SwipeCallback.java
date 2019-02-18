package com.exceptionaldevs.muzyka.ui.widget.recyclerview2.swipe2;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by darken on 29/08/15.
 */
public interface SwipeCallback {
    /**
     * Called to determine whether the given position can be swiped.
     */
    boolean canSwipe(int position);

    /**
     * Called when a view is swiped
     *
     * @param recyclerView   the parent recyclerView of the swiped view
     * @param swipedView     the swiped view
     * @param swipedPosition the position that was swiped
     * @return return true to remove this view
     */
    boolean onSwiped(RecyclerView recyclerView, View swipedView, int swipedPosition);
}
