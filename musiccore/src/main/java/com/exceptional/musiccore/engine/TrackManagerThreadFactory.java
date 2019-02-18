package com.exceptional.musiccore.engine;

import com.exceptional.musiccore.MusicCoreApplication;
import com.exceptional.musiccore.utils.Logy;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

public class TrackManagerThreadFactory implements ThreadFactory {
    private static final String TAG = MusicCoreApplication.MUSICCORE_TAG_PREFIX + "Engine:ThreadFactory";
    private final Map<Thread, Integer> threadMap = new ConcurrentHashMap<>();

    public TrackManagerThreadFactory() {

    }

    public void setAll(int priority) {
        Logy.d(TAG, "Lowering all priorities to " + priority);
        for (Entry<Thread, Integer> entry : threadMap.entrySet()) {
            int was = android.os.Process.getThreadPriority(entry.getValue());
            android.os.Process.setThreadPriority(entry.getValue(), priority);
            Logy.d(TAG, "Priority was (" + was + ") is now (" + android.os.Process.getThreadPriority(entry.getValue()) + ")");
        }
    }

    @Override
    public Thread newThread(final Runnable r) {
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int tid = android.os.Process.myTid();
                threadMap.put(Thread.currentThread(), tid);
                r.run();
            }
        });
        t.setName("JX:Thread:" + threadMap.size());
        return t;

    }
}
