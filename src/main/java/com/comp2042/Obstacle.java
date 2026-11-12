package com.comp2042;

import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Obstacle {
    private static final int MAX_OBSTACLES = 50;

    private Color color = Color.BLACK;
    private ArrayList<Point> obstacles = new ArrayList<>();
    private int x;
    private int y;

    public Obstacle(int x, int y) {
       this.x = x;
       this.y = y;
    }

    public void newObstacles(Snake snek, Coin coin, int amount) {
        int remainingCapacity = MAX_OBSTACLES - obstacles.size();
        if (amount <= 0 || remainingCapacity <= 0) {
            return;
        }

        ArrayList<Point> availableCells = new ArrayList<>();
        Point coinPosition = coin.getPos();
        for (int cellX = 0; cellX < x; cellX++) {
            for (int cellY = 0; cellY < y; cellY++) {
                Point candidate = new Point(cellX, cellY);
                if (!candidate.equals(coinPosition)
                        && !candidate.equals(snek.getHead())
                        && !snek.getBody().contains(candidate)
                        && !obstacles.contains(candidate)) {
                    availableCells.add(candidate);
                }
            }
        }

        Collections.shuffle(availableCells, new Random());
        int cellsToAdd = Math.min(amount, Math.min(availableCells.size(), remainingCapacity));
        obstacles.addAll(availableCells.subList(0, cellsToAdd));
    }

    public Color getColor() {
        return color;
    }

    public ArrayList<Point> getObstacles() {
        return obstacles;
    }

    public void reset() {
        obstacles.clear();
    }
}
