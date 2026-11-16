package com.comp2042;

import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import java.awt.Point;

public class BlueCoin extends Coin {
    // Outline of the coin in the supplied reference image coordinates.
    private static final String OUTLINE = "M676 156 H860 V184 H924 V212 H974 V241 H1009 V268 "
            + "H1042 V318 H1066 V371 H1078 V402 H1103 V612 H1078 V663 H1044 V713 "
            + "H1010 V744 H969 V775 H921 V810 H858 V838 H676 V811 H614 V783 "
            + "H564 V752 H514 V713 H482 V663 H458 V612 H433 V404 H458 V369 H484 V318 "
            + "H513 V267 H566 V239 H618 V211 H670 V183 H676 Z";
    private Image image;

    public BlueCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    BlueCoin(Point position) {
        super(position);
    }

    @Override
    public int getPoints() {
        return 10;
    }

    @Override
    boolean growsSnake() {
        return false;
    }

    @Override
    public Image getImage() {
        if (image == null) {
            image = new Image("resources/views/res/blue-coin-reference.jpg");
        }
        return image;
    }

    @Override
    void draw(GraphicsContext graphics, double x, double y, double size) {
        graphics.save();
        graphics.translate(x, y);
        double scale = size / 682;
        graphics.scale(scale, scale);
        graphics.translate(-427, -156);
        graphics.beginPath();
        graphics.appendSVGPath(OUTLINE);
        graphics.clip();
        graphics.drawImage(getImage(), 0, 0);
        graphics.restore();
        graphics.beginPath();
    }
}
