package com.exceptional.musiccore.engine.queue;

import com.exceptional.musiccore.engine.JXObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by darken on 07/10/14.
 */
public class QueueInternal {

    private final List<JXObject> mContent = new ArrayList<>();

    /**
     * The content of the queue.
     * Use {@code JXPlaylist} methods to modify the list.
     *
     * @return a copy of the content list
     */
    protected synchronized List<JXObject> getContent() {
        return new ArrayList<>(mContent);
    }

    protected synchronized JXObject get(int position) {
        return mContent.get(position);
    }

    protected synchronized int size() {
        return mContent.size();
    }

    protected synchronized boolean isEmpty() {
        return mContent.isEmpty();
    }

    protected synchronized JXObject remove(int position) {
        return mContent.remove(position);
    }

    protected synchronized boolean remove(JXObject object) {
        return mContent.remove(object);
    }

    protected synchronized int lastIndexOf(JXObject object) {
        return mContent.lastIndexOf(object);
    }

    protected synchronized void add(JXObject object) {
        mContent.add(object);
    }

    protected synchronized void addAll(List<JXObject> objects) {
        mContent.addAll(objects);
    }

    protected synchronized void add(int position, JXObject object) {
        mContent.add(position, object);
    }

    protected synchronized void clear() {
        mContent.clear();
    }

}
