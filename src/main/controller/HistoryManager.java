package main.controller;

import java.util.ArrayList;
import java.util.List;

public class HistoryManager {
    private final List<StepSaveStruct> snapshots = new ArrayList<>();
    private int currentIndex = -1;

    public void add(StepSaveStruct snapshot) {
        snapshots.add(snapshot);
        if (currentIndex == snapshots.size() - 2 || currentIndex == -1) {
            currentIndex = snapshots.size() - 1;
        }
    }

    public StepSaveStruct get(int index) { return snapshots.get(index); }
    public StepSaveStruct getCurrent() { return snapshots.get(currentIndex); }

    public boolean canStepBack() { return currentIndex > 0; }
    public boolean canStepForward() { return currentIndex < snapshots.size() - 1; }

    public void moveBack() { if (canStepBack()) currentIndex--; }
    public void moveForward() { if (canStepForward()) currentIndex++; }

    public void clear() { snapshots.clear(); currentIndex = -1; }
    public int size() { return snapshots.size(); }
    public int getIndex() { return currentIndex; }
    public void setIndex(int i) { this.currentIndex = i; }
}
