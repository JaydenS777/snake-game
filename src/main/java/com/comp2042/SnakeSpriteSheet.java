package com.comp2042;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;

/** Source rectangles for the user-approved four-part pixel snake artwork. */
final class SnakeSpriteSheet {
    static final int IMAGE_WIDTH = 2172;
    static final int IMAGE_HEIGHT = 724;
    static final String RESOURCE = "resources/views/res/snake-sprites.png";

    static final SpriteRegion HEAD = new SpriteRegion(104, 156, 336, 410);
    // The sheet repeats several body links vertically; use one complete straight link.
    static final SpriteRegion BODY = new SpriteRegion(704, 147, 222, 173);
    static final SpriteRegion TURN = new SpriteRegion(1177, 147, 379, 406);
    static final SpriteRegion TAIL = new SpriteRegion(1754, 148, 290, 425);

    private SnakeSpriteSheet() {
    }

    static URL resourceUrl() {
        return SnakeSpriteSheet.class.getResource("/" + RESOURCE);
    }

    static Image load() {
        URL resource = resourceUrl();
        if (resource == null) {
            throw new IllegalStateException("Snake sprite sheet is missing from the application resources.");
        }
        Image image = new Image(resource.toExternalForm(), false);
        if (image.isError()) {
            throw new IllegalStateException("Snake sprite sheet could not be decoded.", image.getException());
        }
        return image;
    }

    static ImageView createView(Image sheet, SpriteRegion region) {
        ImageView view = new ImageView(sheet);
        view.setViewport(new Rectangle2D(region.x(), region.y(), region.width(), region.height()));
        view.setFitWidth(GameDimensions.CELL_SIZE);
        view.setFitHeight(GameDimensions.CELL_SIZE);
        view.setPreserveRatio(false);
        view.setSmooth(false);
        return view;
    }

    record SpriteRegion(int x, int y, int width, int height) {
        boolean fitsInside(int imageWidth, int imageHeight) {
            return x >= 0 && y >= 0 && width > 0 && height > 0
                    && x + width <= imageWidth && y + height <= imageHeight;
        }
    }
}
