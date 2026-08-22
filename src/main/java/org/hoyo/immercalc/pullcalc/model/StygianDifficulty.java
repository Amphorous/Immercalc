package org.hoyo.immercalc.pullcalc.model;

// Stygian Onslaught is assumed to reset every patch, same as the source calculator.
public enum StygianDifficulty {
    NORMAL,
    ADVANCING,
    HARD,
    SKIP;

    public int rewardMultiplier() {
        return this == SKIP ? 0 : (ordinal() + 1) * 150;
    }
}
