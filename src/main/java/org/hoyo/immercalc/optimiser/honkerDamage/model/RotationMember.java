package org.hoyo.immercalc.optimiser.honkerDamage.model;

import lombok.Data;

import java.util.List;

@Data
public class RotationMember {
    private String avatarId; // numeric id of aan avatar, string name of a summon
    private TeamMemberDTO teamMemberDTO;
    private List<BuffInstance> buffInstances; // handled at init

    private double maxEnergy; // energy cost for one ult
    private double currentEnergy; // how much energy is in the bar // energy is 0.5*maxEnergy in MoC, we calc by MoC standard

    private int altMaxEnergy; // feixiao/phainon case where an alt energy source is maintained
    private int altCurrentEnergy; // counter for alt energy

    private double maxEnergyCapacity; // max energy and alt max energy are the energy needed to cause one ult, this attribute holds the actual max energy possible to be held at once

    private Boolean usesAltEnergy; // bool to tell if a unit uses alt energy

    private Double actionGauge = 10000.0; // av = ag/spd
    private Double actionValue; // TODO we use AG for turn order calc, AV is ONLY used for AG depletion calc
    // when AV is depleted due to the turn completion of another unit
    // AG = AG - AV_dep * spd
    private double lastEffectiveSpeed; // post-buff speed RotationBuilder used to compute actionValue - kept for debug/verification, not read back by init() itself
    private Double finalActionAdvance; // net (actionAdvance - actionDelay) RotationBuilder used to shift actionGauge - same reason

    private int teamSlot = -1;

    //if cycle
    private Boolean isCycleIndicator = false;
    private int cycleNumber = -1;

    //if summon (this is hella bad, but idk the correct approach yet)
    String summonerId;
    String ownerId;
    boolean allowTeamBuffs = true;
}
