package com.exceptionaldevs.muzyka.ui.widget.recyclerview2;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.drag.DragCallback;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.drag.DragListener;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.swipe2.SwipeCallback;
import com.exceptionaldevs.muzyka.ui.widget.recyclerview2.swipe2.SwipeListener;
import com.exceptionaldevs.muzyka.utils.Logy;

public class SDMRecyclerView extends RecyclerView implements
        ActionMode.Callback,
        SDMViewHolder.ClickListener,
        SDMViewHolder.LongClickListener {
    private static final String TAG = "SDMRecyclerView";
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private final MultiItemSelector mMultiItemSelector;
    private ActionMode.Callback mActionModeCallback;
    private ActionMode mActionMode;
    private JXItemTouchHelper mSwipeTouchHelper;
    private JXItemTouchHelper mDragTouchHelper;

    public SDMRecyclerView(Context context) {
        this(context, null, 0);
    }

    public SDMRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SDMRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mMultiItemSelector = new MultiItemSelector(this);
        setItemViewCacheSize(4);
    }

    @Override
    public SDMRecyclerViewAdapter getAdapter() {
        return (SDMRecyclerViewAdapter) super.getAdapter();
    }

    @Override
    public void setAdapter(Adapter _adapter) {
        if (_adapter instanceof SDMRecyclerViewAdapter) {
            SDMRecyclerViewAdapter adapter = (SDMRecyclerViewAdapter) _adapter;
            adapter.setMultiItemSelector(getMultiItemSelector());
            adapter.setItemClickListener(this);
            adapter.setItemLongClickListener(this);
        } else {
            throw new IllegalArgumentException("Adapter needs to implement SDMRecyclerViewAdapter");
        }
        super.setAdapter(_adapter);
    }

    public void startDrag(SDMViewHolder viewHolder) {
        if (mDragTouchHelper != null)
            mDragTouchHelper.startDrag(viewHolder);
    }

    public void setOnItemDragListener(final DragCallback callback) {
        mDragTouchHelper = new JXItemTouchHelper(new DragListener(this, callback));
        mDragTouchHelper.attachToRecyclerView(this);
    }

    public void setOnItemSwipeListener(final SwipeCallback callback, int swipeDirection) {
        mSwipeTouchHelper = new JXItemTouchHelper(new SwipeListener(swipeDirection, this, callback));
        mSwipeTouchHelper.attachToRecyclerView(this);
    }

    public boolean isInActionMode() {
        return mActionMode != null;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public void setChoiceMode(MultiItemSelector.ChoiceMode choiceMode) {
        mMultiItemSelector.setChoiceMode(choiceMode);
    }

    public void setCheckedItemPositions(SparseBooleanArray checkedItemPositions) {
        if (checkedItemPositions != null) {
            for (int i = 0; i < checkedItemPositions.size(); i++) {
                if (checkedItemPositions.valueAt(i)) {
                    mMultiItemSelector.setItemChecked(checkedItemPositions.keyAt(i), true);
                }
            }
            getAdapter().notifyDataSetChanged();
        } else {
            mMultiItemSelector.clearChoices();
            getAdapter().notifyDataSetChanged();
        }
    }

    public SparseBooleanArray getCheckedItemPositions() {
        return getMultiItemSelector().getCheckedItemPositions().clone();
    }


    public ActionMode startActionModeForToolbar(Toolbar toolbar, ActionMode.Callback callback) {
        if (mActionMode != null) {
            mActionMode.finish();
            getAdapter().notifyDataSetChanged();
        }
        mActionModeCallback = callback;
        mActionMode = toolbar.startActionMode(this);
        return mActionMode;
    }

    public ActionMode startActionMode(AppCompatActivity activity, ActionMode.Callback callback) {
        if (mActionMode != null) {
            mActionMode.finish();
            getAdapter().notifyDataSetChanged();
        }
        mActionModeCallback = callback;
        mActionMode = activity.startActionMode(this);
        return mActionMode;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        Logy.d(TAG, "onCreateActionMode");
        return mActionModeCallback.onCreateActionMode(mode, menu);
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Logy.d(TAG, "onPrepareActionMode");
        return mActionModeCallback.onPrepareActionMode(mode, menu);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Logy.d(TAG, "onActionItemClicked");
        return mActionModeCallback.onActionItemClicked(mode, item);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Logy.d(TAG, "onDestroyActionMode");
        mActionModeCallback.onDestroyActionMode(mode);
        mMultiItemSelector.clearChoices();
        getAdapter().notifyDataSetChanged();
        mActionMode = null;
    }

    public void scrollToPosition(int position, int pixel_from_top) {
        if (getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
            layoutManager.scrollToPositionWithOffset(position, pixel_from_top);
        } else {
            scrollToPosition(position);
        }
    }

    public int getFirstVisiblePosition() {
        if (getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
            return layoutManager.findFirstVisibleItemPosition();
        }
        return 0;
    }

    public void finishActionMode() {
        if (isInActionMode())
            getActionMode().finish();
    }

    @Override
    public boolean onClick(View view, int position, long itemId) {
        boolean consumed = true;
        if (mItemClickListener != null) {
            consumed = mItemClickListener.onRecyclerItemClick(this, view, position, itemId);
        }
        Logy.d(TAG, "onRecyclerItemClick(pos:" + position + ")->" + consumed);
        if (!consumed && isInActionMode()) {
            mMultiItemSelector.toggleItem(position);
            if (mMultiItemSelector.getCheckedItemCount() == 0) {
                mActionMode.finish();
            } else {
                mActionMode.invalidate();
                if (getMultiItemSelector().getChoiceMode() == MultiItemSelector.ChoiceMode.SINGLE) {
                    getAdapter().notifyDataSetChanged();
                } else {
                    getAdapter().notifyItemChanged(position);
                }
            }
        }
        return consumed;
    }

    @Override
    public boolean onLongClick(View view, int position, long itemId) {
        boolean consumed = false;
        if (mItemLongClickListener != null)
            consumed = mItemLongClickListener.onRecyclerItemLongClick(this, view, position, itemId);

        Logy.d(TAG, "onRecyclerItemLongClick(pos:" + position + ")->" + consumed);
        if (!consumed && getMultiItemSelector().getChoiceMode() != MultiItemSelector.ChoiceMode.NONE) {
            if (mActionMode == null) {
                mMultiItemSelector.setItemChecked(position, true);
                getAdapter().notifyDataSetChanged();
                if (mActionMode != null)
                    mActionMode.invalidate();
            } else {
                mMultiItemSelector.toggleItem(position);
                if (mMultiItemSelector.getCheckedItemCount() == 0) {
                    mActionMode.finish();
                } else {
                    mActionMode.invalidate();
                    if (getMultiItemSelector().getChoiceMode() == MultiItemSelector.ChoiceMode.SINGLE) {
                        getAdapter().notifyDataSetChanged();
                    } else {
                        getAdapter().notifyItemChanged(position);
                    }
                }
            }
            consumed = true;
        }
        return consumed;
    }

    public MultiItemSelector getMultiItemSelector() {
        return mMultiItemSelector;
    }

    public int getCheckedItemCount() {
        return mMultiItemSelector.getCheckedItemCount();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        ARVSavedState toRestoreState = (ARVSavedState) state;
        super.onRestoreInstanceState(toRestoreState.getSuperState());
        mMultiItemSelector.onRestoreInstanceState(toRestoreState.mMultiItemSelectorState);
        if (mActionMode != null)
            mActionMode.invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        ARVSavedState state = new ARVSavedState(super.onSaveInstanceState());
        state.mMultiItemSelectorState = mMultiItemSelector.onSaveInstanceState();
        return state;
    }

    static class ARVSavedState extends AbsSavedState {
        Bundle mMultiItemSelectorState;

        ARVSavedState(Parcelable superState) {
            super(superState);
        }

        ARVSavedState(Parcel in) {
            //TODO: "Aktionen nicht speichern" <- this fails
            super(in);
            mMultiItemSelectorState = in.readBundle(MultiItemSelector.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(mMultiItemSelectorState);
        }

        public static final Creator<ARVSavedState> CREATOR = new Creator<ARVSavedState>() {
            @Override
            public ARVSavedState createFromParcel(Parcel in) {
                return new ARVSavedState(in);
            }

            @Override
            public ARVSavedState[] newArray(int size) {
                return new ARVSavedState[size];
            }
        };
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mItemLongClickListener = listener;
    }

    public interface OnItemClickListener {
        /**
         * @return true if the callback consumed the click, false otherwise.
         * If you return TRUE, the recyclerview won't select the item if it is in actionmode
         * Return TRUE means the recyclerview does not continue its normal procedure on this call.
         */
        boolean onRecyclerItemClick(RecyclerView parent, View view, int position, long id);
    }

    public interface OnItemLongClickListener {
        /**
         * @return true if the callback consumed the long click, false otherwise.
         * If you return TRUE, the recyclerview won't select the item if it is in actionmode
         * Return TRUE means the recyclerview does not continue its normal procedure on this call.
         */
        boolean onRecyclerItemLongClick(RecyclerView parent, View view, int position, long id);
    }
}
