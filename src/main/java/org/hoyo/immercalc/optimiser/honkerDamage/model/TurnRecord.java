package org.hoyo.immercalc.optimiser.honkerDamage.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// One record of a turn taken during a rotation run - either a character's
// turn, or a cycle-boundary marker (cycleMarker=true, only cycleNumber is
// set). Two different field subsets matter per case, so construction goes
// through the named factories below rather than one all-args constructor
// that would force passing irrelevant nulls at every call site.
//
// A single polled turn can produce more than one of these sharing the same
// turnNumber - e.g. Sunday's turn triggering an assist attack actually
// performed by Himkin without taking the turn herself - so avatarId marks
// who this particular record is about, not necessarily the queue's turn
// owner.
@Data
public class TurnRecord {
    private int turnNumber;

    private boolean cycleMarker;
    private int cycleNumber;

    private String avatarId;
    private Double speedStat;
    private Double actionValue;
    private Double actionGauge;

    // Snapshot of this record's member's buffs at the moment the record was
    // created - copied rather than referenced, since turnStartBuffStep/
    // turnEndBuffStep keep mutating the live BuffInstance objects afterward.
    // Paired with member.getStats() (never mutated), this is enough to run
    // dmgCalc against this exact turn later without re-simulating.
    private List<BuffInstance> buffInstances = new ArrayList<>();

    // Set only when something notable happened this turn - printed as a
    // follow-up line under the turn's row.
    private String actionSummary;

    public static TurnRecord forCharacterTurn(int turnNumber, RotationMember member, String actionSummary) {
        TurnRecord record = new TurnRecord();
        record.setTurnNumber(turnNumber);
        record.setAvatarId(member.getAvatarId());
        record.setSpeedStat(member.getLastEffectiveSpeed());
        record.setActionValue(member.getActionValue());
        record.setActionGauge(member.getActionGauge());
        record.setActionSummary(actionSummary);

        for (BuffInstance buffInstance : member.getBuffInstances()) {
            record.getBuffInstances().add(buffInstance.copy());
        }
        return record;
    }

    public static TurnRecord forCycleMarker(int turnNumber, int cycleNumber) {
        TurnRecord record = new TurnRecord();
        record.setTurnNumber(turnNumber);
        record.setCycleMarker(true);
        record.setCycleNumber(cycleNumber);
        return record;
    }
}
