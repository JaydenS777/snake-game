package com.comp2042;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.List;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopOfferRulesTest {
    @Test
    void shopRerollsTwoSlotsBeforeUnlockAndThreeAfter() {
        var random = new Random(7);
        boolean sawRevive = false;
        boolean sawShopWithoutRevive = false;
        for (int i = 0; i < 24; i++) {
            var locked = ShopOfferRules.reroll(random, false);
            assertEquals(3, locked.size());
            assertEquals(3, new HashSet<>(locked).size());
            assertEquals(ShopSkill.BLUE_COIN, locked.getLast());
            sawRevive |= locked.contains(ShopSkill.REVIVE);
            sawShopWithoutRevive |= !locked.contains(ShopSkill.REVIVE);

            var unlocked = ShopOfferRules.reroll(random, true);
            assertEquals(3, unlocked.size());
            assertEquals(3, new HashSet<>(unlocked).size());
            assertFalse(unlocked.contains(ShopSkill.BLUE_COIN));
        }
        assertTrue(sawRevive);
        assertTrue(sawShopWithoutRevive);
    }

    @Test
    void purchasingBlueFillsOnlyItsOwnSlotWithAnAvailableCard() {
        var previous = List.of(ShopSkill.RED_COIN_UPGRADE, ShopSkill.REVIVE, ShopSkill.BLUE_COIN);
        var updated = ShopOfferRules.replaceBlueOffer(previous, new Random(7));
        assertEquals(3, updated.size());
        assertEquals(previous.subList(0, 2), updated.subList(0, 2));
        assertTrue(List.of(ShopSkill.GOLD_COIN_UPGRADE, ShopSkill.LENGTH_DECREASE).contains(updated.getLast()));
    }
}
