package com.exceptionaldevs.muzyka.ui.widget.recyclerview2;

import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

/**
 * Created by darken on 29/12/14.
 * https://github.com/lucasr/twoway-view
 */
public class MultiItemSelector {
    public static final int INVALID_POSITION = -1;
    private MultiItemSelectorListener mListener;

    public enum ChoiceMode {
        NONE,
        SINGLE,
        MULTIPLE
    }

    private final SDMRecyclerView mRecyclerView;
    private ChoiceMode mChoiceMode = ChoiceMode.NONE;
    private SparseBooleanArray mCheckedStates;
    private LongSparseArray<Integer> mCheckedIdStates;
    private int mCheckedCount;
    private static final String STATE_KEY_CHOICE_MODE = "choiceMode";
    private static final String STATE_KEY_CHECKED_STATES = "SparseBooleanArray";
    private static final String STATE_KEY_CHECKED_ID_STATES = "checkedIdStates";
    private static final String STATE_KEY_CHECKED_COUNT = "checkedCount";
    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;

    public MultiItemSelector(SDMRecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    public boolean isEverythingSelected() {
        if (mRecyclerView.getAdapter() == null)
            return true;
        int checkableItemCount = 0;
        for (int i = 0; i < mRecyclerView.getAdapter().getItemCount(); i++) {
            if (mRecyclerView.getAdapter().isItemSelectable(i))
                checkableItemCount++;
        }
        return checkableItemCount == mCheckedCount;
    }

    public void setAllItems(boolean checked) {
        for (int i = 0; i < mRecyclerView.getAdapter().getItemCount(); i++) {
            if (mRecyclerView.getAdapter().isItemSelectable(i)) {
                setItemChecked(i, checked);
            }
        }
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * Returns the number of items currently selected. This will only be valid
     * if the choice mode is not {@link ChoiceMode#NONE} (default).
     * <p/>
     * <p>To determine the specific items that are currently selected, use one of
     * the <code>getChecked*</code> methods.
     *
     * @return The number of items currently selected
     * @see #getCheckedItemPosition()
     * @see #getCheckedItemPositions()
     * @see #getCheckedItemIds()
     */
    public int getCheckedItemCount() {
        return mCheckedCount;
    }

    /**
     * Returns the checked state of the specified position. The result is only
     * valid if the choice mode has been set to {@link ChoiceMode#SINGLE}
     * or {@link ChoiceMode#MULTIPLE}.
     *
     * @param position The item whose checked state to return
     * @return The item's checked state or <code>false</code> if choice mode
     * is invalid
     * @see #setChoiceMode(ChoiceMode)
     */
    public boolean isItemChecked(int position) {
        if (mChoiceMode != ChoiceMode.NONE && mCheckedStates != null) {
            return mCheckedStates.get(position);
        }
        return false;
    }

    /**
     * Returns the currently checked item. The result is only valid if the choice
     * mode has been set to {@link ChoiceMode#SINGLE}.
     *
     * @return The position of the currently checked item or
     * {@link #INVALID_POSITION} if nothing is selected
     * @see #setChoiceMode(ChoiceMode)
     */
    public int getCheckedItemPosition() {
        if (mChoiceMode == ChoiceMode.SINGLE && mCheckedStates != null && mCheckedStates.size() == 1) {
            return mCheckedStates.keyAt(0);
        }
        return INVALID_POSITION;
    }

    /**
     * Returns the set of checked items in the list. The result is only valid if
     * the choice mode has not been set to {@link ChoiceMode#NONE}.
     *
     * @return A SparseBooleanArray which will return true for each call to
     * get(int position) where position is a position in the list,
     * or <code>null</code> if the choice mode is set to
     * {@link ChoiceMode#NONE}.
     */
    public SparseBooleanArray getCheckedItemPositions() {
        if (mChoiceMode != ChoiceMode.NONE) {
            return mCheckedStates;
        }
        return null;
    }

    /**
     * Returns the set of checked items ids. The result is only valid if the
     * choice mode has not been set to {@link ChoiceMode#NONE} and the adapter
     * has stable IDs.
     *
     * @return A new array which contains the id of each checked item in the
     * list.
     * @see android.support.v7.widget.RecyclerView.Adapter#hasStableIds()
     */
    public long[] getCheckedItemIds() {
        if (mChoiceMode == ChoiceMode.NONE
                || mCheckedIdStates == null || mRecyclerView.getAdapter() == null) {
            return new long[0];
        }
        final int count = mCheckedIdStates.size();
        final long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = mCheckedIdStates.keyAt(i);
        }
        return ids;
    }

    public boolean toggleItem(int position) {
        return setItemChecked(position, !isItemChecked(position));
    }

    public void toggleItemWithNotification(int position) {
        if (toggleItem(position))
            notifyAdapter(position);
    }

    private void notifyAdapter(int position) {
        if (mChoiceMode == ChoiceMode.SINGLE) {
            mRecyclerView.getAdapter().notifyDataSetChanged();
        } else {
            mRecyclerView.getAdapter().notifyItemChanged(position);
        }
    }

    public void setItemCheckedWithNotify(int position, boolean isChecked) {
        if (setItemChecked(position, isChecked))
            notifyAdapter(position);
    }

    /**
     * Sets the checked state of the specified position. The is only valid if
     * the choice mode has been set to {@link ChoiceMode#SINGLE} or
     * {@link ChoiceMode#MULTIPLE}.
     *
     * @param position The item whose checked state is to be checked
     * @param checked  The new checked state for the item
     * @return true if the item's checked state was changed
     */
    public boolean setItemChecked(int position, boolean checked) {
        if (!mRecyclerView.getAdapter().isItemSelectable(position)) {
            return false;
        }
        if (mChoiceMode == ChoiceMode.NONE) {
            return false;
        }
        boolean stateChanged = false;
        final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (mChoiceMode == ChoiceMode.MULTIPLE) {
            boolean oldValue = mCheckedStates.get(position);
            mCheckedStates.put(position, checked);
            if (mCheckedIdStates != null && adapter.hasStableIds()) {
                if (checked) {
                    mCheckedIdStates.put(adapter.getItemId(position), position);
                } else {
                    mCheckedIdStates.delete(adapter.getItemId(position));
                }
            }
            if (oldValue != checked) {
                stateChanged = true;
                if (checked) {
                    mCheckedCount++;
                } else {
                    mCheckedCount--;
                }
            }
        } else {
            boolean updateIds = mCheckedIdStates != null && adapter.hasStableIds();
            // Clear all values if we're checking something, or unchecking the currently selected item
            if (checked || isItemChecked(position)) {
                mCheckedStates.clear();
                if (updateIds) {
                    mCheckedIdStates.clear();
                }
            }
            // This may end up selecting the checked we just cleared but this way
            // we ensure length of mCheckStates is 1, a fact getCheckedItemPosition relies on
            if (checked) {
                mCheckedStates.put(position, true);
                if (updateIds) {
                    mCheckedIdStates.put(adapter.getItemId(position), position);
                }
                mCheckedCount = 1;
            } else if (mCheckedStates.size() == 0 || !mCheckedStates.valueAt(0)) {
                mCheckedCount = 0;
            }
        }
        if (mListener != null && stateChanged)
            mListener.onSelectionChanged(this);
        return stateChanged;
    }

    /**
     * Clears any choices previously set.
     */
    public void clearChoices() {
        if (mCheckedStates != null)
            mCheckedStates.clear();
        if (mCheckedIdStates != null)
            mCheckedIdStates.clear();

        mCheckedCount = 0;
        if (mListener != null)
            mListener.onSelectionChanged(this);
    }

    /**
     * Returns the current choice mode.
     *
     * @see #setChoiceMode(ChoiceMode)
     */
    public ChoiceMode getChoiceMode() {
        return mChoiceMode;
    }

    /**
     * Defines the choice behavior for the List. By default, Lists do not have any choice behavior
     * ({@link ChoiceMode#NONE}). By setting the choiceMode to {@link ChoiceMode#SINGLE}, the
     * List allows up to one item to be in a chosen state. By setting the choiceMode to
     * {@link ChoiceMode#MULTIPLE}, the list allows any number of items to be chosen.
     *
     * @param choiceMode One of {@link ChoiceMode#NONE}, {@link ChoiceMode#SINGLE}, or
     *                   {@link ChoiceMode#MULTIPLE}
     */
    public void setChoiceMode(ChoiceMode choiceMode) {
        if (mChoiceMode == choiceMode) {
            return;
        }
        mChoiceMode = choiceMode;
        if (mChoiceMode != ChoiceMode.NONE) {
            if (mCheckedStates == null) {
                mCheckedStates = new SparseBooleanArray();
            }
            final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            if (mCheckedIdStates == null && adapter != null && adapter.hasStableIds()) {
                mCheckedIdStates = new LongSparseArray<>();
            }
        } else {
            clearChoices();
            mCheckedStates = null;
            mCheckedIdStates = null;
        }
    }

    public void onAdapterDataChanged() {
        final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (mChoiceMode == ChoiceMode.NONE || adapter == null || !adapter.hasStableIds()) {
            return;
        }
        final int itemCount = adapter.getItemCount();
        // Clear out the positional check states, we'll rebuild it below from IDs.
        mCheckedStates.clear();
        for (int checkedIndex = 0; checkedIndex < mCheckedIdStates.size(); checkedIndex++) {
            final long currentId = mCheckedIdStates.keyAt(checkedIndex);
            final int currentPosition = mCheckedIdStates.valueAt(checkedIndex);
            final long newPositionId = adapter.getItemId(currentPosition);
            if (currentId != newPositionId) {
                // Look around to see if the ID is nearby. If not, uncheck it.
                final int start = Math.max(0, currentPosition - CHECK_POSITION_SEARCH_DISTANCE);
                final int end = Math.min(currentPosition + CHECK_POSITION_SEARCH_DISTANCE, itemCount);
                boolean found = false;
                for (int searchPos = start; searchPos < end; searchPos++) {
                    final long searchId = adapter.getItemId(searchPos);
                    if (currentId == searchId) {
                        found = true;
                        mCheckedStates.put(searchPos, true);
                        mCheckedIdStates.setValueAt(checkedIndex, searchPos);
                        break;
                    }
                }
                if (!found) {
                    mCheckedIdStates.delete(currentId);
                    mCheckedCount--;
                    checkedIndex--;
                }
            } else {
                mCheckedStates.put(currentPosition, true);
            }
        }
    }

    public Bundle onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putInt(STATE_KEY_CHOICE_MODE, mChoiceMode.ordinal());

        int[] stateKeys = new int[0];
        boolean[] stateValues = new boolean[0];
        if (mCheckedStates != null) {
            stateKeys = new int[mCheckedStates.size()];
            for (int i = 0; i < mCheckedStates.size(); i++)
                stateKeys[i] = mCheckedStates.keyAt(i);
            stateValues = new boolean[mCheckedStates.size()];
            for (int i = 0; i < mCheckedStates.size(); i++)
                stateValues[i] = mCheckedStates.valueAt(i);
        }
        state.putIntArray(STATE_KEY_CHECKED_STATES + "keys", stateKeys);
        state.putBooleanArray(STATE_KEY_CHECKED_STATES + "values", stateValues);

        long[] idKeys = new long[0];
        int[] idValues = new int[0];
        if (mCheckedIdStates != null) {
            idKeys = new long[mCheckedIdStates.size()];
            for (int i = 0; i < mCheckedIdStates.size(); i++)
                idKeys[i] = mCheckedIdStates.keyAt(i);
            idValues = new int[mCheckedIdStates.size()];
            for (int i = 0; i < mCheckedIdStates.size(); i++)
                idValues[i] = mCheckedIdStates.valueAt(i);
        }
        state.putLongArray(STATE_KEY_CHECKED_ID_STATES + "keys", idKeys);
        state.putIntArray(STATE_KEY_CHECKED_ID_STATES + "values", idValues);

        state.putInt(STATE_KEY_CHECKED_COUNT, mCheckedCount);
        return state;
    }

    public void onRestoreInstanceState(Bundle state) {
        setChoiceMode(ChoiceMode.values()[state.getInt(STATE_KEY_CHOICE_MODE)]);

        int[] stateKeys = state.getIntArray(STATE_KEY_CHECKED_STATES + "keys");
        if (stateKeys != null) {
            boolean[] stateValues = state.getBooleanArray(STATE_KEY_CHECKED_STATES + "values");
            for (int i = 0; i < stateKeys.length; i++) {
                mCheckedStates.put(stateKeys[i], stateValues[i]);
            }
        }

        long[] idKeys = state.getLongArray(STATE_KEY_CHECKED_ID_STATES + "keys");
        if (idKeys != null) {
            int[] idValues = state.getIntArray(STATE_KEY_CHECKED_ID_STATES + "values");
            for (int i = 0; i < idKeys.length; i++) {
                mCheckedIdStates.put(idKeys[i], idValues[i]);
            }
        }

        mCheckedCount = state.getInt(STATE_KEY_CHECKED_COUNT);
    }

    public void setListener(MultiItemSelectorListener listener) {
        mListener = listener;
    }

    public interface MultiItemSelectorListener {
        void onSelectionChanged(MultiItemSelector multiItemSelector);
    }

}
