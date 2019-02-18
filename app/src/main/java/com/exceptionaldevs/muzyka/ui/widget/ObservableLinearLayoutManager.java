package com.exceptionaldevs.muzyka.ui.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by sebnap on 15.01.16.
 */
public class ObservableLinearLayoutManager extends LinearLayoutManager {
    OnOverscroll listener;

    public ObservableLinearLayoutManager(Context context) {
        super(context);
    }

    public ObservableLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public ObservableLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrollRange = super.scrollVerticallyBy(dy, recycler, state);
        int overscroll = dy - scrollRange;
        if (overscroll > 0) {
            // bottom overscroll
            if (getListener() != null)
                getListener().onOverscrollBottom();
        } else if (overscroll < 0) {
            // top overscroll
            if (getListener() != null)
                getListener().onOverscollTop();
        } else {
            if (getListener() != null)
                getListener().onScroll();
        }
        return scrollRange;
    }

    public OnOverscroll getListener() {
        return listener;
    }

    public void setListener(OnOverscroll listener) {
        this.listener = listener;
    }

    public interface OnOverscroll {
        void onOverscollTop();

        void onOverscrollBottom();

        void onScroll();
    }
}
