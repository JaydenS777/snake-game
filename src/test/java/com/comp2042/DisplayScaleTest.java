package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisplayScaleTest {
    @Test
    void fullscreenScaleFitsTheAvailableScreenWithoutCropping() {
        assertEquals(2.7, DisplayScale.fullscreenScale(1920, 1080, 400, 400), 0.0001);
    }

    @Test
    void fullscreenScaleUsesTheShorterScreenDimension() {
        assertEquals(1.5, DisplayScale.fullscreenScale(1000, 600, 400, 400), 0.0001);
    }

    @Test
    void rejectsInvalidDisplayOrCanvasDimensions() {
        assertThrows(IllegalArgumentException.class,
                () -> DisplayScale.fullscreenScale(0, 1080, 400, 400));
    }

    @Test
    void windowedModeKeepsNativeSizeAndTextScale() {
        assertEquals(1.0, DisplayScale.WINDOWED_SCALE);
        assertEquals(1.0, DisplayScale.uiScale(DisplayScale.WINDOWED_SCALE));
    }

    @Test
    void windowedScaleKeepsTheCanvasWithinTheLargeWindowViewport() {
        assertEquals(0.8, DisplayScale.windowedScale(2560, 1440, 2560, 1440), 0.0001);
        assertEquals(1.0, DisplayScale.windowedScale(2560, 1440, 2040, 1140), 0.0001);
    }

    @Test
    void mainMenuUsesIntegerScaleForLargeDisplays() {
        assertEquals(2.0, DisplayScale.mainMenuScale(2560, 1440));
        assertEquals(3.0, DisplayScale.mainMenuScale(3840, 2160));
        assertEquals(1.0, DisplayScale.mainMenuScale(1280, 720));
    }

    @Test
    void scalesOverlayWithTheCanvasAndCapsItOnVeryLargeDisplays() {
        assertEquals(2.7, DisplayScale.uiScale(2.7), 0.0001);
        assertEquals(3.0, DisplayScale.uiScale(4.5), 0.0001);
    }
}
