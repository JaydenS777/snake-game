package com.comp2042;

import java.awt.Point;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RevivalTest {
    @Test
    void startsWithOneReviveCanBuyMoreAndConsumesOneAtATime() {
        var inventory = new SkillInventory();
        var game = new Game(30, 20, false, 'N');
        var snake = new Snake(30, 20, false, new Obstacle(30, 20));
        assertEquals(1, inventory.count(ShopSkill.REVIVE));
        game.addPoints(800);
        assertTrue(inventory.purchase(ShopSkill.REVIVE, game, snake));
        assertEquals(0, game.getScorePoints());
        assertEquals(2, inventory.count(ShopSkill.REVIVE));
        assertTrue(inventory.consumeRevive());
        assertTrue(inventory.consumeRevive());
        assertFalse(inventory.consumeRevive());
        inventory.reset();
        assertEquals(1, inventory.count(ShopSkill.REVIVE));
    }

    @Test
    void reviveRetainsSixTrailingSegmentsOfAnElevenSegmentSnake() {
        var obstacle = new Obstacle(30, 20);
        var snake = new Snake(30, 20, false, obstacle);
        for (int i = 0; i < 9; i++) {
            grow(snake, 'L');
        }
        List<Point> previousBody = snake.getBody();
        Point blocked = snake.getHead();
        blocked.x--;
        obstacle.getObstacles().add(blocked);
        snake.move('L');
        assertTrue(snake.getDead());

        assertEquals('L', snake.reviveFromTailHalf());
        assertFalse(snake.getDead());
        assertEquals(previousBody.get(5), snake.getHead());
        assertEquals(previousBody.subList(0, 5), snake.getBody());
    }

    @Test
    void invincibilityAllowsCrossingOwnBodyWithoutChangingItsPath() {
        var snake = new Snake(30, 20, false, new Obstacle(30, 20));
        grow(snake, 'L');
        grow(snake, 'L');
        grow(snake, 'D');
        grow(snake, 'R');
        snake.enterInvincibility();
        List<Point> bodyBeforeCrossing = snake.getBody();
        Point previousHead = snake.getHead();
        snake.move('U');
        assertFalse(snake.getDead());
        assertTrue(snake.hitSelf());
        assertEquals(bodyBeforeCrossing, snake.getBody().subList(0, bodyBeforeCrossing.size()));
        assertEquals(previousHead, snake.getBody().getLast());
        snake.exitInvincibility();
        assertTrue(snake.getDead());
    }

    @Test
    void invincibilityExpiresSafelyIfSnakeAlreadyClearedObstacle() {
        var obstacle = new Obstacle(30, 20);
        var snake = new Snake(30, 20, false, obstacle);
        Point blocked = snake.getHead();
        blocked.x--;
        obstacle.getObstacles().add(blocked);
        snake.enterInvincibility();
        snake.move('L');
        assertEquals(blocked, snake.getHead());
        assertFalse(snake.getDead());
        snake.ateAnyCoin(List.of());
        snake.move('L');
        snake.ateAnyCoin(List.of());
        snake.exitInvincibility();
        assertFalse(snake.getDead());
    }

    private void grow(Snake snake, char direction) {
        snake.move(direction);
        snake.ateAnyCoin(List.of(new Coin(snake.getHead())));
    }
}
