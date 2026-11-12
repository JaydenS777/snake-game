package com.comp2042;

final class DisplayScale {
    static final double WINDOWED_SCALE = 1.0;

    private DisplayScale() {
    }

    static double fullscreenScale(double screenWidth, double screenHeight,
                                 double canvasWidth, double canvasHeight) {
        if (!positiveFinite(screenWidth) || !positiveFinite(screenHeight)
                || !positiveFinite(canvasWidth) || !positiveFinite(canvasHeight)) {
            throw new IllegalArgumentException("Display and canvas dimensions must be positive and finite.");
        }
        return Math.max(1.0, Math.min(screenWidth / canvasWidth, screenHeight / canvasHeight));
    }

    static double windowedScale(double screenWidth, double screenHeight,
                                double canvasWidth, double canvasHeight) {
        if (!positiveFinite(screenWidth) || !positiveFinite(screenHeight)
                || !positiveFinite(canvasWidth) || !positiveFinite(canvasHeight)) {
            throw new IllegalArgumentException("Display and canvas dimensions must be positive and finite.");
        }
        double targetWidth = screenWidth * GameDimensions.WINDOWED_SCREEN_FRACTION;
        double targetHeight = screenHeight * GameDimensions.WINDOWED_SCREEN_FRACTION;
        return Math.min(1.0, Math.min(targetWidth / canvasWidth, targetHeight / canvasHeight));
    }

    static double mainMenuScale(double viewportWidth, double viewportHeight) {
        if (!positiveFinite(viewportWidth) || !positiveFinite(viewportHeight)) {
            throw new IllegalArgumentException("Viewport dimensions must be positive and finite.");
        }
        double fitScale = Math.min(viewportWidth / 1000.0, viewportHeight / 600.0);
        return Math.max(1.0, Math.min(3.0, Math.round(fitScale)));
    }

    static double uiScale(double displayScale) {
        if (!positiveFinite(displayScale)) {
            throw new IllegalArgumentException("Display scale must be positive and finite.");
        }
        return Math.min(displayScale, 3.0);
    }

    private static boolean positiveFinite(double value) {
        return Double.isFinite(value) && value > 0.0;
    }
}
