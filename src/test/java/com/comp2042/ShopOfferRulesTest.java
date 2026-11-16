package com.comp2042;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShopOfferRulesTest {
    @Test
    void initialShopShowsEachOfTheThreeSkillsOnce() {
        assertEquals(java.util.List.of(ShopSkill.values()), ShopOfferRules.initialOffers());
    }

    @Test
    void refreshAlwaysReturnsThreeAvailableSkillCards() {
        var offers = ShopOfferRules.reroll(new Random(7));

        assertEquals(3, offers.size());
        offers.forEach(skill -> assertNotNull(skill));
    }
}
