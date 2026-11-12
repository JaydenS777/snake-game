package com.comp2042;

final class CoinSpawnRules {
    static final int MAX_ACTIVE_COINS = 5;
    static final double MIN_INTERVAL_SECONDS = 1.5;
    static final double MAX_INTERVAL_SECONDS = 2.0;
    static final double GOLD_PROBABILITY = 0.20;

    enum Type { RED, GOLD }

    private CoinSpawnRules() {
    }

    static double intervalForRoll(double roll) {
        validateRoll(roll);
        return MIN_INTERVAL_SECONDS + roll * (MAX_INTERVAL_SECONDS - MIN_INTERVAL_SECONDS);
    }

    static Type typeForRoll(double roll) {
        validateRoll(roll);
        return roll < GOLD_PROBABILITY ? Type.GOLD : Type.RED;
    }

    static boolean hasRoom(int activeCoins) {
        return activeCoins < MAX_ACTIVE_COINS;
    }

    private static void validateRoll(double roll) {
        if (!Double.isFinite(roll) || roll < 0.0 || roll >= 1.0) {
            throw new IllegalArgumentException("Roll must be in the range [0, 1).");
        }
    }
}
