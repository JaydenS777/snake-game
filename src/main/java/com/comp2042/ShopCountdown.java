package com.comp2042;

final class ShopCountdown {
    static final int DURATION_SECONDS = 60;

    private int remainingSeconds = DURATION_SECONDS;

    int getRemainingSeconds() {
        return remainingSeconds;
    }

    int tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
        }
        return remainingSeconds;
    }

    boolean isFinished() {
        return remainingSeconds == 0;
    }

    void reset() {
        remainingSeconds = DURATION_SECONDS;
    }
}
