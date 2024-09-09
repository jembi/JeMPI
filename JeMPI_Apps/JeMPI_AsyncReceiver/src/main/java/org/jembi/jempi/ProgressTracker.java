package org.jembi.jempi;

public final class ProgressTracker {
    private static ProgressTracker instance;
    private int progress;

    private ProgressTracker() {
    }

    public static synchronized ProgressTracker getInstance() {
        if (instance == null) {
            instance = new ProgressTracker();
        }
        return instance;
    }

    public synchronized void setProgress(final int newProgress) {
        this.progress = newProgress;
    }

    public synchronized int getProgress() {
        return progress;
    }
}
