package com.exceptionaldevs.muzyka;

/**
 * Created by sebnap on 31.01.16.
 */
public interface QueueRotationCall {
    boolean shouldQueueRotationCall();
    void queueRotationCall(Runnable run);
}
