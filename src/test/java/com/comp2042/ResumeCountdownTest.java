package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeCountdownTest {
    @Test
    void countsDownFromThreeAndFinishesAfterOne() {
        ResumeCountdown countdown = new ResumeCountdown();

        countdown.start();
        assertEquals("3", countdown.currentNumber());
        assertTrue(countdown.isActive());

        assertEquals("2", countdown.advance());
        assertEquals("1", countdown.advance());
        assertEquals("", countdown.advance());
        assertFalse(countdown.isActive());
    }

    @Test
    void startResetsTheCountdown() {
        ResumeCountdown countdown = new ResumeCountdown();

        countdown.start();
        countdown.advance();
        countdown.start();

        assertEquals("3", countdown.currentNumber());
    }
}
