package com.comp2042;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Builds the initial shop choices and the three choices shown after a refresh. */
final class ShopOfferRules {
    private ShopOfferRules() {
    }

    static List<ShopSkill> initialOffers() {
        return List.of(ShopSkill.values());
    }

    static List<ShopSkill> reroll(Random random) {
        ShopSkill[] skills = ShopSkill.values();
        List<ShopSkill> offers = new ArrayList<>(skills.length);
        for (int i = 0; i < skills.length; i++) {
            offers.add(skills[random.nextInt(skills.length)]);
        }
        return List.copyOf(offers);
    }
}
