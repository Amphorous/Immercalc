package org.hoyo.immercalc.optimiser.honkerDamage.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

@Data
public class Summon extends RotationMember { // extended from rotmem so all the other props are generated there

    public Summon(String avatarId, String summonerId, String ownerId, HashMap<String, Double> stats, boolean allowTeamBuffs) {
        this.setAvatarId(avatarId);
        this.summonerId = summonerId;
        this.ownerId = ownerId;
        this.allowTeamBuffs = allowTeamBuffs;
        this.setActionGauge(10000.0);
        this.setBuffInstances(new ArrayList<>());
        this.setTeamMemberDTO(new TeamMemberDTO(stats, null, null, null));
        this.setBuffInstances(new ArrayList<>());
    }
}
