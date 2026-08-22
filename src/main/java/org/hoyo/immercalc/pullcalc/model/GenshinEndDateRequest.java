package org.hoyo.immercalc.pullcalc.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GenshinEndDateRequest {
    LocalDate startDate;
    EndDateMode mode;

    // Meaning depends on mode: day count for DAYS_FROM_NOW, or "plusPatches"
    // (0 = the current patch, +1 for each patch after that) for the two
    // PATCH_PHASE_* modes.
    int value;
}
