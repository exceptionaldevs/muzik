package com.exceptional.musiccore.engine.queuetasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JXTasker implements JXTask.JXTaskCallback {
    private final static int ALLOWED_WORKERS = 1;
    private final LinkedBlockingDeque<Runnable> mWorkerThreadQueue = new LinkedBlockingDeque<>();
    private ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(ALLOWED_WORKERS, ALLOWED_WORKERS, Long.MAX_VALUE, TimeUnit.NANOSECONDS, mWorkerThreadQueue);
    private Map<JXTask, JXTask.JXTaskCallback> mCallbackMap = new HashMap<>();
    private Map<String, JXTask> mJXTaskMap = new HashMap<>();

    public String execute(JXTask task, JXTask.JXTaskCallback callback) {
        String uuid = UUID.randomUUID().toString();
        task.setTag(uuid);
        task.setCallback(this);
        mCallbackMap.put(task, callback != null ? callback : this);
        mJXTaskMap.put(task.getTag(), task);
        mThreadPool.execute(task);
        task.mState = JXTask.State.QUEUED;
        return uuid;
    }

    public void addListener(String tag, JXTask.JXTaskCallback callback) {
        JXTask task = mJXTaskMap.get(tag);
        if (task != null) {
            synchronized (task) {
                if (task.mState == JXTask.State.FINISHED) {
                    mCallbackMap.remove(task);
                    mJXTaskMap.remove(task.getTag());
                    callback.onTaskDone(task);
                } else {
                    mCallbackMap.put(task, callback);
                }
            }
        }
    }

    public void removeListener(String tag, JXTask.JXTaskCallback callback) {
        JXTask task = mJXTaskMap.get(tag);
        if (task != null) {
            synchronized (task) {
                mCallbackMap.remove(task);
            }
        }
    }


    @Override
    public void onTaskDone(JXTask task) {
        synchronized (task) {
            if (mCallbackMap.containsKey(task)) {
                mJXTaskMap.remove(task.getTag());
                JXTask.JXTaskCallback callback = mCallbackMap.remove(task);
                if (callback != this)
                    callback.onTaskDone(task);
            }
        }
    }
}