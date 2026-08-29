package org.hoyo.immercalc.optimiser.honkerDamage.service;

import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TurnRecord;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

// Shared by every CharacterDmgCalculator - builds a team, runs the turn
// order simulation to completion, and hands back the TurnRecords it
// produced. Previously duplicated per-character (HimkinNova's real dmgCalc
// and its debug harness carried near-identical copies of this loop). Will
// also house the damage formula that evaluates a TurnRecord, once that's
// written.
@Service
public class SimulationEngine {

    private final RotationBuilder rotationBuilder;
    private final TeamGenerator teamGeneratorService;

    public SimulationEngine(RotationBuilder rotationBuilder, TeamGenerator teamGeneratorService) {
        this.rotationBuilder = rotationBuilder;
        this.teamGeneratorService = teamGeneratorService;
    }

    public List<TurnRecord> simulationEngine(TeamMemberDTO teamMemberDTO, String teamName, List<BuffInstance> initialBuffs,
                                              ActionDecisionMaker actionDecisionMaker, int rotationCycles) {
        Team team = teamGeneratorService.teamGenerator(teamMemberDTO, teamName);
        RotationMember cycle = teamGeneratorService.getSeedCycle();

        List<TurnRecord> turnRecords = new ArrayList<>();
        int turnNumber = 1;

        PriorityQueue<RotationMember> rotationQueue = rotationBuilder.init(team, initialBuffs, cycle);
        while (!rotationQueue.isEmpty()) {
            RotationMember turnOwner = rotationQueue.poll();
            Double consumedAV = turnOwner.getActionValue();

            if (turnOwner.getIsCycleIndicator()) {
                turnRecords.add(TurnRecord.forCycleMarker(turnNumber, turnOwner.getCycleNumber()));
                if (turnOwner.getCycleNumber() == rotationCycles) break;
                cycle.setCycleNumber(cycle.getCycleNumber() + 1);
            } else {
                rotationBuilder.turnStartBuffStep(team.getTeamMembers(), turnOwner.getAvatarId());
                List<TurnRecord> actionRecords = actionDecisionMaker.actionDecision(turnOwner, team, turnNumber);
                rotationBuilder.turnEndBuffStep(team.getTeamMembers(), turnOwner.getAvatarId());

                turnRecords.addAll(actionRecords);
            }
            rotationBuilder.recalculateTurnOrder(team.getTeamMembers(), rotationQueue, cycle, turnOwner.getAvatarId(), consumedAV);
            turnNumber++;
        }

        return turnRecords;
    }
}
