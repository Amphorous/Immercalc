package org.hoyo.immercalc.pullcalc.service;

import org.hoyo.immercalc.pullcalc.InvalidPullCalcRequestException;
import org.hoyo.immercalc.pullcalc.model.CurrencyType;
import org.hoyo.immercalc.pullcalc.model.GenshinEndDateRequest;
import org.hoyo.immercalc.pullcalc.model.GenshinEndDateResponse;
import org.hoyo.immercalc.pullcalc.model.GenshinPullCalcRequest;
import org.hoyo.immercalc.pullcalc.model.GenshinPullCalcResponse;
import org.hoyo.immercalc.pullcalc.model.GenshinPullTotals;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/*
 *  Ported from the standalone V6 pull calculator (PullCalculator_6_v0)
 *  updated for version : 6.0
 *  updated on date : Sep 7 2025
 */
@Service
public class GenshinPullCalcService {

    // Patch 5.0 release date
    private static final LocalDate BASE_PATCH_DATE = LocalDate.of(2024, 8, 28);
    private static final int PATCH_LENGTH_DAYS = 42;

    public GenshinEndDateResponse calculateEndDate(GenshinEndDateRequest request) {
        LocalDate start = request.getStartDate();
        int daysFromStart = switch (request.getMode()) {
            case DAYS_FROM_NOW -> request.getValue();
            case PATCH_PHASE_1 -> daysTillNthPhaseOfPatch(start, 1, request.getValue());
            case PATCH_PHASE_2 -> daysTillNthPhaseOfPatch(start, 2, request.getValue());
        };

        GenshinEndDateResponse response = new GenshinEndDateResponse();
        response.setEndDate(start.plusDays(daysFromStart));
        return response;
    }

    public GenshinPullCalcResponse calculate(GenshinPullCalcRequest request) {
        if (request.getAbyssStars() == null || request.getAbyssStars().length != 4) {
            throw new InvalidPullCalcRequestException("abyssStars must contain exactly 4 values (Floors 9-12).");
        }

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();
        int totalDays = dateDifference(start, end);

        List<LocalDate> patchResetDates = patchResetDatesInRange(start, end);
        int patchResetCount = patchResetDates.size();

        int startPrimogems = request.getCurrencyType() == CurrencyType.FATE
                ? request.getStartCurrencyAmount() * 160
                : request.getStartCurrencyAmount();

        int monthlyResetCount = monthlyEventOccurrences(start, end, 1);
        int abyssCount = monthlyEventOccurrences(start, end, 15);

        int abyssTotal = abyssStarsTotal(request.getAbyssStars()) * abyssCount;
        int monthlyTotal = monthlyResetCount * 160 * 5;
        int imaginariumTotal = monthlyResetCount * imaginariumTotal(request.getImaginariumStars());
        int stygianTotal = monthlyResetCount * request.getStygianDifficulty().rewardMultiplier();

        int welkinDaysUsed = Math.min(request.getWelkinDaysLeft(), totalDays);
        int welkinDaysLeftFinal = request.getWelkinDaysLeft() - welkinDaysUsed;
        int dailyTotal = welkinDaysUsed * 150 + (totalDays - welkinDaysUsed) * 60;

        int patch = 600 * patchResetCount;
        int codes = 300 * patchResetCount;
        int trials = 80 * patchResetCount;

        int totalPulls = (startPrimogems + abyssTotal + imaginariumTotal + stygianTotal
                + monthlyTotal + dailyTotal + codes + patch + trials) / 160;

        int totalPullsAfterSgReuse = totalPulls + starglitterConversionPulls(
                totalPulls, request.getPity(), request.getStartStarglitter(), request.getAvgFourStarSgPerBanner());

        GenshinPullTotals totals = new GenshinPullTotals();
        totals.setDailyTotal(dailyTotal);
        totals.setAbyssTotal(abyssTotal);
        totals.setImaginariumTotal(imaginariumTotal);
        totals.setStygianTotal(stygianTotal);
        totals.setMonthlyTotal(monthlyTotal);
        totals.setCodes(codes);
        totals.setPatch(patch);
        totals.setTrials(trials);

        GenshinPullCalcResponse response = new GenshinPullCalcResponse();
        response.setPatchResetCount(patchResetCount);
        response.setPatchResetDates(patchResetDates);
        response.setTotals(totals);
        response.setWelkinDaysLeft(welkinDaysLeftFinal);
        response.setTotalDays(totalDays);
        response.setTotalPulls(totalPulls);
        response.setTotalPullsAfterSgReuse(totalPullsAfterSgReuse);
        return response;
    }

    private int dateDifference(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.DAYS.between(start, end);
    }

    // Counts how many times dayOfMonth occurs between start and end inclusive.
    private int monthlyEventOccurrences(LocalDate start, LocalDate end, int dayOfMonth) {
        int count = 0;
        LocalDate current = start.withDayOfMonth(Math.min(dayOfMonth, start.lengthOfMonth()));
        if (current.isBefore(start)) {
            current = current.plusMonths(1);
        }
        while (!current.isAfter(end)) {
            count++;
            current = current.plusMonths(1);
        }
        return count;
    }

    private List<LocalDate> patchResetDatesInRange(LocalDate start, LocalDate end) {
        LocalDate patchDate = alignPatchDateTo(start);

        List<LocalDate> patches = new ArrayList<>();
        while (!patchDate.isAfter(end)) {
            patches.add(patchDate);
            patchDate = patchDate.plusDays(PATCH_LENGTH_DAYS);
        }
        return patches;
    }

    private int daysTillNthPhaseOfPatch(LocalDate start, int phase, int plusPatches) {
        LocalDate patchDate = alignPatchDateTo(start).plusDays((long) PATCH_LENGTH_DAYS * plusPatches);

        int daysUntilReset = dateDifference(start, patchDate);
        if (phase == 1) {
            if (daysUntilReset <= 21) {
                throw new InvalidPullCalcRequestException(
                        "Phase 1 of that patch has already started or is too close - pick phase 2, or a later patch.");
            }
            return daysUntilReset - 21;
        }
        return daysUntilReset - 1;
    }

    // Walks BASE_PATCH_DATE forward/backward in 42-day steps until it lands on
    // the first patch reset on or after start.
    private LocalDate alignPatchDateTo(LocalDate start) {
        LocalDate patchDate = BASE_PATCH_DATE;
        while (patchDate.isAfter(start)) {
            patchDate = patchDate.minusDays(PATCH_LENGTH_DAYS);
        }
        while (patchDate.isBefore(start)) {
            patchDate = patchDate.plusDays(PATCH_LENGTH_DAYS);
        }
        return patchDate;
    }

    private int abyssStarsTotal(int[] abyssStars) {
        int total = 0;
        for (int stars : abyssStars) {
            switch (stars) {
                case 9:
                    total += 100;
                case 8:
                case 7:
                case 6:
                    total += 50;
                case 5:
                case 4:
                case 3:
                    total += 50;
                default:
                    break;
            }
        }
        return total;
    }

    private int imaginariumTotal(int stars) {
        int total = 0;
        switch (stars) {
            case 10: total += 120;
            case 9: total += 60;
            case 8: total += 120;
            case 7: total += 60;
            case 6: total += 100;
            case 5: total += 60;
            case 4: total += 60;
            case 3: total += 100;
            case 2: total += 60;
            case 1: total += 60;
            default: break;
        }
        return total;
    }

    // Returns extra pulls gained from converting leftover Starglitter (in
    // batches of 5) after spending `pulls` wishes down to a 4-star pity of 81.
    private int starglitterConversionPulls(int pulls, int pity, int startingStarglitter, int avgFourStarSg) {
        int returnedPulls = 0;
        int starglitter = startingStarglitter;
        while (starglitter > 5 || pulls > 0) {
            while (pity < 81 && pulls > 0) {
                pulls--;
                pity++;
                if (pity % 10 == 0) {
                    starglitter += avgFourStarSg;
                }
                if (pity == 80) {
                    starglitter += 10;
                    pity = 0;
                }
            }
            returnedPulls += starglitter / 5;
            pulls = starglitter / 5;
            starglitter %= 5;
        }
        return returnedPulls;
    }
}
