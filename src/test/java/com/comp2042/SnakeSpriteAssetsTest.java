package com.comp2042;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnakeSpriteAssetsTest {
    @Test
    void runtimeSpriteLoaderFindsTheSheetOnTheApplicationClasspath() {
        assertNotNull(SnakeSpriteSheet.resourceUrl());
    }

    @Test
    void timerPanelReferenceImageIsAvailableAtItsOriginalResolution() throws IOException {
        BufferedImage image = load("timer-ui-reference.jpg");
        assertEquals(1073, image.getWidth());
        assertEquals(445, image.getHeight());
    }

    @Test
    void shopCurrencyCoinIsTheSMarkedCoinWithTransparentCorners() throws IOException {
        BufferedImage coin = load("shop-coin.png");

        assertTrue(coin.getWidth() > 0);
        assertTrue(coin.getHeight() > 0);
        assertEquals(0, coin.getRGB(0, 0) >>> 24);
        assertEquals(0, coin.getRGB(coin.getWidth() - 1, 0) >>> 24);
        assertEquals(0, coin.getRGB(0, coin.getHeight() - 1) >>> 24);
        assertEquals(0, coin.getRGB(coin.getWidth() - 1, coin.getHeight() - 1) >>> 24);
        assertTrue((coin.getRGB(coin.getWidth() / 2, coin.getHeight() / 2) >>> 24) > 0);
    }

    @Test
    void approvedSpriteSheetHasFourValidArtworkRegions() throws IOException {
        BufferedImage sheet = load("snake-sprites.png");

        assertEquals(SnakeSpriteSheet.IMAGE_WIDTH, sheet.getWidth());
        assertEquals(SnakeSpriteSheet.IMAGE_HEIGHT, sheet.getHeight());
        for (SnakeSpriteSheet.SpriteRegion region : new SnakeSpriteSheet.SpriteRegion[]{
                SnakeSpriteSheet.HEAD, SnakeSpriteSheet.BODY,
                SnakeSpriteSheet.TURN, SnakeSpriteSheet.TAIL}) {
            assertTrue(region.fitsInside(sheet.getWidth(), sheet.getHeight()));
            assertTrue(hasVisiblePixel(sheet, region));
        }
    }

    @Test
    void snakeSpritesHaveMatchingCellDimensions() throws IOException {
        for (String name : new String[]{"snakehead-v2.png", "snakebody-v2.png",
                "snaketurn-v2.png", "snaketail-v2.png"}) {
            BufferedImage image = load(name);
            assertEquals(40, image.getWidth(), name + " width");
            assertEquals(40, image.getHeight(), name + " height");
        }
    }

    @Test
    void adjacentSpritePortsMatchWithoutTransparentSeams() throws IOException {
        BufferedImage head = load("snakehead-v2.png");
        BufferedImage body = load("snakebody-v2.png");
        BufferedImage turn = load("snaketurn-v2.png");
        BufferedImage tail = load("snaketail-v2.png");

        int[] bodyPort = row(body, 0);
        assertArrayEquals(bodyPort, row(body, 39));
        assertArrayEquals(bodyPort, row(head, 39));
        assertArrayEquals(bodyPort, row(turn, 0));
        assertArrayEquals(bodyPort, row(tail, 0));
        assertArrayEquals(Arrays.copyOfRange(bodyPort, 6, 34),
                Arrays.copyOfRange(column(turn, 0), 6, 34));

        for (int x = 6; x < 34; x++) {
            assertTrue((body.getRGB(x, 0) >>> 24) > 0);
            assertTrue((head.getRGB(x, 39) >>> 24) > 0);
            assertTrue((turn.getRGB(x, 0) >>> 24) > 0);
            assertTrue((tail.getRGB(x, 0) >>> 24) > 0);
        }
    }

    private BufferedImage load(String name) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/resources/views/res/" + name)) {
            assertNotNull(stream, name + " should be on the classpath");
            return ImageIO.read(stream);
        }
    }

    private int[] row(BufferedImage image, int y) {
        return image.getRGB(0, y, image.getWidth(), 1, null, 0, image.getWidth());
    }

    private int[] column(BufferedImage image, int x) {
        return image.getRGB(x, 0, 1, image.getHeight(), null, 0, 1);
    }

    private boolean hasVisiblePixel(BufferedImage image, SnakeSpriteSheet.SpriteRegion region) {
        for (int y = region.y(); y < region.y() + region.height(); y++) {
            for (int x = region.x(); x < region.x() + region.width(); x++) {
                if ((image.getRGB(x, y) >>> 24) > 16) return true;
            }
        }
        return false;
    }
}
