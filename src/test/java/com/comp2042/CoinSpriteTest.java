package com.comp2042;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoinSpriteTest {
    private static final String RESOURCE_ROOT = "/resources/views/res/";
    private static final int COIN_CENTER = 282;

    @Test
    void redAndGoldCoinsHaveMatchingTransparentPixelArtShapes() throws Exception {
        BufferedImage red = load("redcoin-v4.png");
        BufferedImage gold = load("goldcoin-v4.png");

        assertEquals(564, red.getWidth());
        assertEquals(564, red.getHeight());
        assertEquals(red.getWidth(), gold.getWidth());
        assertEquals(red.getHeight(), gold.getHeight());
        assertEquals(0, alpha(red, 0, 0));
        assertEquals(0, alpha(gold, 0, 0));
        assertEquals(255, alpha(red, COIN_CENTER, COIN_CENTER));
        assertEquals(255, alpha(gold, COIN_CENTER, COIN_CENTER));
        assertTrue((red.getRGB(COIN_CENTER, COIN_CENTER) & 0x00FFFFFF)
                        != (gold.getRGB(COIN_CENTER, COIN_CENTER) & 0x00FFFFFF),
                "The red and gold coin centers should remain visually distinct.");
        int[] redBounds = opaqueBounds(red);
        int[] goldBounds = opaqueBounds(gold);
        assertTrue(Math.abs((redBounds[2] - redBounds[0]) - (goldBounds[2] - goldBounds[0])) <= 8,
                "The red and gold coins should have nearly the same visible size.");
        assertTrue(Math.abs((redBounds[3] - redBounds[1]) - (goldBounds[3] - goldBounds[1])) <= 8,
                "The red and gold coins should have nearly the same visible size.");

        for (int y = 0; y < red.getHeight(); y++) {
            for (int x = 0; x < red.getWidth(); x++) {
                assertTrue(alpha(red, x, y) == 0 || alpha(red, x, y) == 255,
                        "The red coin should not contain semi-transparent background pixels.");
                assertTrue(alpha(gold, x, y) == 0 || alpha(gold, x, y) == 255,
                        "The gold coin should not contain semi-transparent background pixels.");
            }
        }
    }

    private int[] opaqueBounds(BufferedImage image) {
        int minX = image.getWidth(), minY = image.getHeight();
        int maxX = -1, maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (alpha(image, x, y) > 0) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return new int[]{minX, minY, maxX + 1, maxY + 1};
    }

    private BufferedImage load(String fileName) throws Exception {
        try (InputStream input = getClass().getResourceAsStream(RESOURCE_ROOT + fileName)) {
            assertNotNull(input, "Missing coin resource: " + fileName);
            return ImageIO.read(input);
        }
    }

    private int alpha(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) >>> 24;
    }
}
