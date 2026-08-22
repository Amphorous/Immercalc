package org.hoyo.immercalc.pullcalc.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GenshinPullCalcRequest {
    LocalDate startDate;

    // Supplied by the frontend - either entered manually or taken from a
    // prior call to the end-date endpoint.
    LocalDate endDate;

    int pity;
    int avgFourStarSgPerBanner;

    CurrencyType currencyType;
    int startCurrencyAmount;

    int startStarglitter;
    int welkinDaysLeft;

    // Spiral Abyss stars, Floors 9-12 in order.
    int[] abyssStars;

    // Total Imaginarium Theatre stars obtained (0-10).
    int imaginariumStars;

    StygianDifficulty stygianDifficulty;
}
