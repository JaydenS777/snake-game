package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameDimensionsTest {
    @Test
    void fullscreenGridUsesTheWholeTwoKViewportAtFortyPixelCells() {
        GameDimensions.Grid grid = GameDimensions.forFullscreen(2560, 1440);

        assertEquals(64, grid.columns());
        assertEquals(36, grid.rows());
        assertEquals(40, grid.cellSize());
        assertEquals(2560, grid.canvasWidth(), 0.001);
        assertEquals(1440, grid.canvasHeight(), 0.001);
    }

    @Test
    void windowedGridUsesAnEightyPercentViewportInsteadOfTheOldSmallSquare() {
        GameDimensions.Grid grid = GameDimensions.forWindowed(2560, 1440);

        assertEquals(51, grid.columns());
        assertEquals(28, grid.rows());
        assertEquals(2040, grid.canvasWidth(), 0.001);
        assertEquals(1120, grid.canvasHeight(), 0.001);
    }

    @Test
    void supportsDifferentScreenShapesAndSizes() {
        GameDimensions.Grid grid = GameDimensions.forFullscreen(1920, 1080);

        assertEquals(48, grid.columns());
        assertEquals(27, grid.rows());
    }

    @Test
    void snakeWrapsAtTheOuterEdgeOfTheScreenSizedGrid() {
        GameDimensions.Grid grid = GameDimensions.forFullscreen(2560, 1440);
        Obstacle obstacle = new Obstacle(grid.columns(), grid.rows());
        Snake snake = new Snake(grid.columns(), grid.rows(), true, obstacle);

        for (int step = 0; step <= grid.columns() / 2; step++) {
            snake.move('L');
        }

        assertEquals(grid.columns() - 1, snake.getHead().x);
    }

    @Test
    void rejectsInvalidViewportDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> GameDimensions.forFullscreen(0, 1440));
    }
}
