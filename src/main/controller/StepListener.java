package main.controller;

public interface StepListener {
    void onStep(String message, boolean isMajor);
}
