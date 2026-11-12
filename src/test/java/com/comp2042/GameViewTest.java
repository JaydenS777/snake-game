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

    @Test
    void timeLabelShowsTheRemainingCountdown() {
        assertEquals("01:00", GameView.timeText(60));
        assertEquals("00:09", GameView.timeText(9));
        assertEquals("00:00", GameView.timeText(0));
    }

    @Test
    void timeProgressBarShrinksWithTheRemainingTime() {
        assertEquals(1.0, GameView.timeProgress(60), 0.0001);
        assertEquals(0.5, GameView.timeProgress(30), 0.0001);
        assertEquals(0.0, GameView.timeProgress(0), 0.0001);
        assertEquals(0.0, GameView.timeProgress(-5), 0.0001);
    }

    @Test
    void shopCurrencyTextFormatsTheScoreAsAReadableCoinAmount() {
        assertEquals("0", GameView.shopPointsText(0));
        assertEquals("2,450", GameView.shopPointsText(2450));
    }

    @Test
    void gameBackgroundMatchesTheRequestedSolidDarkGreen() {
        assertEquals("#07210b", GameView.GAME_BACKGROUND_COLOR);
    }
}
