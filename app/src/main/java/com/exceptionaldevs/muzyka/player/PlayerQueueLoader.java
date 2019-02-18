package com.exceptionaldevs.muzyka.player;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import com.exceptional.musiccore.engine.JXObject;
import com.exceptional.musiccore.engine.queue.Queue;
import com.exceptional.musiccore.library.tracks.Track;
import com.exceptionaldevs.muzyka.utils.Logy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darken on 26.03.14.
 */
public class PlayerQueueLoader extends AsyncTaskLoader<List<Track>> implements Queue.JXQueueListener {
    private static final String TAG = "PlayerPlaylistLoader";
    private List<Track> mData;
    private Queue mQueue;

    public PlayerQueueLoader(Context context, Queue queue) {
        super(context);
        mQueue = queue;
    }

    @Override
    public void deliverResult(List<Track> data) {
        if (isReset()) {
            //No no no, bad Android, keep your race conditions
            if (mData != null)
                onReleaseResources(mData);
        }
        List<Track> oldData = mData;
        mData = data;

        if (isStarted()) {
            super.deliverResult(data);
        }

        if (oldData != null)
            onReleaseResources(oldData);
    }

    @Override
    protected void onStartLoading() {
        mQueue.addListener(this);
        onContentChanged();
        if (takeContentChanged())
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    public void onCanceled(List<Track> data) {
        onReleaseResources(data);
    }

    @Override
    protected void onReset() {
        onStopLoading();
        mQueue.removeListener(this);
        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }
    }

    protected void onReleaseResources(List<Track> data) {

    }

    @Override
    public List<Track> loadInBackground() {
        Logy.d(TAG, "loadInBackground start...");
        long dur = System.currentTimeMillis();

        List<Track> result = new ArrayList<>();
        for (JXObject obj : mQueue.getTracks()) {
            result.add(new Track(obj));
        }

        Logy.d(TAG, "loadInBackground done:" + (System.currentTimeMillis() - dur) + " itemcnt:" + result.size());
        return result;
    }

    @Override
    public void onPlaylistDataChanged(Queue queue) {
        onContentChanged();
    }
}