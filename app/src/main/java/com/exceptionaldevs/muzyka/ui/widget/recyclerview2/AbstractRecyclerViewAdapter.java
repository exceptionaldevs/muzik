package com.exceptionaldevs.muzyka.ui.widget.recyclerview2;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRecyclerViewAdapter<T, H extends SDMViewHolder> extends SDMRecyclerViewAdapter<H> {
    private final ArrayList<T> mData = new ArrayList<>();

    public List<T> getData() {
        return mData;
    }

    public void setData(List<T> newData) {
        this.mData.clear();
        if (newData != null) {
            this.mData.addAll(newData);
        }
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    public T getItem(int position) {
        return getData().get(position);
    }
}

