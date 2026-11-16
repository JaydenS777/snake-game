package com.comp2042;

import javafx.scene.image.Image;

public class GoldCoin extends Coin {
    private Image image;

    public GoldCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    GoldCoin(java.awt.Point position) {
        super(position);
    }

    public Image getImage() {
        if (image == null) {
            image = new Image("resources/views/res/goldcoin-v4.png");
        }
        return image;
    }

    public int getPoints() {
        int points = 15;
        return points;
    }
}
