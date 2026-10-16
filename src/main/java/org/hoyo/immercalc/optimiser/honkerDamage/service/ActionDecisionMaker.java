package org.hoyo.immercalc.optimiser.honkerDamage.service;

import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TurnRecord;

import java.util.List;
import java.util.PriorityQueue;

// Decides what turnOwner does on their turn - called at the //process point
// in HimkinNova's rotation loop. Swappable: debug wiring passes a
// do-nothing-except-one-scripted-buff implementation for verifying the
// engine's math; real business logic will implement this with actual
// autobattle decisions later.
public interface ActionDecisionMaker {
    // Returns the TurnRecords produced by turnOwner's turn, all sharing this
    // same turnNumber - normally just turnOwner's own record, but a
    // follow-up action triggered without taking the turn (e.g. Himkin's
    // assist off Sunday's action) can add more, each built against its own
    // member so its avatarId/stats/buff snapshot are correct.
    List<TurnRecord> actionDecision(PriorityQueue<RotationMember> rotationQueue, RotationMember turnOwner, Team team, int turnNumber);
}
