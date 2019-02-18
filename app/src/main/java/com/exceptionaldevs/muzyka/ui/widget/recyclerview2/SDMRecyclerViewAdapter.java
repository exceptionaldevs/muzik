package com.exceptionaldevs.muzyka.ui.widget.recyclerview2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public abstract class SDMRecyclerViewAdapter<H extends SDMViewHolder> extends RecyclerView.Adapter<H>
        implements
        SDMViewHolder.LongClickListener,
        SDMViewHolder.ClickListener, View.OnTouchListener {
    private MultiItemSelector mMultiItemSelector;
    private View.OnTouchListener mOnTouchListener;
    private SDMViewHolder.LongClickListener mLongClickListener;
    private SDMViewHolder.ClickListener mClickListener;

    public void setMultiItemSelector(MultiItemSelector multiItemSelector) {
        mMultiItemSelector = multiItemSelector;
    }

    public MultiItemSelector getMultiItemSelector() {
        return mMultiItemSelector;
    }

    public boolean isItemSelectable(int position) {
        return true;
    }

    @Override
    public H onCreateViewHolder(ViewGroup parent, int viewType) {
        H viewHolder = onCreateSDMViewHolder(LayoutInflater.from(parent.getContext()), parent, viewType);
        if (viewHolder == null) {
            throw new RuntimeException("onCreateSDMViewHolder returns null!");
        }
        viewHolder.setOnTouchListener(this);
        viewHolder.setClickListener(this);
        viewHolder.setLongClickListener(this);
        return viewHolder;
    }

    public abstract H onCreateSDMViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(H holder, int position) {
        onBindSDMViewHolder(holder, position);
        if (mMultiItemSelector != null)
            holder.getRootView().setActivated(mMultiItemSelector.isItemChecked(position));
    }

    public abstract void onBindSDMViewHolder(H _holder, int position);

    public void setOnTouchListener(View.OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    public void setItemClickListener(SDMViewHolder.ClickListener listener) {
        mClickListener = listener;
    }

    public void setItemLongClickListener(SDMViewHolder.LongClickListener listener) {
        mLongClickListener = listener;
    }

    @Override
    public boolean onClick(View view, int position, long itemId) {
        if (mClickListener == null)
            return false;
        return mClickListener.onClick(view, position, itemId);
    }

    @Override
    public boolean onLongClick(View view, int position, long itemId) {
        if (mLongClickListener == null)
            return false;
        return mLongClickListener.onLongClick(view, position, itemId);
    }

    @Override
    public void onViewRecycled(H holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mOnTouchListener == null)
            return false;
        return mOnTouchListener.onTouch(v, event);
    }
}

