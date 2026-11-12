package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopRefreshRulesTest {
    @Test
    void eachRefreshCostsTwoHundredCoinsAndOnlyThreeAreAllowedPerVisit() {
        ShopRefreshRules rules = new ShopRefreshRules();

        assertTrue(rules.canRefresh(600));
        assertTrue(rules.recordRefresh(600));
        assertTrue(rules.recordRefresh(400));
        assertTrue(rules.recordRefresh(200));
        assertFalse(rules.canRefresh(1000));
        assertFalse(rules.recordRefresh(1000));
        assertEquals(3, rules.refreshesUsed());
    }

    @Test
    void refreshIsUnavailableWhenBalanceIsBelowTwoHundred() {
        ShopRefreshRules rules = new ShopRefreshRules();

        assertFalse(rules.canRefresh(199));
        assertFalse(rules.recordRefresh(199));
        assertEquals(0, rules.refreshesUsed());
    }

    @Test
    void refreshLimitCanResetForANewShopVisit() {
        ShopRefreshRules rules = new ShopRefreshRules();
        rules.recordRefresh(200);

        rules.reset();

        assertTrue(rules.canRefresh(200));
        assertEquals(0, rules.refreshesUsed());
    }
}
