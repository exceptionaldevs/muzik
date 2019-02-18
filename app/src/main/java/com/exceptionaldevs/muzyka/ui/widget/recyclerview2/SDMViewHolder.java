package com.exceptionaldevs.muzyka.ui.widget.recyclerview2;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by darken on 16.06.2015.
 */
public class SDMViewHolder extends RecyclerView.ViewHolder {
    private ClickListener mClickListener;
    private LongClickListener mLongClickListener;
    private View.OnTouchListener mOnTouchListener;

    public SDMViewHolder(final View itemView) {
        super(itemView);
        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mOnTouchListener == null)
                    return false;
                return mOnTouchListener.onTouch(v, event);
            }
        });
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener == null)
                    return;
                if (getAdapterPosition() == RecyclerView.NO_POSITION)
                    return;
                mClickListener.onClick(v, getAdapterPosition(), getItemId());
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongClickListener == null)
                    return false;
                if (getAdapterPosition() == RecyclerView.NO_POSITION)
                    return true;
                return mLongClickListener.onLongClick(itemView, getAdapterPosition(), getItemId());
            }
        });
    }

    public interface ClickListener {
        boolean onClick(View view, int position, long itemId);
    }

    public interface LongClickListener {
        boolean onLongClick(View view, int position, long itemId);
    }

    public View.OnTouchListener getOnTouchListener() {
        return mOnTouchListener;
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    public ClickListener getClickListener() {
        return mClickListener;
    }

    public void setClickListener(ClickListener listener) {
        mClickListener = listener;
    }

    public LongClickListener getLongClickListener() {
        return mLongClickListener;
    }

    public void setLongClickListener(LongClickListener listener) {
        mLongClickListener = listener;
    }

    public void post(Runnable runnable) {
        getRootView().post(runnable);
    }

    public Context getContext() {
        return this.itemView.getContext();
    }

    public View getRootView() {
        return itemView;
    }

    public String getString(@StringRes int string) {
        return getResources().getString(string);
    }

    public Resources getResources() {
        return getContext().getResources();
    }

    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(getContext());
    }
}
