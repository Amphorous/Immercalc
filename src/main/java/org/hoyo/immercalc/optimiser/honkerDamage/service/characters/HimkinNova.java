package org.hoyo.immercalc.optimiser.honkerDamage.service.characters;

import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TurnRecord;
import org.hoyo.immercalc.optimiser.honkerDamage.debug.RotationRunResult;
import org.hoyo.immercalc.optimiser.honkerDamage.model.characterActionModels.HimkinNova.FastPremiumVerdictV0.Greg;
import org.hoyo.immercalc.optimiser.honkerDamage.service.ActionDecisionMaker;
import org.hoyo.immercalc.optimiser.honkerDamage.service.CharacterDmgCalculator;
import org.hoyo.immercalc.optimiser.honkerDamage.service.RotationBuilder;
import org.hoyo.immercalc.optimiser.honkerDamage.service.SimulationEngine;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class HimkinNova implements CharacterDmgCalculator, ActionDecisionMaker {

    public static final String AVATAR_ID = "1510";
    public static final String[] LEADERBOARD_NAMES = new String[]{"HimkinSunnyWeltGreg"};

    private final RotationBuilder rotationBuilder;
    private final SimulationEngine simulationEngine;
    private final Greg greg;

    public HimkinNova(RotationBuilder rotationBuilder, SimulationEngine simulationEngine, Greg greg) {
        this.rotationBuilder = rotationBuilder;
        this.simulationEngine = simulationEngine;
        this.greg = greg;
    }

    @Override
    public String getAvatarId() {
        return AVATAR_ID;
    }

    @Override
    public Double dmgCalc(TeamMemberDTO teamMemberDTO, Integer rotationCycles) {
        Map<String, List<TurnRecord>> turnRecordsByTeam = new HashMap<>();

        // we might have multiple leaderboards later, the leaderboard names must be appropriately given to reflect exact team states
        for(String teamName: LEADERBOARD_NAMES){
            List<BuffInstance> initialBuffs = getInitialBuffs(teamName);
            List<TurnRecord> turnRecords = simulationEngine.simulationEngine(teamMemberDTO, teamName, initialBuffs, this, rotationCycles);
            turnRecordsByTeam.put(teamName, turnRecords);
        }
        return 0.0;
    }

    @Override
    public List<TurnRecord> actionDecision(PriorityQueue<RotationMember> rotationQueue, RotationMember turnOwner, Team team, int turnNumber) {
        switch (turnOwner.getAvatarId()) {
            case AVATAR_ID -> {
                // himkin
            }
            case "1313" -> {
                // sunny
            }
            case "1004" -> {
                // welt
            }
            case "1414" -> {
                // greg
                if(turnOwner.getCurrentEnergy() == turnOwner.getMaxEnergy()){
                    greg.ult(turnOwner, team, turnNumber);
                }
                if(team.getSkillPoints() > 3){
                    greg.skill(turnOwner, rotationQueue, team, AVATAR_ID, turnNumber); // TODO add this to the turn order list
                } else {
                    greg.basic(turnOwner, team, turnNumber);
                }
                if(turnOwner.getCurrentEnergy() == turnOwner.getMaxEnergy()){
                    greg.ult(turnOwner, team, turnNumber);
                }
            }
            case "soulDragon" -> {
                // check for
            }
        }
        return List.of();
    }

    @Override
    public List<BuffInstance> getInitialBuffs(String teamName) {
        switch (teamName) {
            case "HimkinSunnyWeltGreg":{
                List<BuffInstance> initialBuffs = new ArrayList<>();
                BuffInstance buff;
                // welt 40% action advance
                buff = new BuffInstance();
                buff.setBuffName("Sprightly Vonwacq AA");
                buff.setBuffKey("ActionAdvance");
                buff.setBuffValue(0.4);
                buff.setBuffLifetime(1);
                buff.setCountsDownAtTurnEnd(false);
                buff.setBuffOwnerAvatarId("1004");
                buff.setBuffRecipientAvatarId("1004");
                buff.setConsumeOnApplication(true);
                buff.setUsesLeft(1);
                initialBuffs.add(buff);

                // greg 40% action advance
                buff = new BuffInstance();
                buff.setBuffName("Greg Core Passive AA");
                buff.setBuffKey("ActionAdvance");
                buff.setBuffValue(0.4);
                buff.setBuffLifetime(1);
                buff.setCountsDownAtTurnEnd(false);
                buff.setBuffOwnerAvatarId("1414");
                buff.setBuffRecipientAvatarId("1414");
                buff.setConsumeOnApplication(true);
                buff.setUsesLeft(1);
                initialBuffs.add(buff);

                return initialBuffs;
            }
        }
        return new ArrayList<>();
    }

    public List<RotationMember> debugInitRotation(Team team, List<BuffInstance> initialBuffs) {
        PriorityQueue<RotationMember> rotationOrder = rotationBuilder.init(team, initialBuffs, null);
        List<RotationMember> ordered = new ArrayList<>();
        while (!rotationOrder.isEmpty()) {
            ordered.add(rotationOrder.poll());
        }
        return ordered;
    }

    // Debug/verification only: runs Himkin's speed demon team over N cycles
    public RotationRunResult debugRunRotation(TeamMemberDTO teamMemberDTO, ActionDecisionMaker actionDecisionMaker, int rotationCycles) {
        String teamName = LEADERBOARD_NAMES[0];
        List<BuffInstance> initialBuffs = getInitialBuffs(teamName);

        List<TurnRecord> turnRecords = simulationEngine.simulationEngine(teamMemberDTO, teamName, initialBuffs, actionDecisionMaker, rotationCycles);

        Set<String> seenAvatarIds = new HashSet<>();
        Set<BuffInstance> initialBuffsForDisplay = new LinkedHashSet<>();
        for (TurnRecord record : turnRecords) {
            if (record.isCycleMarker() || !seenAvatarIds.add(record.getAvatarId())) continue;
            for (BuffInstance buffInstance : record.getBuffInstances()) {
                if (buffInstance.getBuffId() == null || buffInstance.getBuffId().charAt(0) != '0') break;
                initialBuffsForDisplay.add(buffInstance);
            }
        }

        RotationRunResult result = new RotationRunResult();
        result.setInitialBuffs(new ArrayList<>(initialBuffsForDisplay));
        result.setTurnRecords(turnRecords);
        return result;
    }

}
