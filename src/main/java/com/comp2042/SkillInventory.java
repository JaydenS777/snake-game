package com.comp2042;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Tracks purchased skills for the current game. */
final class SkillInventory {
    static final int MAX_PURCHASES_PER_VISIT = 3;
    private final EnumMap<ShopSkill, Integer> counts = new EnumMap<>(ShopSkill.class);
    private int purchasesThisVisit;

    SkillInventory() {
        reset();
    }

    boolean consumeRevive() {
        int remaining = count(ShopSkill.REVIVE);
        if (remaining == 0) {
            return false;
        }
        counts.put(ShopSkill.REVIVE, remaining - 1);
        return true;
    }

    void startShopVisit() {
        purchasesThisVisit = 0;
    }

    int purchasesThisVisit() {
        return purchasesThisVisit;
    }

    boolean isBlueCoinUnlocked() {
        return count(ShopSkill.BLUE_COIN) > 0;
    }

    boolean canPurchase(ShopSkill skill, int balance, Snake snake) {
        return purchasesThisVisit < MAX_PURCHASES_PER_VISIT && balance >= skill.cost()
                && (skill != ShopSkill.BLUE_COIN || !isBlueCoinUnlocked())
                && (skill != ShopSkill.LENGTH_DECREASE || snake.getBody().size() >= 5);
    }

    boolean purchase(ShopSkill skill, Game game, Snake snake) {
        if (!canPurchase(skill, game.getScorePoints(), snake) || !game.spendPoints(skill.cost())) {
            return false;
        }

        if (skill == ShopSkill.LENGTH_DECREASE && snake.removeTailSegments(5) != 5) {
            game.addPoints(skill.cost());
            return false;
        }

        counts.merge(skill, 1, Integer::sum);
        purchasesThisVisit++;
        return true;
    }

    int count(ShopSkill skill) {
        return counts.getOrDefault(skill, 0);
    }

    int bonusPointsFor(Coin coin) {
        if (coin instanceof GoldCoin) {
            return count(ShopSkill.GOLD_COIN_UPGRADE) * 5;
        }
        if (coin.getClass() == Coin.class) {
            return count(ShopSkill.RED_COIN_UPGRADE) * 2;
        }
        return 0;
    }

    Map<ShopSkill, Integer> counts() {
        return Collections.unmodifiableMap(new EnumMap<>(counts));
    }

    void reset() {
        counts.clear();
        counts.put(ShopSkill.REVIVE, 1);
        startShopVisit();
    }
}
