package com.comp2042;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class CoinSpawnRulesTest {
    @Test
    void spawnIntervalsRangeFromOnePointFiveToTwoSeconds() {
        assertEquals(1.5, CoinSpawnRules.intervalForRoll(0.0));
        assertEquals(1.75, CoinSpawnRules.intervalForRoll(0.5));
        assertTrue(CoinSpawnRules.intervalForRoll(0.999) < 2.0);
    }

    @Test
    void goldIsLessLikelyThanRed() {
        assertEquals(CoinSpawnRules.Type.GOLD, CoinSpawnRules.typeForRoll(0.0));
        assertEquals(CoinSpawnRules.Type.GOLD, CoinSpawnRules.typeForRoll(0.199));
        assertEquals(CoinSpawnRules.Type.RED, CoinSpawnRules.typeForRoll(0.2));
        assertEquals(CoinSpawnRules.Type.RED, CoinSpawnRules.typeForRoll(0.99));
    }

    @Test
    void onlySpawnsWhileBelowFiveActiveCoins() {
        assertTrue(CoinSpawnRules.hasRoom(4));
        assertFalse(CoinSpawnRules.hasRoom(5));
    }

    @Test
    void generatedCoinPositionAvoidsSnakeObstaclesAndExistingCoins() {
        Obstacle obstacle = new Obstacle(6, 6);
        Point obstaclePosition = new Point(0, 0);
        obstacle.getObstacles().add(obstaclePosition);
        Snake snake = new Snake(6, 6, false, obstacle);
        Point existingCoinPosition = new Point(1, 1);
        List<Coin> existingCoins = List.of(new Coin(existingCoinPosition));

        Point position = Coin.randomFreePosition(6, 6, snake, obstacle, existingCoins,
                new Random(19));

        assertNotNull(position);
        assertNotEquals(obstaclePosition, position);
        assertNotEquals(existingCoinPosition, position);
        assertNotEquals(snake.getHead(), position);
        assertFalse(snake.getBody().contains(position));
    }

    @Test
    void returnsNoPositionWhenTheBoardIsFull() {
        Obstacle obstacle = new Obstacle(3, 3);
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                obstacle.getObstacles().add(new Point(x, y));
            }
        }
        Snake snake = new Snake(3, 3, false, obstacle);

        assertEquals(null, Coin.randomFreePosition(3, 3, snake, obstacle, List.of(),
                new Random(1)));
    }
}
