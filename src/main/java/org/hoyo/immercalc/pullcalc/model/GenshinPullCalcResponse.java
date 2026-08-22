package org.hoyo.immercalc.pullcalc.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class GenshinPullCalcResponse {
    int patchResetCount;
    List<LocalDate> patchResetDates;

    GenshinPullTotals totals;

    int welkinDaysLeft;
    int totalDays;

    int totalPulls;
    int totalPullsAfterSgReuse;
}
