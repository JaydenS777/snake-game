package com.comp2042;

/** Limits refresh spending for one shop visit. */
final class ShopRefreshRules {
    static final int COST = 200;
    static final int MAX_REFRESHES = 3;

    private int refreshesUsed;

    boolean canRefresh(int balance) {
        return refreshesUsed < MAX_REFRESHES && balance >= COST;
    }

    boolean recordRefresh(int balance) {
        if (!canRefresh(balance)) {
            return false;
        }
        refreshesUsed++;
        return true;
    }

    int refreshesUsed() {
        return refreshesUsed;
    }

    void reset() {
        refreshesUsed = 0;
    }
}
