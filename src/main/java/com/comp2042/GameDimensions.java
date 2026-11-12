package com.comp2042;

final class GameDimensions {
    static final int CELL_SIZE = 40;
    static final double WINDOWED_SCREEN_FRACTION = 0.8;

    private GameDimensions() {
    }

    static Grid forFullscreen(double screenWidth, double screenHeight) {
        return fit(screenWidth, screenHeight);
    }

    static Grid forWindowed(double screenWidth, double screenHeight) {
        return fit(screenWidth * WINDOWED_SCREEN_FRACTION,
                screenHeight * WINDOWED_SCREEN_FRACTION);
    }

    private static Grid fit(double width, double height) {
        if (!Double.isFinite(width) || !Double.isFinite(height) || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Viewport dimensions must be positive and finite.");
        }

        int columns = Math.max(1, (int) Math.floor(width / CELL_SIZE));
        int rows = Math.max(1, (int) Math.floor(height / CELL_SIZE));
        return new Grid(columns, rows, CELL_SIZE);
    }

    record Grid(int columns, int rows, int cellSize) {
        Grid {
            if (columns <= 0 || rows <= 0 || cellSize <= 0) {
                throw new IllegalArgumentException("Grid dimensions and cell size must be positive.");
            }
        }

        double canvasWidth() {
            return (double) columns * cellSize;
        }

        double canvasHeight() {
            return (double) rows * cellSize;
        }
    }
}
