package com.exceptional.musiccore.engine.queuetasks;

public abstract class JXSnackTask extends JXTask implements Snackable {
    private JXTask mSnackTask;
    private String mSnackMessage;
    private String mSnackActionName;

    public JXSnackTask() {
        super();
    }

    public JXSnackTask(String snackMessage, String snackActionName) {
        super();
        setSnackMessage(snackMessage);
        setSnackActionName(snackActionName);
    }

    @Override
    public JXTask getSnackTask() {
        return mSnackTask;
    }

    @Override
    public JXTask setUndoTask(JXTask undoTask) {
        return mSnackTask = undoTask;
    }

    public String getSnackMessage() {
        return mSnackMessage;
    }

    public String getSnackActionName() {
        return mSnackActionName;
    }

    public void setSnackMessage(String snackMessage) {
        mSnackMessage = snackMessage;
    }

    public void setSnackActionName(String snackActionName) {
        mSnackActionName = snackActionName;
    }
}