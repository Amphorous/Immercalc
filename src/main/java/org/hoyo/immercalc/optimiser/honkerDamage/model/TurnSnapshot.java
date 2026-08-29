package org.hoyo.immercalc.optimiser.honkerDamage.model;

import lombok.Data;

import java.util.Map;

@Data
// this is the final action taken, written in stone. This obj is written to a list only once an action taken by the unit is finalised, and is only used to eval damage dealt
// as such, info such as SP and Energy isnt needed to be maintained here
public class TurnSnapshot {
    private Map<String, Double> stats; // stats for this instant in time
    private Map<String, Double> buffs; // things like skill dmg% inc etc, not stat buffs
    private String action; // things like call starblazer will instantly apply buff of 100% action advance to specific user
    private String avatarId;
}
