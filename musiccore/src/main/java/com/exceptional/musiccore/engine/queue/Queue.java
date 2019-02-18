package com.exceptional.musiccore.engine.queue;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.JXObjectFactory;
import com.exceptional.musiccore.engine.PlayerOptions;
import com.exceptional.musiccore.engine.PlayerOptions.RepeatMode;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptional.musiccore.utils.Logy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Queue {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "QueueHandler";
    private final Context mContext;
    private final QueueInternal mNormalQueueObj = new QueueInternal();
    private final List<JXQueueListener> mListeners = new ArrayList<>();
    // TODO persist options
    private PlayerOptions mPlayerOptions = new PlayerOptions();

    private ArrayList<Integer> mPlayOrder = new ArrayList<>();
    private int mPlayOrderPosition = -1;
    private int mPlaylistPosition = -1;

    public Queue(Context context) {
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    private synchronized void updateShuffle() {
        int queueSize = getInternalQueue().size();
        mPlayOrder = new ArrayList<>(queueSize);
        for (int i = 0; i < queueSize; i++) {
            if (i == mPlaylistPosition && getShuffleMode() != PlayerOptions.ShuffleMode.NONE)
                continue;
            mPlayOrder.add(i);
        }
        if (getShuffleMode() == PlayerOptions.ShuffleMode.RANDOM_TRACK) {
            Collections.shuffle(mPlayOrder);
        }
        if (getShuffleMode() != PlayerOptions.ShuffleMode.NONE && mPlaylistPosition != -1) {
            mPlayOrder.add(mPlaylistPosition, mPlaylistPosition);
        }
        mPlayOrderPosition = mPlaylistPosition;
    }

    private QueueInternal getInternalQueue() {
        return mNormalQueueObj;
    }

    public List<JXObject> getTracks() {
        return getInternalQueue().getContent();
    }

    public int getCurrentPosition() {
        return mPlaylistPosition;
    }

    @Nullable
    public synchronized JXObject getCurrentTrack() {
        if (mPlaylistPosition == -1)
            return null;
        return getInternalQueue().get(mPlaylistPosition);
    }

    public synchronized void setCurrentPlayingPosition(int position) {
        if (position >= getInternalQueue().size() || position < -1)
            throw new IndexOutOfBoundsException("length=" + getInternalQueue().size() + "; index=" + position);
        mPlaylistPosition = position;
        for (int i = 0; i < mPlayOrder.size(); i++) {
            if (mPlayOrder.get(i) == position)
                mPlayOrderPosition = i;
        }
    }

    public synchronized JXObject getAndSet(int position) {
        setCurrentPlayingPosition(position);
        return get(mPlaylistPosition);
    }

    private int resolveIntToExt(int position) {
        if (position == -1)
            return -1;
        return mPlayOrder.get(position);
    }

    @Nullable
    public synchronized JXObject requestTrack(boolean select) {
        int newPosition = -1;
        if (!getInternalQueue().isEmpty()) {
            if (mPlayerOptions.getRepeatMode() == RepeatMode.TRACK) {
                newPosition = mPlayOrderPosition;
            } else if (mPlayerOptions.getRepeatMode() == RepeatMode.PLAYLIST && mPlayOrderPosition == getInternalQueue().size() - 1) {
                newPosition = 0;
            } else if (mPlayerOptions.getRepeatMode() == RepeatMode.NONE && mPlayOrderPosition == getInternalQueue().size() - 1) {
                newPosition = -1;
            } else {
                newPosition = mPlayOrderPosition + 1;
            }
        }
        int newExternalPosition = resolveIntToExt(newPosition);
        if (select) {
            mPlayOrderPosition = newPosition;
            mPlaylistPosition = newExternalPosition;
        }
        if (newPosition == -1) {
            return null;
        } else {
            return getInternalQueue().get(newExternalPosition);
        }
    }

    public synchronized JXObject getNext(boolean set) {
        int nextPosition;
        if (getInternalQueue().isEmpty()) {
            nextPosition = -1;
        } else if (mPlayOrderPosition == getInternalQueue().size() - 1) {
            nextPosition = 0;
        } else {
            nextPosition = mPlayOrderPosition + 1;
        }
        int nextExternalPosition = resolveIntToExt(nextPosition);
        if (set) {
            mPlayOrderPosition = nextPosition;
            mPlaylistPosition = nextExternalPosition;
        }
        return nextPosition != -1 ? getInternalQueue().get(nextExternalPosition) : null;
    }

    public synchronized JXObject getPrevious(boolean set) {
        int previousPosition;
        if (getInternalQueue().isEmpty()) {
            previousPosition = -1;
        } else if (mPlayOrderPosition < 1 && getInternalQueue().size() > 0) {
            previousPosition = getInternalQueue().size() - 1;
        } else {
            previousPosition = mPlayOrderPosition - 1;
        }
        int previousExternalPosition = resolveIntToExt(previousPosition);
        if (set) {
            mPlayOrderPosition = previousPosition;
            mPlaylistPosition = previousExternalPosition;
        }
        return previousPosition != -1 ? getInternalQueue().get(previousExternalPosition) : null;
    }

    public synchronized void move(int from, int to, boolean notify) {
        JXObject jogger = getInternalQueue().remove(from);
        getInternalQueue().add(to, jogger);
        if (getCurrentPosition() == from) {
            setCurrentPlayingPosition(to);
        } else if (getCurrentPosition() > from && getCurrentPosition() <= to) {
            // FROM | CURRENT | TO ---> CURRENT | FROM | TO
            setCurrentPlayingPosition(getCurrentPosition() - 1);
        } else if (getCurrentPosition() < from && getCurrentPosition() >= to) {
            // TO  | CURRENT | FROM ---> TO | FROM | CURRENT
            setCurrentPlayingPosition(getCurrentPosition() + 1);
        }
        // TODO what about shuffle?
        if (notify)
            notifyOnDataChanged();
    }

    public synchronized void addTracks(List<Track> tracks) {
        for (Track t : tracks) {
            getInternalQueue().add(t.getJXObject());
        }
        updateShuffle();
        notifyOnDataChanged();
    }

    public synchronized void add(JXObject object, int position) {
        getInternalQueue().add(position, object);
        if (position <= mPlayOrderPosition)
            mPlayOrderPosition++;
        updateShuffle();
        notifyOnDataChanged();
    }

    public synchronized int add(JXObject object) {
        Logy.d(TAG, "Adding:" + object.getLibrarySource());
        getInternalQueue().add(object);
        int pos = getInternalQueue().size() - 1;
        updateShuffle();
        notifyOnDataChanged();
        return pos;
    }

    public synchronized void add(List<JXObject> objects) {
        for (JXObject o : objects) {
            getInternalQueue().add(o);
        }
        updateShuffle();
        notifyOnDataChanged();
    }

    public synchronized void removeLastOccurrenceOf(List<JXObject> objects) {
        for (JXObject object : objects) {
            int pos = getInternalQueue().lastIndexOf(object);
            if (pos != -1)
                remove(pos, false);
        }
        updateShuffle();
        notifyOnDataChanged();
    }

    public synchronized void remove(int position) {
        remove(position, true);
    }

    public synchronized void remove(int position, boolean update) {
        if (position == mPlaylistPosition) {
            mPlaylistPosition = -1;
        } else if (position < mPlaylistPosition) {
            mPlaylistPosition--;
        }
        getInternalQueue().remove(position);
        if (update) {
            updateShuffle();
            notifyOnDataChanged();
        }
    }

    public synchronized JXObject get(int position) {
        return getInternalQueue().get(position);
    }

    public RepeatMode getRepeatMode() {
        return mPlayerOptions.getRepeatMode();
    }

    public PlayerOptions.ShuffleMode getShuffleMode() {
        return mPlayerOptions.getShuffleMode();
    }

    public void setRepeatMode(RepeatMode repeat) {
        mPlayerOptions.setRepeatMode(repeat);
    }


    public synchronized void setShuffleMode(PlayerOptions.ShuffleMode shuffleMode) {
        mPlayerOptions.setShuffleMode(shuffleMode);
        updateShuffle();
        notifyOnDataChanged();
    }

    public synchronized void clear() {
        getInternalQueue().clear();
        mPlaylistPosition = -1; // Cleared the queue, not playing any entry out of it, but the player may still play a track.
        updateShuffle();
        notifyOnDataChanged();
    }

    public synchronized int size() {
        return getInternalQueue().size();
    }

    public synchronized boolean hasTracks() {
        return !getInternalQueue().isEmpty();
    }

    public synchronized boolean isEmpty() {
        return getInternalQueue().isEmpty();
    }

    public PlayerOptions getOptions() {
        return mPlayerOptions;
    }

    public void publicNotifyDataChanged() {
        notifyOnDataChanged();
    }

    private void notifyOnDataChanged() {
        // FIXME inefficient to do the full save everytime
        saveState();
        for (JXQueueListener l : mListeners) {
            l.onPlaylistDataChanged(this);
        }
    }

    public void addListener(JXQueueListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(JXQueueListener listener) {
        mListeners.remove(listener);
    }

    public interface JXQueueListener {
        void onPlaylistDataChanged(Queue queue);
    }

    public synchronized void saveState() {
        long start = System.currentTimeMillis();
        QueueDB db = QueueDB.getInstance(getContext());
        db.setQueue(getInternalQueue().getContent());
        long stop = System.currentTimeMillis();
        Logy.d(TAG, "Queue time to store:" + (stop - start));
    }

    public synchronized void loadState() {
        QueueDB db = QueueDB.getInstance(getContext());
        List<Uri> uriList = db.getQueue();

        JXObjectFactory jxObjectFactory = new JXObjectFactory(mContext);
        List<JXObject> restoredObjects = jxObjectFactory.make(uriList);
        for (JXObject restored : restoredObjects) {
            uriList.remove(restored.getLibrarySource());
            getInternalQueue().add(restored);
        }

        for (Uri orphan : uriList)
            Logy.w(TAG, "Couldn't restore: " + orphan);

        updateShuffle();

        for (JXQueueListener l : mListeners)
            l.onPlaylistDataChanged(this);

    }


}
