package com.exceptional.musiccore.engine.queuetasks;

public abstract class JXTask implements Runnable {
    private String mTag;
    private JXTaskCallback mCallback;
    protected State mState = State.NEW;

    enum State {
        NEW, QUEUED, RUNNING, FINISHED
    }

    public JXTask() {
    }

    protected void setCallback(JXTaskCallback callback) {
        mCallback = callback;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public String getTag() {
        return mTag;
    }

    public abstract void onDo();

    @Override
    public void run() {
        mState = State.RUNNING;
        onDo();
        mState = State.FINISHED;
        mCallback.onTaskDone(this);
    }

    public interface JXTaskCallback {
        void onTaskDone(JXTask task);
    }
}