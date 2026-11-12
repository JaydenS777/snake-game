package com.comp2042;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ObstacleTest {

    @Test
    void generatedObstaclesStayOnBoardAndUseFreeCellsOnly() {
        Obstacle obstacle = new Obstacle(4, 4);
        Snake snake = new Snake(4, 4, false, obstacle);
        Coin coin = new Coin(4, 4, snake, obstacle) {
            @Override
            public Point getPos() {
                return new Point(0, 0);
            }
        };

        obstacle.newObstacles(snake, coin, 500);

        assertEquals(13, obstacle.getObstacles().size());
        assertEquals(obstacle.getObstacles().size(), new HashSet<>(obstacle.getObstacles()).size());
        for (Point position : obstacle.getObstacles()) {
            assertTrue(position.x >= 0 && position.x < 4);
            assertTrue(position.y >= 0 && position.y < 4);
            assertNotEquals(coin.getPos(), position);
            assertNotEquals(snake.getHead(), position);
            assertFalse(snake.getBody().contains(position));
        }

        obstacle.newObstacles(snake, coin, 1);
        assertEquals(13, obstacle.getObstacles().size());
    }
}
