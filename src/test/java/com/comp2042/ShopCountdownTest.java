package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopCountdownTest {
    @Test
    void countsDownFromSixtyToZeroAndStaysThere() {
        ShopCountdown countdown = new ShopCountdown();

        assertEquals(60, countdown.getRemainingSeconds());
        assertFalse(countdown.isFinished());

        for (int second = 59; second >= 0; second--) {
            assertEquals(second, countdown.tick());
        }

        assertTrue(countdown.isFinished());
        assertEquals(0, countdown.tick());
    }

    @Test
    void resetRestoresSixtySeconds() {
        ShopCountdown countdown = new ShopCountdown();
        countdown.tick();

        countdown.reset();

        assertEquals(60, countdown.getRemainingSeconds());
        assertFalse(countdown.isFinished());
    }
}
