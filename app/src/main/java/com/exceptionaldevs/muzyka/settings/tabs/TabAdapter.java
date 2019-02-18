package com.exceptionaldevs.muzyka.settings.tabs;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.exceptionaldevs.muzyka.R;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMRecyclerView;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMRecyclerViewAdapter;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.SDMViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by darken on 22.01.2016.
 */
class TabAdapter extends SDMRecyclerViewAdapter {
    private final List<TabIdentifier> active = new ArrayList<>();
    private final List<TabIdentifier> hidden = new ArrayList<>();

    public List<TabIdentifier> getActiveTabs() {
        return active;
    }

    public void setActiveTabs(List<TabIdentifier> tabs) {
        active.clear();
        active.addAll(tabs);
        hidden.clear();
        hidden.addAll(Arrays.asList(TabIdentifier.values()));
        hidden.removeAll(active);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int count = 0;
        count += active.size();
        if (!active.isEmpty())
            count++; //header
        count += hidden.size();
        if (!hidden.isEmpty())
            count++; //header
        return count;
    }

    public int getSecondHeaderPosition() {
        return active.isEmpty() ? 0 : active.size() + 1;
    }


    @Nullable
    public TabIdentifier getItem(int position) {
        int secondHeaderPos = getSecondHeaderPosition();
        if (position == 0 || position == secondHeaderPos)
            return null; // header

        TabIdentifier tabIdentifier = null;
        if (active.size() > 0 && (position < secondHeaderPos || secondHeaderPos == 0)) {
            int adjustedPos = position - 1; // header
            tabIdentifier = active.get(adjustedPos);
        } else if (hidden.size() > 0 && position > secondHeaderPos) {
            int adjustedPos = position - secondHeaderPos - 1;
            tabIdentifier = hidden.get(adjustedPos);
        }
        return tabIdentifier;
    }

    public boolean isActive(int position) {
        return active.contains(getItem(position));
    }

    public void swapItems(int adapterFrom, int adapterTo) {
        int corrFrom = adapterFrom - 1;
        int corrTo = adapterTo - 1;
        Collections.swap(active, corrFrom, corrTo);
        notifyItemMoved(adapterFrom, adapterTo);
    }

    public void toggleTabIdentifier(int position) {
        TabIdentifier item = getItem(position);
        if (active.contains(item)) {
            active.remove(item);
            hidden.add(0, item);
            if (active.isEmpty()) {
                notifyItemRemoved(2);
            } else {
                int newPos = 1 + active.size() + 1;
                notifyItemMoved(position, newPos);
            }
        } else if (hidden.contains(item)) {
            hidden.remove(item);
            active.add(item);
            notifyItemMoved(position, active.size());
            if (active.size() == 1) {
                notifyItemInserted(2);
            } else if (hidden.isEmpty()) {
                notifyItemRemoved(getItemCount()); // previous hidden header becomes last active item
            } else {
                int newPos = active.size();
                notifyItemMoved(position, newPos);
            }

        }
        if (active.isEmpty())
            notifyItemRemoved(0);
        if (hidden.isEmpty())
            notifyItemRemoved(getItemCount() - 1);
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        TabIdentifier item = getItem(position);
        if (item == null)
            return 0;
        return 1;
    }

    @Override
    public SDMViewHolder onCreateSDMViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View viewHolder = inflater.inflate(R.layout.adapter_line_tabidentifierheader, parent, false);
            return new TabHeaderHolder(viewHolder);
        } else {
            View viewHolder = inflater.inflate(R.layout.adapter_line_tabidentifier, parent, false);
            return new TabViewHolder(viewHolder);
        }
    }

    @Override
    public void onBindSDMViewHolder(final SDMViewHolder holder, int position) {
        if (holder instanceof TabHeaderHolder) {
            TabHeaderHolder header = (TabHeaderHolder) holder;
            if (position == 0 && active.size() > 0) {
                header.bind(R.string.active, true);
            } else {
                header.bind(R.string.hidden, false);
            }
        } else {
            TabViewHolder tabViewHolder = (TabViewHolder) holder;
            tabViewHolder.bind(getItem(position), isActive(position));
            if (active.size() > 1 && isActive(position)) {
                tabViewHolder.dragAnchor.setVisibility(View.VISIBLE);
                tabViewHolder.dragAnchor.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        ((SDMRecyclerView) holder.itemView.getParent()).startDrag(holder);
                        return false;
                    }
                });
            } else {
                tabViewHolder.dragAnchor.setVisibility(View.GONE);
                tabViewHolder.dragAnchor.setOnTouchListener(null);
            }
        }
    }

    static class TabHeaderHolder extends SDMViewHolder {
        @BindView(R.id.title) TextView title;

        public TabHeaderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setClickListener(null);
            setLongClickListener(null);
        }

        public void bind(@StringRes int titleRes, boolean active) {
            this.title.setText(titleRes);
            if (active) {
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            } else {
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary_dark));
            }
        }
    }

    static class TabViewHolder extends SDMViewHolder {
        @BindView(R.id.title) TextView title;
        @BindView(R.id.summary) TextView summary;
        @BindView(R.id.drag_anchor) View dragAnchor;

        public TabViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final TabIdentifier tabIdentifier, boolean active) {
            title.setText(tabIdentifier.titleRes);
            summary.setText(tabIdentifier.summaryRes);
            if (active) {
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary_dark));
            } else {
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.text_secondary_dark));
            }
        }
    }
}
