package com.comp2042;

import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnakeGeometryTest {
    @Test
    void tailDirectionAcrossLeftEdgeIsLeft() {
        assertEquals(SnakeGeometry.LEFT,
                SnakeGeometry.directionBetween(new Point(0, 5), new Point(19, 5), 20, 20));
    }

    @Test
    void tailDirectionAcrossRightEdgeIsRight() {
        assertEquals(SnakeGeometry.RIGHT,
                SnakeGeometry.directionBetween(new Point(19, 5), new Point(0, 5), 20, 20));
    }

    @Test
    void tailDirectionAcrossVerticalEdgesIsWrapped() {
        assertEquals(SnakeGeometry.UP,
                SnakeGeometry.directionBetween(new Point(5, 0), new Point(5, 19), 20, 20));
        assertEquals(SnakeGeometry.DOWN,
                SnakeGeometry.directionBetween(new Point(5, 19), new Point(5, 0), 20, 20));
    }

    @Test
    void turnSpriteRotationMatchesAllFourCorners() {
        assertEquals(0, SnakeGeometry.turnRotation(SnakeGeometry.DOWN, SnakeGeometry.LEFT));
        assertEquals(0, SnakeGeometry.turnRotation(SnakeGeometry.RIGHT, SnakeGeometry.UP));
        assertEquals(90, SnakeGeometry.turnRotation(SnakeGeometry.DOWN, SnakeGeometry.RIGHT));
        assertEquals(90, SnakeGeometry.turnRotation(SnakeGeometry.LEFT, SnakeGeometry.UP));
        assertEquals(180, SnakeGeometry.turnRotation(SnakeGeometry.UP, SnakeGeometry.RIGHT));
        assertEquals(180, SnakeGeometry.turnRotation(SnakeGeometry.LEFT, SnakeGeometry.DOWN));
        assertEquals(270, SnakeGeometry.turnRotation(SnakeGeometry.UP, SnakeGeometry.LEFT));
        assertEquals(270, SnakeGeometry.turnRotation(SnakeGeometry.RIGHT, SnakeGeometry.DOWN));
        assertEquals(-1, SnakeGeometry.turnRotation(SnakeGeometry.LEFT, SnakeGeometry.LEFT));
    }

    @Test
    void tailPointsAwayFromTheSnakeBodyInAllDirections() {
        assertEquals(0, SnakeGeometry.tailRotation(SnakeGeometry.UP));
        assertEquals(90, SnakeGeometry.tailRotation(SnakeGeometry.RIGHT));
        assertEquals(180, SnakeGeometry.tailRotation(SnakeGeometry.DOWN));
        assertEquals(270, SnakeGeometry.tailRotation(SnakeGeometry.LEFT));
    }
}
