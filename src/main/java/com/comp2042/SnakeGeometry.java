package com.comp2042;

import java.awt.Point;

/** Geometry helpers for snake segments on a board where opposite edges touch. */
final class SnakeGeometry {
    static final int UP = 0;
    static final int RIGHT = 90;
    static final int DOWN = 180;
    static final int LEFT = 270;

    private SnakeGeometry() {
    }

    static int directionBetween(Point from, Point to, int width, int height) {
        if (from.y == to.y) {
            if (to.x == from.x + 1 || (from.x == width - 1 && to.x == 0)) {
                return RIGHT;
            }
            if (to.x == from.x - 1 || (from.x == 0 && to.x == width - 1)) {
                return LEFT;
            }
        }
        if (from.x == to.x) {
            if (to.y == from.y + 1 || (from.y == height - 1 && to.y == 0)) {
                return DOWN;
            }
            if (to.y == from.y - 1 || (from.y == 0 && to.y == height - 1)) {
                return UP;
            }
        }
        throw new IllegalArgumentException("Snake segments must occupy neighboring board cells.");
    }

    /** Returns the rotation for the base corner sprite (top-to-left), or -1 for a straight segment. */
    static int turnRotation(int incomingDirection, int outgoingDirection) {
        if (incomingDirection == DOWN && outgoingDirection == LEFT
                || incomingDirection == RIGHT && outgoingDirection == UP) return 0;
        if (incomingDirection == DOWN && outgoingDirection == RIGHT
                || incomingDirection == LEFT && outgoingDirection == UP) return 90;
        if (incomingDirection == UP && outgoingDirection == RIGHT
                || incomingDirection == LEFT && outgoingDirection == DOWN) return 180;
        if (incomingDirection == UP && outgoingDirection == LEFT
                || incomingDirection == RIGHT && outgoingDirection == DOWN) return 270;
        return -1;
    }

    /** Rotation offset for a tail sprite whose unrotated tip points down. */
    static int tailRotation(int directionFromTailToHead) {
        int tailDirection = (directionFromTailToHead + 180) % 360;
        return (tailDirection - DOWN + 360) % 360;
    }
}
