package com.comp2042;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SnakeTest {

    @Test
    void rainbowPowerUpHasItsOwnReachableRoll() {
        assertEquals(GameController.PowerUpCoinType.GOLD,
                GameController.powerUpCoinTypeForRoll(1, true));
        assertEquals(GameController.PowerUpCoinType.RAINBOW,
                GameController.powerUpCoinTypeForRoll(2, true));
    }

    @Test
    void reachingWinningScoreEndsTheGame() {
        Game game = new Game(20, 20, false, 'N');
        Snake snake = new Snake(20, 20, false, new Obstacle(20, 20));

        for (int i = 0; i < 20 * 20 - 2; i++) {
            game.incScore();
        }

        assertTrue(GameController.gameEnded(game, snake));
    }

    @Test
    void cannotReverseDirectionAndBodyStaysConnected() {
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);

        snake.move('U');
        Point positionAfterTurningUp = snake.getHead();
        snake.ateCoin(coinAt(0, 0, snake, obstacle));

        assertFalse(snake.canMove('D'));
        snake.move('D');

        assertEquals(positionAfterTurningUp, snake.getHead());
        assertFalse(snake.getDead());
        assertBodyConnected(snake);
    }

    @Test
    void hittingWallDoesNotLeaveAnExtraBodySegment() {
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);

        for (int i = 0; i < 10; i++) {
            snake.move('L');
            snake.ateCoin(coinAt(0, 0, snake, obstacle));
        }

        Point positionAtWall = snake.getHead();
        int lengthAtWall = snake.getBody().size();
        snake.move('L');

        assertTrue(snake.getDead());
        assertEquals(positionAtWall, snake.getHead());
        assertEquals(lengthAtWall, snake.getBody().size());
        assertBodyConnected(snake);
    }

    @Test
    void selfCollisionDoesNotIncreaseSnakeLength() {
        Obstacle obstacle = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacle);

        for (int i = 0; i < 4; i++) {
            snake.move('L');
            assertTrue(snake.ateCoin(coinAt(snake.getHead().x, snake.getHead().y, snake, obstacle)));
        }

        snake.move('U');
        snake.ateCoin(coinAt(0, 0, snake, obstacle));
        snake.move('R');
        snake.ateCoin(coinAt(0, 0, snake, obstacle));
        int lengthBeforeCollision = snake.getBody().size();

        snake.move('D');

        assertTrue(snake.getDead());
        assertEquals(lengthBeforeCollision, snake.getBody().size());
        assertBodyConnected(snake);
    }

    private static Coin coinAt(int x, int y, Snake snake, Obstacle obstacle) {
        Point position = new Point(x, y);
        return new Coin(20, 20, snake, obstacle) {
            @Override
            public Point getPos() {
                return new Point(position);
            }
        };
    }

    private static void assertBodyConnected(Snake snake) {
        ArrayList<Point> body = snake.getBody();
        for (int i = 1; i < body.size(); i++) {
            assertAdjacent(body.get(i - 1), body.get(i));
        }
        if (!body.isEmpty()) {
            assertAdjacent(body.get(body.size() - 1), snake.getHead());
        }
    }

    private static void assertAdjacent(Point first, Point second) {
        int distance = Math.abs(first.x - second.x) + Math.abs(first.y - second.y);
        assertEquals(1, distance, () -> first + " and " + second + " should be adjacent");
    }
}
