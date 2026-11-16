package com.comp2042;

import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillInventoryTest {
    @Test
    void allThreeCardsUseTheRequestedPrices() {
        assertEquals(300, ShopSkill.RED_COIN_UPGRADE.cost());
        assertEquals(300, ShopSkill.GOLD_COIN_UPGRADE.cost());
        assertEquals(300, ShopSkill.LENGTH_DECREASE.cost());
    }

    @Test
    void purchasedCoinUpgradesAddTheirBonusAndStack() {
        Game game = new Game(20, 20, false, 'N');
        game.addPoints(900);
        Snake snake = new Snake(20, 20, false, new Obstacle(20, 20));
        SkillInventory inventory = new SkillInventory();

        assertTrue(inventory.purchase(ShopSkill.RED_COIN_UPGRADE, game, snake));
        assertTrue(inventory.purchase(ShopSkill.RED_COIN_UPGRADE, game, snake));
        assertTrue(inventory.purchase(ShopSkill.GOLD_COIN_UPGRADE, game, snake));

        assertEquals(4, inventory.bonusPointsFor(new Coin(new Point(0, 0))));
        assertEquals(5, inventory.bonusPointsFor(new GoldCoin(new Point(1, 0))));
        assertEquals(0, game.getScorePoints());
        assertEquals(2, inventory.count(ShopSkill.RED_COIN_UPGRADE));
    }

    @Test
    void fourthPurchaseIsBlockedUntilTheNextShopVisit() {
        Game game = new Game(20, 20, false, 'N');
        game.addPoints(1200);
        Snake snake = new Snake(20, 20, false, new Obstacle(20, 20));
        SkillInventory inventory = new SkillInventory();
        ShopSkill skill = ShopSkill.RED_COIN_UPGRADE;

        for (int i = 0; i < 3; i++) {
            assertTrue(inventory.purchase(skill, game, snake));
        }
        assertFalse(inventory.purchase(skill, game, snake));
        assertEquals(300, game.getScorePoints());
        assertEquals(3, inventory.purchasesThisVisit());

        inventory.startShopVisit();

        assertTrue(inventory.purchase(skill, game, snake));
        assertEquals(1, inventory.purchasesThisVisit());
        assertEquals(4, inventory.count(skill));
    }

    @Test
    void lengthCardRequiresFiveSegmentsAndRemovesExactlyFive() {
        Snake snake = new Snake(20, 20, false, new Obstacle(20, 20));
        Game game = new Game(20, 20, false, 'N');
        game.addPoints(300);
        SkillInventory inventory = new SkillInventory();

        assertFalse(inventory.canPurchase(ShopSkill.LENGTH_DECREASE, game.getScorePoints(), snake));
        assertFalse(inventory.purchase(ShopSkill.LENGTH_DECREASE, game, snake));
        assertEquals(300, game.getScorePoints());

        for (int i = 0; i < 4; i++) {
            snake.move('L');
            snake.ateAnyCoin(java.util.List.of(new Coin(snake.getHead())));
        }

        assertEquals(5, snake.getBody().size());
        assertTrue(inventory.purchase(ShopSkill.LENGTH_DECREASE, game, snake));
        assertEquals(0, snake.getBody().size());
        assertEquals(1, inventory.count(ShopSkill.LENGTH_DECREASE));
        assertEquals(0, game.getScorePoints());
    }

    @Test
    void unaffordablePurchaseDoesNotChangeCoinsOrOwnership() {
        Game game = new Game(20, 20, false, 'N');
        game.addPoints(299);
        SkillInventory inventory = new SkillInventory();
        Snake snake = new Snake(20, 20, false, new Obstacle(20, 20));

        assertFalse(inventory.purchase(ShopSkill.RED_COIN_UPGRADE, game, snake));
        assertEquals(299, game.getScorePoints());
        assertEquals(0, inventory.count(ShopSkill.RED_COIN_UPGRADE));
    }

    @Test
    void resetClearsOwnedSkills() {
        Game game = new Game(20, 20, false, 'N');
        game.addPoints(300);
        SkillInventory inventory = new SkillInventory();
        Snake snake = new Snake(20, 20, false, new Obstacle(20, 20));
        inventory.purchase(ShopSkill.RED_COIN_UPGRADE, game, snake);

        inventory.reset();

        assertEquals(0, inventory.count(ShopSkill.RED_COIN_UPGRADE));
        assertEquals(0, inventory.bonusPointsFor(new Coin(new Point(0, 0))));
    }
}
