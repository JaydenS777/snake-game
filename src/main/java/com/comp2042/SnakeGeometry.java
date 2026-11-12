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

    /** Returns the rotation for the existing corner sprite, or -1 for a straight segment. */
    static int turnRotation(int incomingDirection, int outgoingDirection) {
        if (incomingDirection == outgoingDirection) {
            return -1;
        }

        if (incomingDirection == LEFT && outgoingDirection == UP
                || incomingDirection == DOWN && outgoingDirection == RIGHT) {
            return 180;
        }
        if (incomingDirection == LEFT && outgoingDirection == DOWN
                || incomingDirection == UP && outgoingDirection == RIGHT) {
            return 270;
        }
        if (incomingDirection == RIGHT && outgoingDirection == UP
                || incomingDirection == DOWN && outgoingDirection == LEFT) {
            return 90;
        }
        if (incomingDirection == RIGHT && outgoingDirection == DOWN
                || incomingDirection == UP && outgoingDirection == LEFT) {
            return 0;
        }
        return -1;
    }
}
