package com.comp2042;

final class PauseState {
    private boolean paused;
    private boolean resumeAnimationOnResume;

    boolean isPaused() {
        return paused;
    }

    void pause(boolean animationWasRunning) {
        if (paused) {
            return;
        }
        paused = true;
        resumeAnimationOnResume = animationWasRunning;
    }

    boolean resume() {
        if (!paused) {
            return false;
        }

        paused = false;
        boolean shouldResumeAnimation = resumeAnimationOnResume;
        resumeAnimationOnResume = false;
        return shouldResumeAnimation;
    }

    void reset() {
        paused = false;
        resumeAnimationOnResume = false;
    }
}
