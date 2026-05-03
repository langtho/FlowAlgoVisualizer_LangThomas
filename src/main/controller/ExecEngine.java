package main.controller;

public class ExecEngine {
    private volatile boolean paused = true;
    private int delay = 500;
    private boolean stepRequested = false;

    public void waitIfNeeded() throws InterruptedException {
        synchronized (this) {
            while (paused && !stepRequested) {
                wait();
            }
        }

        if (delay > 0 && !stepRequested) {
            Thread.sleep(delay);
        }

        synchronized (this) {
            if (stepRequested) {
                stepRequested = false;
                paused = true;
            }
        }
    }

    public synchronized void reset() {
        this.paused = true;
        this.stepRequested = false;
    }

    public synchronized void pause() { paused = true; notifyAll(); }
    public synchronized void resume() { paused = false; notifyAll(); }
    public synchronized void requestStep() { stepRequested = true; paused = false; notifyAll(); }

    public void setDelay(int d, javax.swing.Timer playbackTimer) { this.delay = d;
        if (playbackTimer != null) {
            playbackTimer.setDelay(d);
        }
       }
    public boolean isPaused() { return paused; }
    public int getDelay(){return delay;}
}