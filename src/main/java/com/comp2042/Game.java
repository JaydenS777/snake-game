package com.comp2042;

import javafx.scene.paint.Color;

public class Game {
    private Color color = Color.web("feffe4");
    private int width;
    private int height;
    private int score;
    private int maxScore;
    private double multiplier;
    private int scorePoints;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;

    public Game(int x, int y, boolean powerUp, char difficulty) {
        this(x, y, powerUp, difficulty, y > 50 ? 10 : 20);
    }

    public Game(int x, int y, boolean powerUp, char difficulty, int cellSize) {
        if (difficulty == 'E') {
            speed = 8.5;
            noBorder = true;
        } else if (difficulty == 'N') {
            speed = 8.5;
            noBorder = false;
        } else if (difficulty == 'H') {
            speed = 13;
            noBorder = false;
        }
        this.powerUp = powerUp;
        width = x;
        height = y;
        maxScore = x * y - 2;
        this.powerUp = powerUp;
        multiplier = cellSize;
    }

    public boolean gameWon() {
        return score == maxScore;
    }

    public void incScore() {
        score++;
    }

    public void decMaxScore(int num) {
        this.maxScore -= (num - 1);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resetScore() {
        score = 0;
        scorePoints = 0;
    }

    public Color getColor() {
        return color;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void addPoints(int n) {
        scorePoints += n;
    }

    public boolean spendPoints(int amount) {
        if (amount < 0 || scorePoints < amount) {
            return false;
        }
        scorePoints -= amount;
        return true;
    }

    public int getScorePoints() {
        return scorePoints;
    }

    public boolean isPowerUp() {
        return powerUp;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isNoBorder() {
        return noBorder;
    }
}
