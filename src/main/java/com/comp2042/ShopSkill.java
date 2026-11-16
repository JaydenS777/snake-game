package com.comp2042;

/** The skills currently offered by the shop. */
enum ShopSkill {
    RED_COIN_UPGRADE("Red Coin Upgrade", "Gain an extra +2 points for each Red Coin.",
            300, "red-coin-upgrade-card.png"),
    GOLD_COIN_UPGRADE("Gold Coin Upgrade", "Gain an extra +5 points for each Gold Coin.",
            300, "gold-coin-upgrade-card.png"),
    LENGTH_DECREASE("Length Decrease", "Decrease the snake's length by 5 segments.",
            300, "length-decrease-card.png"),
    BLUE_COIN("Blue Coin", "Unlock blue coins worth 10 coins without growing.", 150, "blue-coin-card.png"),
    REVIVE("Revive", "Revive once after death.", 800, "revive-card.png");

    private final String title;
    private final String description;
    private final int cost;
    private final String imageResource;

    ShopSkill(String title, String description, int cost, String imageResource) {
        this.title = title;
        this.description = description;
        this.cost = cost;
        this.imageResource = imageResource;
    }

    String title() {
        return title;
    }

    String description() {
        return description;
    }

    int cost() {
        return cost;
    }

    String imageResource() {
        return imageResource;
    }

}
