package com.comp2042;

final class ResumeCountdown {
    private static final int STARTING_NUMBER = 3;
    private int remaining;

    void start() {
        remaining = STARTING_NUMBER;
    }

    String currentNumber() {
        return remaining > 0 ? Integer.toString(remaining) : "";
    }

    String advance() {
        if (remaining > 0) {
            remaining--;
        }
        return currentNumber();
    }

    boolean isActive() {
        return remaining > 0;
    }
}
