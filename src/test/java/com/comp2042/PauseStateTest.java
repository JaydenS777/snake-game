package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PauseStateTest {

    @Test
    void resumesAnimationOnlyWhenItWasRunningBeforePause() {
        PauseState state = new PauseState();

        state.pause(true);
        assertTrue(state.isPaused());
        assertTrue(state.resume());
        assertFalse(state.isPaused());

        state.pause(false);
        assertTrue(state.isPaused());
        assertFalse(state.resume());
        assertFalse(state.isPaused());
    }

    @Test
    void resetClearsPausedStateAndResumeFlag() {
        PauseState state = new PauseState();
        state.pause(true);

        state.reset();

        assertFalse(state.isPaused());
        assertFalse(state.resume());
    }
}
