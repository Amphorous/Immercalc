package org.hoyo.immercalc.optimiser.honkerDamage.debug;

import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TurnRecord;
import org.hoyo.immercalc.optimiser.honkerDamage.service.ActionDecisionMaker;
import org.hoyo.immercalc.optimiser.honkerDamage.service.RotationBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.PriorityQueue;

// Debug-only ActionDecisionMaker - every character does nothing on their turn
// except Sunday (1313), whose turn triggers a scripted 100% ActionAdvance on
// Himkin (1510), just to prove applyIndividualBuff mid-rotation actually
// reaches the rotation queue's ordering. Swap this for a real business-logic
// implementation once one exists.
@Component
public class DebugActionDecisionMaker implements ActionDecisionMaker {

    private final RotationBuilder rotationBuilder;

    public DebugActionDecisionMaker(RotationBuilder rotationBuilder) {
        this.rotationBuilder = rotationBuilder;
    }

    @Override
    public List<TurnRecord> actionDecision(PriorityQueue<RotationMember> rotationQueue, RotationMember turnOwner, Team team, int turnNumber) {
        if ("1313".equals(turnOwner.getAvatarId())) {
            BuffInstance buff = new BuffInstance();
            buff.setBuffName("Debug: Sunday's turn advances Himkin");
            buff.setBuffKey("ActionAdvance");
            buff.setBuffValue(1.0);
            buff.setBuffLifetime(1);
            buff.setCountsDownAtTurnEnd(false);
            buff.setBuffOwnerAvatarId("1510");
            buff.setBuffRecipientAvatarId("1510");
            buff.setConsumeOnApplication(true);
            buff.setUsesLeft(1);
            rotationBuilder.applyIndividualBuff(team, buff, turnNumber);

            return List.of(TurnRecord.forCharacterTurn(turnNumber, turnOwner, "sunday used skill [Action advance 1510 (himkin) by 100%]"));
        }
        return List.of(TurnRecord.forCharacterTurn(turnNumber, turnOwner, null));
    }
}
