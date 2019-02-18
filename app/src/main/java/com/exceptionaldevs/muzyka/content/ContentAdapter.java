package com.exceptionaldevs.muzyka.content;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filterable;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMRecyclerViewAdapter;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ContentAdapter<ITEM, VH extends SDMViewHolder>
        extends SDMRecyclerViewAdapter<SDMViewHolder>
        implements Filterable {
    protected final Context mContext;
    private final List<ITEM> mItems = new ArrayList<>();       // possible filtered
    private final List<ITEM> mSourceItems = new ArrayList<>();       // not filtered

    public ContentAdapter(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<ITEM> getData() {
        return mItems;
    }

    public void setData(List<ITEM> data) {
        mSourceItems.clear();
        mItems.clear();
        if (data != null) {
            mSourceItems.addAll(data);
            mItems.addAll(data);
        }
    }

    public List<ITEM> getSourceData() {
        return mSourceItems;
    }

    @Nullable
    public ITEM getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public SDMViewHolder onCreateSDMViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        return onCreateBasicItemViewHolder(inflater, parent, viewType);
    }

    public abstract VH onCreateBasicItemViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType);

    @Override
    public void onBindSDMViewHolder(SDMViewHolder holder, int position) {
        onBindBasicItemView((VH) holder, position);
    }

    public abstract void onBindBasicItemView(VH holder, int position);

    public ITEM removeItem(int position) {
        final ITEM model = mItems.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, ITEM model) {
        mItems.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final ITEM model = mItems.remove(fromPosition);
        mItems.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<ITEM> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<ITEM> newModels) {
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final ITEM model = mItems.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ITEM> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final ITEM model = newModels.get(i);
            if (!mItems.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ITEM> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final ITEM model = newModels.get(toPosition);
            final int fromPosition = mItems.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public void swapItems(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++)
                Collections.swap(getData(), i, i + 1);
        } else {
            for (int i = from; i > to; i--)
                Collections.swap(getData(), i, i - 1);
        }
    }


}

