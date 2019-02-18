package com.exceptionaldevs.muzyka.ui.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by sebnap on 15.01.16.
 */
public class ObservableGridLayoutManager extends GridLayoutManager {
    OnOverscroll listener;

    public ObservableGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ObservableGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public ObservableGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
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
