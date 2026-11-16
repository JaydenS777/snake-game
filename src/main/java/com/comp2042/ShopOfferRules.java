package com.comp2042;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Picks three offers and reserves one slot for the unpurchased blue coin unlock. */
final class ShopOfferRules {
    static final int SLOT_COUNT = 3;
    private static final List<ShopSkill> STANDARD_SKILLS = List.of(
            ShopSkill.RED_COIN_UPGRADE, ShopSkill.GOLD_COIN_UPGRADE, ShopSkill.LENGTH_DECREASE, ShopSkill.REVIVE);

    private ShopOfferRules() {
    }

    static List<ShopSkill> reroll(Random random, boolean blueUnlocked) {
        var available = new ArrayList<>(STANDARD_SKILLS);
        var offers = new ArrayList<ShopSkill>(SLOT_COUNT);
        int randomSlots = blueUnlocked ? SLOT_COUNT : SLOT_COUNT - 1;
        for (int i = 0; i < randomSlots; i++) {
            offers.add(available.remove(random.nextInt(available.size())));
        }
        if (!blueUnlocked) {
            offers.add(ShopSkill.BLUE_COIN);
        }
        return List.copyOf(offers);
    }

    static List<ShopSkill> replaceBlueOffer(List<ShopSkill> offers, Random random) {
        var available = new ArrayList<>(STANDARD_SKILLS);
        available.removeAll(offers);
        var updated = new ArrayList<>(offers);
        updated.set(SLOT_COUNT - 1, available.get(random.nextInt(available.size())));
        return List.copyOf(updated);
    }
}
