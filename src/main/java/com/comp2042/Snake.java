package com.comp2042;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;

public class Snake {
    private Color headColor = Color.web("2eb872");
    private Color bodyColor = Color.web("a3de83");
    private boolean dead = false;
    private final Point head = new Point();
    private final ArrayList<Point> body = new ArrayList<>();
    private final int gx;
    private final int gy;
    private boolean ignoreBorders = true;
    private boolean noBorder;
    private Obstacle obstacle;
    private ImageView imageHead;
    private ImageView imageBody;
    private ImageView imageTurn;
    private ImageView imageTail;
    private Image spriteSheet;
    private int snakeDir = 270;
    private char direction = 'L';

    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        this.head.setLocation(x / 2, y / 2);
        this.body.add(new Point(x / 2 + 1, y / 2));
        this.gx = x;
        this.gy = y;
    }

    public void move(char dir) {
        if (dead || !canMove(dir)) {
            return;
        }

        int nextX = head.x;
        int nextY = head.y;
        switch (dir) {
            case 'U':
                nextY--;
                break;
            case 'D':
                nextY++;
                break;
            case 'L':
                nextX--;
                break;
            case 'R':
                nextX++;
                break;
            default:
                return;
        }

        if (nextX < 0 || nextY < 0 || nextX >= gx || nextY >= gy) {
            if (!ignoreBorders && !noBorder) {
                dead = true;
                return;
            }

            if (nextX < 0) nextX = gx - 1;
            else if (nextX >= gx) nextX = 0;
            if (nextY < 0) nextY = gy - 1;
            else if (nextY >= gy) nextY = 0;
        }

        Point nextHead = new Point(nextX, nextY);
        if (obstacle.getObstacles().contains(nextHead)) {
            dead = true;
            return;
        }

        body.add(new Point(head));
        head.setLocation(nextHead);
        direction = dir;
        snakeDir = switch (dir) {
            case 'U' -> 0;
            case 'R' -> 90;
            case 'D' -> 180;
            default -> 270;
        };

        if (body.size() > 4 && hitSelf() && !body.get(0).equals(head)) {
            dead = true;
            // The tail still advances on the fatal step, so a collision cannot
            // make the snake appear to gain an extra segment.
            body.remove(0);
        }
    }

    boolean canMove(char dir) {
        if (dir != 'U' && dir != 'D' && dir != 'L' && dir != 'R') {
            return false;
        }

        return !((direction == 'U' && dir == 'D')
                || (direction == 'D' && dir == 'U')
                || (direction == 'L' && dir == 'R')
                || (direction == 'R' && dir == 'L'));
    }

    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        } else if (body.get(bodyCount).getY() == body.get(bodyCount - 1).getY()) {
            return 270;
        } else {
            return 0;
        }
    }

    public boolean ateCoin(Coin coin) {
        Coin eatenCoin = ateAnyCoin(java.util.List.of(coin));
        return eatenCoin != null;
    }

    public Coin ateAnyCoin(java.util.List<Coin> coins) {
        for (Coin coin : coins) {
            if (head.equals(coin.getPos())) {
                return coin;
            }
        }
        if (!body.isEmpty()) {
            body.remove(0);
        }
        return null;
    }

    public boolean hitSelf() {
        return body.contains(head);
    }

    public ArrayList<Point> getBody() {
        return new ArrayList<>(body);
    }

    public Point getHead() {
        return new Point(head);
    }

    public Color getHeadColor() {
        return headColor;
    }

    public Color getBodyColor() {
        return bodyColor;
    }

    public boolean getDead() {
        return dead;
    }

    public void ignoreBorders(boolean ign) {
        ignoreBorders = ign;
    }

    public ImageView getImageViewHead() {
        loadImages();
        return imageHead;
    }

    public ImageView getImageViewBody() {
        loadImages();
        return imageBody;
    }

    public ImageView getImageViewTurn() {
        loadImages();
        return imageTurn;
    }

    public int getSnakeDir() {
        return snakeDir;
    }

    public ImageView getImageViewTail() {
        loadImages();
        return imageTail;
    }

    Image getSpriteSheet() {
        loadImages();
        return spriteSheet;
    }

    private void loadImages() {
        if (imageHead != null) {
            return;
        }

        spriteSheet = SnakeSpriteSheet.load();
        imageHead = SnakeSpriteSheet.createView(spriteSheet, SnakeSpriteSheet.HEAD);
        imageBody = SnakeSpriteSheet.createView(spriteSheet, SnakeSpriteSheet.BODY);
        imageTurn = SnakeSpriteSheet.createView(spriteSheet, SnakeSpriteSheet.TURN);
        imageTail = SnakeSpriteSheet.createView(spriteSheet, SnakeSpriteSheet.TAIL);
    }
}
