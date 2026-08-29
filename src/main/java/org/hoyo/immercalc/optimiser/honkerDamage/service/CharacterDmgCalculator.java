package org.hoyo.immercalc.optimiser.honkerDamage.service;

import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;

import java.util.List;

// One implementation per avatarId (see service/characters). Objective
// collects every bean implementing this interface and dispatches to the
// right one by getAvatarId() - adding a new character means writing a new
// implementation, nothing else needs to change in Objective.
public interface CharacterDmgCalculator {
    String getAvatarId();
    Double dmgCalc(TeamMemberDTO teamMemberDTO, Integer rotationCycles);
    List<BuffInstance> getInitialBuffs(String teamName);
}
