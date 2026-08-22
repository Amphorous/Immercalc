package org.hoyo.immercalc.pullcalc.model;

// Mirrors the non-manual end-date choices from the original calculator -
// manual entry needs no calculation, so the frontend never calls the
// end-date endpoint for that option.
public enum EndDateMode {
    DAYS_FROM_NOW,
    PATCH_PHASE_1,
    PATCH_PHASE_2
}
