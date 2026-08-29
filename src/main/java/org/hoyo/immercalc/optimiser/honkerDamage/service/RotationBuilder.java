package org.hoyo.immercalc.optimiser.honkerDamage.service;

import org.hoyo.immercalc.optimiser.honkerDamage.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

// Stateless - init() returns the built rotationOrder instead of storing it on
// this, so every caller gets its own local queue and this is safe to share
// as a singleton bean across concurrent dmgCalc calls (previously held
// rotationOrder/finalisedTurnOrder as public instance fields, which is why
// @Service was disabled - two concurrent calcs would have stomped on each
// other's state).
@Service
public class RotationBuilder {

    /**
     * Public so ActionDecisionMaker implementations (debug and real business
     * logic alike) can apply a buff mid-rotation - init() uses this too.
     * @param team
     * @param buffInstance
     */
    public void applyIndividualBuff(Team team, BuffInstance buffInstance, int turnNumber) {
        buffInstance.setBuffId(turnNumber + "-" + team.getMember(buffInstance.getRecipientId()).getBuffInstances().toArray().length);
        team.getMember(buffInstance.getRecipientId()).getBuffInstances().add(buffInstance);
    }

    /**
     *
     * @param team
     * @param buffInstance
     */
    private void applyTeamBuff(Team team, BuffInstance buffInstance, int turnNumber) {
        for (RotationMember member : team.getTeamMembers()) {
            if (!member.getIsCycleIndicator()) {
                buffInstance.setBuffId(turnNumber + "-" + member.getBuffInstances().toArray().length);
                member.getBuffInstances().add(buffInstance);
            }
        }
    }

    /**
     * team contains a List of RotationMembers in order (order matters, must be the same as in game left to right team order)
     * initialBuffs is the List of buff instances to apply - each one carries its own recipientId
     * (an avatarId, or BuffInstance.TEAM_RECIPIENT_ID to apply to the whole team).
     * <br/> <br/>
     * The buffInstances will be given as references to every holder,
     * and the ticking happens only at the end of buffOwner's turn.
     *
     * @param team Team.java
     * @param initialBuffs List<BuffInstance>
     * @param cycle RotationMember
     */
    public PriorityQueue<RotationMember> init(Team team, List<BuffInstance> initialBuffs, RotationMember cycle) {
        PriorityQueue<RotationMember> rotationOrder = new PriorityQueue<>(
                Comparator.comparingDouble(RotationMember::getActionValue) // (asc ag) TODO read RotationMember.java documentation to learn why AG is used instead of AV. (Future Me) nvm, it has to be by action value, cuz imagine speed 1000 AG 10001 vs speed 1 AG 10000, clearly the former is faster cuz their AV is lower than the latter
                        .thenComparingDouble(RotationMember::getTeamSlot) // tiebreaker using slot (asc)
        );

        // teamBuff.setRecipientId(BuffInstance.TEAM_RECIPIENT_ID); TODO thats how you teamBuff
        for (BuffInstance buffInstance : initialBuffs) { // TODO be careful with buffInstance objs, we need to pass them as references
            if (BuffInstance.TEAM_RECIPIENT_ID.equals(buffInstance.getRecipientId())) applyTeamBuff(team, buffInstance, 0);
            else applyIndividualBuff(team, buffInstance, 0);
        }
        // evaluate each units speed using buffs first, then get action value, then populate the rotationOrder Queue
        // once the queue is populated, then poll,
        // at the start of the turn for each buff if(!countsDownAtTurnEnd) then reduce lifetime, (if 0 delete buff) and eval new stats (temp)
        // if(countsDownAtTurnEnd) then eval new stats (temp) then reduce lifetime
        // NOTE at init, no character's turn has actually started, so no buff countdown occurs
        // only speed rel buffs are applied to calc effective speed to populate turn order

        // init: calc speed, get action value, add all chars to rotationOrder
        // buffs like vonwacq which apply turn one, turn start ticking buffs expire when the char steps onto feild
        // PQ will sort by action value, head is popped, action is taken, if ult available, then ult (autobattle rules)
        // specific conditions such as "if buffId expired, then always attempt skill" or "never skill (greg in speed himkin team)" are used to bind unit behaviour
        // popped unit's av, ag is recalced and is re added to the queue
        int teamSlot = 1;
        for (RotationMember rotationMember : team.getTeamMembers()) {
            if (!rotationMember.getIsCycleIndicator()) {
                rotationMember.setTeamSlot(teamSlot++); // shneaky team slot update
                Double effectiveSpeed = calculateSpeed(rotationMember);
                rotationMember.setLastEffectiveSpeed(effectiveSpeed);
                initialActionValue(rotationMember, effectiveSpeed); // ag needs to reflect live in av, av changes only reflect at turn ends in ag
                // i guess we just do the (av, ag) update at the end of every turn
            }
            rotationOrder.offer(rotationMember);
        }
        if (cycle != null) {
            rotationOrder.offer(cycle);
        }
        return rotationOrder;
    }

    // Whether this buff should still take effect right now. False once a
    // consumeOnApplication buff has used up its usesLeft - lifetime keeps
    // ticking down independently either way, we never delete buffs.
    private boolean isBuffActive(BuffInstance buffInstance) {
        if (buffInstance.getBuffLifetime() != null && buffInstance.getBuffLifetime() == 0) return false;
        if (Boolean.TRUE.equals(buffInstance.getConsumeOnApplication())) {
            return buffInstance.getUsesLeft() > 0;
        }
        return true;
    }

    // Call only from inside the switch branch that actually applied the
    // buff's effect - e.g. ActionAdvance/ActionDelay should shift AV exactly
    // once no matter how many times recalculateActionValue runs afterward.
    private void consumeUseIfApplicable(BuffInstance buffInstance) {
        if (Boolean.TRUE.equals(buffInstance.getConsumeOnApplication())) {
            buffInstance.setUsesLeft(buffInstance.getUsesLeft() - 1);
        }
    }

    private Double calculateSpeed(RotationMember member) {
        Double effectiveSpeed;
        Double baseSpeed = 0.0; // base
        Double speedDelta = 0.0; // flat
        Double speedAddedRatio = 0.0; // %

        for(BuffInstance buffInstance: member.getBuffInstances()) {
            if (!isBuffActive(buffInstance)) continue;
            switch (buffInstance.getBuffKey()) {
                case "BaseSpeed" -> {
                    baseSpeed = buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
                case "SpeedDelta" -> {
                    speedDelta = buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
                case "SpeedAddedRatio" -> {
                    speedAddedRatio = buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
            }
        }
        effectiveSpeed = (
                (getStat(member, "BaseSpeed") + baseSpeed) * (1 + (getStat(member, "SpeedAddedRatio")) + speedAddedRatio)
        ) + (getStat(member, "SpeedDelta") + speedDelta);
        return effectiveSpeed;
    };

    private void initialActionValue(RotationMember member, Double effectiveSpeed) {
        // since this is init, AG is 10000 (default), no complex AV <-> AG linking logic here (this will be impl in specific dmg calc classes)
        Double actionGauge = member.getActionGauge();
        Double actionValue = member.getActionValue();
        Double actionAdvance = 0.0; // these values are %, so usually below 1, above 0
        Double actionDelay = 0.0;

        for(BuffInstance buffInstance: member.getBuffInstances()) {
            if (!isBuffActive(buffInstance)) continue;
            switch (buffInstance.getBuffKey()) {
                case "ActionAdvance" -> {
                    actionAdvance = buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
                case "ActionDelay" -> {
                    actionDelay = buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
            }
        }

        actionGauge = actionGauge - ( (actionAdvance - actionDelay) * 10000 );
        actionValue = actionGauge / effectiveSpeed;

        member.setActionValue(actionValue);
        member.setActionGauge(actionGauge);
        member.setFinalActionAdvance(actionAdvance - actionDelay);

    }

    public Double getStat(RotationMember member, String statName) {
        return member.getTeamMemberDTO().getStats().getOrDefault(statName, 0.0);
    }

    public Double getStat(TeamMemberDTO member, String statName){
        return member.getStats().getOrDefault(statName, 0.0);
    }

    public void turnStartBuffStep(List<RotationMember> members, String turnOwnerAvatarId) {
        for (RotationMember member : members) {
            List<BuffInstance> buffInstances = member.getBuffInstances();
            for (BuffInstance buffInstance : buffInstances) {
                if(buffInstance.getBuffOwner().equals(turnOwnerAvatarId) && !buffInstance.getCountsDownAtTurnEnd() && (buffInstance.getBuffLifetime() > 0)) {
                    // this buff ticks down now (if buff timer depends on current turn owner, the buff ticks at turn start, and the buff isnt already expired)
                    buffInstance.setBuffLifetime(buffInstance.getBuffLifetime() - 1);
                }
            }
        }
    }

    public void turnEndBuffStep(List<RotationMember> members, String turnOwnerAvatarId) {
        for (RotationMember member : members) {
            List<BuffInstance> buffInstances = member.getBuffInstances();
            for (BuffInstance buffInstance : buffInstances) {
                if(buffInstance.getBuffOwner().equals(turnOwnerAvatarId) && buffInstance.getCountsDownAtTurnEnd()  && (buffInstance.getBuffLifetime() > 0)) {
                    // this buff ticks down now (if buff timer depends on current turn owner, the buff ticks at turn end, and the buff isnt already expired)
                    buffInstance.setBuffLifetime(buffInstance.getBuffLifetime() - 1);
                }
            }
        }
    }
    


    /**
     * Inputs contain rotation members with their buffs appropriately stepped, no change in AG/AV
     * Speed, AG and AV are recalculated
     */
    public void recalculateTurnOrder(List<RotationMember> members, PriorityQueue<RotationMember> emptyQueue, RotationMember cycle,
                                                              String turnOwnerAvatarId, Double consumedActionValue) {
        emptyQueue.clear();

        if(Objects.equals(turnOwnerAvatarId, "cycle")) cycle.setActionValue(100.0);
        else recalculateActionValue(cycle, consumedActionValue);
        emptyQueue.offer(cycle);

        for(RotationMember member : members) {
            if(member.getAvatarId().equals(turnOwnerAvatarId)) {
                member.setActionGauge(10000.0);
                member.setActionValue(10000.0 / member.getLastEffectiveSpeed());
            } else recalculateActionValue(member, consumedActionValue);
            emptyQueue.offer(member);
        }
    }

    /**
     * New AV = Current AV * Current SPD / New SPD - 10000 * (Advance - Delay) / New SPD
     * @param member
     * @param consumedActionValue
     */
    public void recalculateActionValue(RotationMember member, Double consumedActionValue) {
        consumedActionValue = Math.max(consumedActionValue, 0.0);

        if(member.getIsCycleIndicator()) {
            member.setActionValue(Math.max(member.getActionValue() - consumedActionValue, 0.0));
            return;
        }

        Double baseSpeed = getStat(member, "BaseSpeed");
        Double speedDelta = getStat(member, "SpeedDelta");
        Double speedAddedRatio = getStat(member, "SpeedAddedRatio");
        Double actionAdvance = 0.0;
        Double actionDelay = 0.0;

        for (BuffInstance buffInstance : member.getBuffInstances()) {
            if (!isBuffActive(buffInstance)) continue;
            switch (buffInstance.getBuffKey()) {
                case "BaseSpeed" -> {
                    baseSpeed += buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
                case "SpeedDelta" -> {
                    speedDelta += buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
                case "SpeedAddedRatio" -> {
                    speedAddedRatio += buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
                case "ActionAdvance" -> {
                    actionAdvance += buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
                case "ActionDelay" -> {
                    actionDelay += buffInstance.getBuffValue();
                    consumeUseIfApplicable(buffInstance);
                }
            }
        }

        Double newEffectiveSpeed = baseSpeed * (1 + speedAddedRatio) + speedDelta;
        Double actionValue = member.getActionValue() * (member.getLastEffectiveSpeed() / newEffectiveSpeed)
                - 10000 * (actionAdvance - actionDelay) / newEffectiveSpeed;

        member.setLastEffectiveSpeed(newEffectiveSpeed);
        member.setActionValue(Math.max(actionValue, 0.0));
        member.setActionGauge(Math.max(actionValue, 0.0) * newEffectiveSpeed);

        actionValue = Math.max(member.getActionValue() - consumedActionValue, 0.0);
        member.setActionValue(actionValue);
        member.setActionGauge(actionValue * member.getLastEffectiveSpeed());
    }

    /**
     * Old code, works, but is pretty bad
     * @param member
     * @param consumedActionValue
     */
    public void recalculateActionValueOld(RotationMember member, Double consumedActionValue) {
        Double baseSpeed = getStat(member, "BaseSpeed"); // base
        Double speedDelta = getStat(member, "SpeedDelta"); // flat
        Double speedAddedRatio = getStat(member, "SpeedAddedRatio"); // %
        for(BuffInstance buffInstance: member.getBuffInstances()) {
            if(buffInstance.getBuffLifetime() != 0){
                switch (buffInstance.getBuffKey()) {
                    case "BaseSpeed" -> {
                        baseSpeed += buffInstance.getBuffValue();
                        Double newEffectiveSpeed = baseSpeed * (1 + speedAddedRatio) + speedDelta;
                        Double actionValue = member.getActionValue();
                        actionValue = actionValue * (member.getLastEffectiveSpeed() / newEffectiveSpeed);

                        member.setLastEffectiveSpeed(newEffectiveSpeed);
                        member.setActionValue(actionValue);
                        member.setActionGauge(actionValue * newEffectiveSpeed);

                    }
                    case "SpeedDelta" -> {
                        speedDelta += buffInstance.getBuffValue();
                        Double newEffectiveSpeed = baseSpeed * (1 + speedAddedRatio) + speedDelta;
                        Double actionValue = member.getActionValue();
                        actionValue = actionValue * (member.getLastEffectiveSpeed() / newEffectiveSpeed);

                        member.setLastEffectiveSpeed(newEffectiveSpeed);
                        member.setActionValue(actionValue);
                        member.setActionGauge(actionValue * newEffectiveSpeed);
                    }
                    case "SpeedAddedRatio" -> {
                        speedAddedRatio += buffInstance.getBuffValue();
                        Double newEffectiveSpeed = baseSpeed * (1 + speedAddedRatio) + speedDelta;
                        Double actionValue = member.getActionValue();
                        actionValue = actionValue * (member.getLastEffectiveSpeed() / newEffectiveSpeed);

                        member.setLastEffectiveSpeed(newEffectiveSpeed);
                        member.setActionValue(actionValue);
                        member.setActionGauge(actionValue * newEffectiveSpeed);
                    }
                    case "ActionAdvance" -> {
                        Double actionGauge = member.getActionValue() * member.getLastEffectiveSpeed();
                        actionGauge = actionGauge - (buffInstance.getBuffValue() * 10000);
                        member.setActionGauge(actionGauge);
                        member.setActionValue(actionGauge / member.getLastEffectiveSpeed());
                    }
                    case "ActionDelay" -> {
                        Double actionGauge = member.getActionValue() * member.getLastEffectiveSpeed();
                        actionGauge = actionGauge + (buffInstance.getBuffValue() * 10000);
                        member.setActionGauge(actionGauge);
                        member.setActionValue(actionGauge / member.getLastEffectiveSpeed());
                    }
                }
            }
        }
        
        Double actionValue = member.getActionValue() - consumedActionValue;
        member.setActionValue(actionValue);
        member.setActionGauge(actionValue * member.getLastEffectiveSpeed());
        
    }
}
