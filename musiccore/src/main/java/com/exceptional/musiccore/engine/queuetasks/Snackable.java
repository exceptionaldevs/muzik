package com.exceptional.musiccore.engine.queuetasks;

public interface Snackable {
    public JXTask getSnackTask();

    public JXTask setUndoTask(JXTask undoTask);
}