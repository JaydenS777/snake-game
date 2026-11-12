package com.comp2042;

import javafx.scene.image.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Coin {
    private final Point pos;
    private Image image;

    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
        this(requirePosition(randomFreePosition(x, y, snek, obstacle, List.of(), new Random())));
    }

    Coin(Point position) {
        this.pos = new Point(position);
    }

    static Point randomFreePosition(int width, int height, Snake snek, Obstacle obstacle,
                                   List<Coin> existingCoins, Random random) {
        Set<Point> occupied = new HashSet<>(obstacle.getObstacles());
        occupied.add(snek.getHead());
        occupied.addAll(snek.getBody());
        for (Coin coin : existingCoins) {
            occupied.add(coin.getPos());
        }

        List<Point> freePositions = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Point candidate = new Point(x, y);
                if (!occupied.contains(candidate)) {
                    freePositions.add(candidate);
                }
            }
        }

        if (freePositions.isEmpty()) {
            return null;
        }
        return new Point(freePositions.get(random.nextInt(freePositions.size())));
    }

    private static Point requirePosition(Point position) {
        if (position == null) {
            throw new IllegalStateException("No free cell is available for a coin.");
        }
        return position;
    }

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    public Image getImage() {
        if (image == null) {
            image = new Image("resources/views/res/redcoin-v4.png");
        }
        return image;
    }
}
