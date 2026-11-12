package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameViewTest {

    @Test
    void restartButtonIsOnlyShownAfterGameEnds() {
        assertFalse(GameView.shouldShowRestartButton(false));
        assertTrue(GameView.shouldShowRestartButton(true));
    }

    @Test
    void gameOverOverlayIsShownOnlyWhenTheSnakeIsDead() {
        assertFalse(GameView.shouldShowGameOverOverlay(false));
        assertTrue(GameView.shouldShowGameOverOverlay(true));
    }

    @Test
    void finalScoreTextShowsTheScoreAtGameOver() {
        assertEquals("Final Score: 42", GameView.finalScoreText(42));
    }
}
