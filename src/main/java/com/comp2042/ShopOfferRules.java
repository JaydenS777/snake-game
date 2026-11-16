package com.comp2042;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Rerolls the ordinary cards and keeps the special offers available. */
final class ShopOfferRules {
    private static final List<ShopSkill> STANDARD_SKILLS = List.of(
            ShopSkill.RED_COIN_UPGRADE, ShopSkill.GOLD_COIN_UPGRADE, ShopSkill.LENGTH_DECREASE);

    private ShopOfferRules() {
    }

    static List<ShopSkill> initialOffers(boolean blueUnlocked) {
        return includeBlueCard(STANDARD_SKILLS, blueUnlocked);
    }

    static List<ShopSkill> reroll(Random random, boolean blueUnlocked) {
        List<ShopSkill> offers = new ArrayList<>();
        for (int i = 0; i < STANDARD_SKILLS.size(); i++) {
            offers.add(STANDARD_SKILLS.get(random.nextInt(STANDARD_SKILLS.size())));
        }
        return includeBlueCard(offers, blueUnlocked);
    }

    private static List<ShopSkill> includeBlueCard(List<ShopSkill> offers, boolean blueUnlocked) {
        List<ShopSkill> result = new ArrayList<>(offers);
        if (!blueUnlocked) {
            result.add(ShopSkill.BLUE_COIN);
        }
        result.add(ShopSkill.REVIVE);
        return List.copyOf(result);
    }
}
