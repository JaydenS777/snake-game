package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameTest {
    @Test
    void spendingCoinsDeductsOnlyWhenTheBalanceIsEnough() {
        Game game = new Game(20, 20, false, 'N');
        game.addPoints(250);

        assertTrue(game.spendPoints(200));
        assertEquals(50, game.getScorePoints());
        assertFalse(game.spendPoints(200));
        assertEquals(50, game.getScorePoints());
    }
}
