package com.exceptionaldevs.muzyka.ui.widget.recyclerview2.drag;

import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.JXItemTouchHelper;

/**
 * Created by darken on 29/08/15.
 */
public class DragListener extends JXItemTouchHelper.Callback {
    private static final String TAG = "DragListener";
    private final int mDragDirection;
    private final RecyclerView mRecyclerView;
    private final DragCallback mCallback;

    public DragListener(RecyclerView recyclerView, DragCallback callback) {
        mDragDirection = JXItemTouchHelper.UP | JXItemTouchHelper.DOWN;
        mRecyclerView = recyclerView;
        mCallback = callback;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int adapterPosition = viewHolder.getAdapterPosition();
        if (adapterPosition != RecyclerView.NO_POSITION && mCallback.canDrag(adapterPosition)) {
            return makeMovementFlags(mDragDirection, 0);
        } else {
            return 0;
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return mCallback.onDragged(recyclerView, viewHolder, target);
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        View view = viewHolder.itemView;
        view.setTranslationX(0f);
        view.setTranslationY(0f);

        final Object tag = view.getTag(android.support.v7.recyclerview.R.id.item_touch_helper_previous_elevation);
        if (tag != null && tag instanceof Float) {
            ViewCompat.setElevation(view, (Float) tag);
        }
        view.setTag(android.support.v7.recyclerview.R.id.item_touch_helper_previous_elevation, null);
        super.clearView(recyclerView, viewHolder);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View view = viewHolder.itemView;
        if (isCurrentlyActive) {
            Object originalElevation = view.getTag(android.support.v7.recyclerview.R.id.item_touch_helper_previous_elevation);
            if (originalElevation == null) {
                originalElevation = ViewCompat.getElevation(view);
                float newElevation = 1f + findMaxElevation(recyclerView, view);
                ViewCompat.setElevation(view, newElevation);
                view.setTag(android.support.v7.recyclerview.R.id.item_touch_helper_previous_elevation, originalElevation);
            }
        }

        viewHolder.itemView.setTranslationX(dX);
        viewHolder.itemView.setTranslationY(dY);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private float findMaxElevation(RecyclerView recyclerView, View itemView) {
        final int childCount = recyclerView.getChildCount();
        float max = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = recyclerView.getChildAt(i);
            if (child == itemView) {
                continue;
            }
            final float elevation = ViewCompat.getElevation(child);
            if (elevation > max) {
                max = elevation;
            }
        }
        return max;
    }

    public void onStartDragging() {

    }

    public void onStopDragging() {

    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        mCallback.onDragStateChanged(viewHolder != null ? DragCallback.DRAG_STATE_DRAGGING : DragCallback.DRAG_STATE_IDLE);
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }

}
