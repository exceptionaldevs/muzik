package com.exceptionaldevs.muzyka.ui.widget.recyclerview2.swipe2;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.view.ViewConfiguration;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.JXItemTouchHelper;

/**
 * Created by darken on 29/08/15.
 */
public class SwipeListener extends JXItemTouchHelper.Callback {
    private static final String TAG = "SwipeListener";
    private final int mSwipeDirs;
    private final RecyclerView mRecyclerView;
    private final SwipeCallback mCallback;

    public SwipeListener(int swipeDirs, RecyclerView recyclerView, SwipeCallback callback) {
        mSwipeDirs = swipeDirs;
        mRecyclerView = recyclerView;
        mCallback = callback;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (mCallback.canSwipe(viewHolder.getAdapterPosition())) {
            return makeMovementFlags(0, mSwipeDirs);
        } else {
            return 0;
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }
    
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mCallback.onSwiped(mRecyclerView, viewHolder.itemView, viewHolder.getAdapterPosition());
        ((Swipeable2) viewHolder.itemView).onResetSwipeView();
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        int slop = vc.getScaledTouchSlop();
        if (viewHolder.itemView instanceof Swipeable2 && Math.abs(dX) > slop) {
            Swipeable2 swip = (Swipeable2) viewHolder.itemView;
            swip.onSwipeDraw(c, recyclerView, viewHolder, dX, isCurrentlyActive);
        }
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        ((Swipeable2) viewHolder.itemView).onResetSwipeView();
    }

    @Override
    public long getAnimationDuration(RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
        return 0;
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder.itemView instanceof Swipeable2) {
            Swipeable2 swip = (Swipeable2) viewHolder.itemView;
            return swip.getSwipeThreshold();
        }
        return super.getSwipeThreshold(viewHolder);
    }

}
