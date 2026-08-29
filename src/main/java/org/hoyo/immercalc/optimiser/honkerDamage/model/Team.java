package org.hoyo.immercalc.optimiser.honkerDamage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    // Order matters - must match in-game left to right team order. A plain
    // List instead of the old LinkedHashMap<String, RotationMember>, since
    // each RotationMember now carries its own avatarId - there's nothing
    // left for an external map key to add.
    private List<RotationMember> teamMembers;
    private int skillPoints;

    public RotationMember getMember(String avatarId) {
        return teamMembers.stream()
                .filter(member -> member.getAvatarId().equals(avatarId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No team member with avatarId " + avatarId));
    }

//    public Team(List<RotationMember> teamMembers, int skillPoints) {
//        this.teamMembers = teamMembers;
//        this.skillPoints = skillPoints;
//    }
}
