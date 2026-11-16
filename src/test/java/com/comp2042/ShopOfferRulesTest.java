package com.comp2042;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ShopOfferRulesTest {
    @Test
    void initialShopShowsBlueWithTheThreeStandardSkillsUntilUnlocked() {
        assertEquals(List.of(ShopSkill.values()), ShopOfferRules.initialOffers(false));
        assertEquals(List.of(ShopSkill.RED_COIN_UPGRADE, ShopSkill.GOLD_COIN_UPGRADE,
                ShopSkill.LENGTH_DECREASE, ShopSkill.REVIVE), ShopOfferRules.initialOffers(true));
    }

    @Test
    void refreshKeepsBlueInTheSameSlotUntilPurchased() {
        var locked = ShopOfferRules.reroll(new Random(7), false);
        var unlocked = ShopOfferRules.reroll(new Random(7), true);

        assertEquals(5, locked.size());
        assertEquals(ShopSkill.BLUE_COIN, locked.get(3));
        assertEquals(unlocked.subList(0, 3), locked.subList(0, 3));
        assertEquals(ShopSkill.REVIVE, locked.getLast());
        assertEquals(ShopSkill.REVIVE, unlocked.getLast());
        assertFalse(unlocked.contains(ShopSkill.BLUE_COIN));
    }
}
