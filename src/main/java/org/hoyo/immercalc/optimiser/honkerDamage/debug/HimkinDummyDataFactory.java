package org.hoyo.immercalc.optimiser.honkerDamage.debug;

import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Fixtures for the /honker/debug endpoint - not production code, just enough
// to exercise RotationBuilder.init() with realistic-shaped data.
public class HimkinDummyDataFactory {

    private static final Random RANDOM = new Random();

    private static final String HIMKIN_AVATAR_ID = "1510";
    private static final List<String> TEAMMATE_AVATAR_IDS = List.of("9001", "9002", "9003");

    public static Team buildDummyTeam() {
        List<RotationMember> members = new ArrayList<>();
        members.add(buildHimkin());
        for (String avatarId : TEAMMATE_AVATAR_IDS) {
            members.add(buildRandomTeammate(avatarId));
        }

        Team team = new Team();
        team.setTeamMembers(members);
        team.setSkillPoints(3);
        return team;
    }

    public static List<BuffInstance> buildDummyBuffs(Team team) {
        List<BuffInstance> buffs = new ArrayList<>();

        // One ActionAdvance or ActionDelay per teammate - this is what
        // actually moves each member's actionGauge away from the shared
        // 10000.0 default before the rotation queue is built.
        for (String avatarId : TEAMMATE_AVATAR_IDS) {
            boolean advance = RANDOM.nextBoolean();
            BuffInstance buff = new BuffInstance();
            buff.setBuffName(advance ? "Dummy Advance" : "Dummy Delay");
            buff.setBuffKey(advance ? "ActionAdvance" : "ActionDelay");
            buff.setBuffValue(0.1 + RANDOM.nextDouble() * 0.2); // 0.10-0.30
            buff.setBuffLifetime(1);
            buff.setCountsDownAtTurnEnd(true);
            buff.setBuffOwnerAvatarId(avatarId);
            buff.setBuffRecipientAvatarId(avatarId);
            buffs.add(buff);
        }

        // One team-wide speed buff.
        BuffInstance teamBuff = new BuffInstance();
        teamBuff.setBuffName("Dummy Team Speed Buff");
        teamBuff.setBuffKey("SpeedAddedRatio");
        teamBuff.setBuffValue(0.1);
        teamBuff.setBuffLifetime(2);
        teamBuff.setCountsDownAtTurnEnd(false);
        teamBuff.setBuffOwnerAvatarId(HIMKIN_AVATAR_ID);
        teamBuff.setBuffRecipientAvatarId(BuffInstance.TEAM_RECIPIENT_ID);
        buffs.add(teamBuff);

        // One decorative buff RotationBuilder doesn't react to - proves
        // non-speed buffs are still carried/reported, not silently dropped.
        String decoratedAvatarId = TEAMMATE_AVATAR_IDS.get(RANDOM.nextInt(TEAMMATE_AVATAR_IDS.size()));
        BuffInstance decorative = new BuffInstance();
        decorative.setBuffName("Dummy Crit Buff");
        decorative.setBuffKey("CriticalChance");
        decorative.setBuffValue(0.05);
        decorative.setBuffLifetime(3);
        decorative.setCountsDownAtTurnEnd(false);
        decorative.setBuffOwnerAvatarId(decoratedAvatarId);
        decorative.setBuffRecipientAvatarId(decoratedAvatarId);
        buffs.add(decorative);

        return buffs;
    }

    // Lifted from the real build JSON pasted in chat (fightProps.stats) -
    // field names already match TeamMemberDTO.stats' naming convention. Also
    // used as the seed TeamMemberDTO for debugRunRotation(), since
    // HimkinNova's own teamMemberDTOGenerator() sends this through Delta's
    // real /fightprop/swap-weapons endpoint before using it.
    public static TeamMemberDTO buildHimkinSeedTeamMemberDTO() {
        TeamMemberDTO dto = new TeamMemberDTO();
        Map<String, Double> stats = new HashMap<>();
        stats.put("HPDelta", 858.0151799999999);
        stats.put("BaseAttack", 1391.7959999999998);
        stats.put("AttackDelta", 450.17635699999994);
        stats.put("BaseAggro", 75.0);
        stats.put("DefenceAddedRatio", 0.0918);
        stats.put("SpeedDelta", 38.831999999999994);
        stats.put("CriticalDamage", 0.90824);
        stats.put("BaseDefence", 1014.3000000000001);
        stats.put("CriticalChance", 0.7910799990000001);
        stats.put("BaseSpeed", 98.0);
        stats.put("SPRatio", 1.194394);
        stats.put("BreakDamageAddedRatio", 0.17496);
        stats.put("BaseHP", 1972.1519999999998);
        stats.put("DefenceDelta", 116.42825300000001);
        stats.put("AttackAddedRatio", 1.1430400000000005);
        stats.put("FireAddedRatio", 0.08);
        // Not present in the source build (no speed% substat currently
        // equipped), but calculateSpeed() reads it unconditionally.
        stats.put("SpeedAddedRatio", 0.0);

        dto.setStats(stats);
        // relicNodes grouped by setId: 327 appears on 2 nodes, 131 on 4.
        dto.setRelicSets(Map.of(327, 2, 131, 4));
        dto.setWeaponId("23060");
        dto.setWeaponRank(1);
        return dto;
    }

    private static RotationMember buildHimkin() {
        RotationMember member = new RotationMember();
        member.setAvatarId(HIMKIN_AVATAR_ID);
        member.setTeamMemberDTO(buildHimkinSeedTeamMemberDTO());
        member.setBuffInstances(new ArrayList<>());
        return member;
    }

    private static RotationMember buildRandomTeammate(String avatarId) {
        TeamMemberDTO dto = new TeamMemberDTO();
        Map<String, Double> stats = new HashMap<>();
        stats.put("BaseSpeed", 90.0 + RANDOM.nextDouble() * 60.0); // 90-150
        stats.put("SpeedDelta", 0.0);
        stats.put("SpeedAddedRatio", 0.0);
        dto.setStats(stats);
        dto.setRelicSets(Map.of());
        dto.setWeaponId("unknown");
        dto.setWeaponRank(1);

        RotationMember member = new RotationMember();
        member.setAvatarId(avatarId);
        member.setTeamMemberDTO(dto);
        member.setBuffInstances(new ArrayList<>());
        return member;
    }
}
