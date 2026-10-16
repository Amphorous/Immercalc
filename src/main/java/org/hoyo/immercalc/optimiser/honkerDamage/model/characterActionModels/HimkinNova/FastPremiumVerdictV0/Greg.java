package org.hoyo.immercalc.optimiser.honkerDamage.model.characterActionModels.HimkinNova.FastPremiumVerdictV0;

import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Summon;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.service.RotationBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Service
public class Greg {

    public static final String AVATAR_ID = "1414";
    private final RotationBuilder rotationBuilder;

    public Greg(RotationBuilder rotationBuilder) {
        this.rotationBuilder = rotationBuilder;
    }

    public void skill(RotationMember greg, PriorityQueue<RotationMember> rotationQueue, Team team, String targetId, int turnNumber) {
        greg.setCurrentEnergy(Math.clamp(greg.getCurrentEnergy() + 30, 0, greg.getMaxEnergy()));

        BuffInstance ownerBuff = new BuffInstance();
        ownerBuff.setBuffName("soulDragonBondmate");
        ownerBuff.setBuffLifetime(-1);
        ownerBuff.setBuffRecipientAvatarId(targetId);
        rotationBuilder.transferBuff(team, ownerBuff, turnNumber);

        if(team.getTeamMembers().stream().noneMatch(member -> member.getAvatarId().equals("soulDragon"))) {
            RotationMember summon = new Summon("soulDragon", AVATAR_ID, targetId, new HashMap<>(Map.of("BaseSpeed", 165.0)), false);
            rotationBuilder.createSummon(rotationQueue, team, summon);
        }

        // TODO check if greg's relics have an outgoing buff and apply them

        team.setSkillPoints(team.getSkillPoints() - 1);
    }

    public void basic(RotationMember greg, Team team, int turnNumber){
        greg.setCurrentEnergy(Math.clamp(greg.getCurrentEnergy() + 20, 0, greg.getMaxEnergy()));
        // dmg calc NA?
        team.setSkillPoints(team.getSkillPoints() + 1);
    }

    public void ult(RotationMember greg, Team team, int turnNumber){
        greg.setCurrentEnergy(5.0);
        // applyIndividualBuff to soulDragon (if available) called "enhanced" for 2 turns, endsAtTurnEnd
        if(team.getMember("soulDragon") != null){
            BuffInstance dragonEnhanced = new BuffInstance();
            dragonEnhanced.setBuffName("soulDragonEnhance");
            dragonEnhanced.setBuffLifetime(2);
            dragonEnhanced.setCountsDownAtTurnEnd(true);
            dragonEnhanced.setBuffRecipientAvatarId("soulDragon");
            dragonEnhanced.setBuffOwnerAvatarId("soulDragon");

            rotationBuilder.transferBuff(team, dragonEnhanced, turnNumber);
        }
        // dmg calc ult?
    }
}
