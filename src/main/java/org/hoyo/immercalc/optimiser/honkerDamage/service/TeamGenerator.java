package org.hoyo.immercalc.optimiser.honkerDamage.service;

import org.hoyo.immercalc.client.DeltaClient;
import org.hoyo.immercalc.client.model.SwapWeaponsRequest;
import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Builds Team/TeamMemberDTO presets for named team compositions - shared
// infrastructure any CharacterDmgCalculator can call to assemble a team,
// the same way every character shares RotationBuilder for turn-order math.
// Originally lived on HimkinNova only because it was the first (and so far
// only) caller - nothing here is Himkin-specific except the "1510" slot in
// the "HimkinSunnyWeltGreg" preset itself, exactly like "1313"/"1004"/"1414"
// are hardcoded for that same named preset.
@Service
public class TeamGenerator {

    private final RotationBuilder rotationBuilder;
    private final DeltaClient deltaClient;

    public TeamGenerator(RotationBuilder rotationBuilder, DeltaClient deltaClient) {
        this.rotationBuilder = rotationBuilder;
        this.deltaClient = deltaClient;
    }

    public Team teamGenerator(TeamMemberDTO teamMemberDTO, String teamName) {
        Team team;
        RotationMember[] members = new RotationMember[4];

        // TODO add a switch case for team name

        // team slot 1 - Himkin
        members[0] = new RotationMember();
        members[0].setAvatarId("1510");
        members[0].setTeamMemberDTO(teamMemberDTOGenerator(teamName, "1510", teamMemberDTO));
        members[0].setBuffInstances(new ArrayList<>());
        members[0].setUsesAltEnergy(false);
        members[0].setMaxEnergy(150.0);
        members[0].setMaxEnergyCapacity(150.0);
        members[0].setCurrentEnergy(75.0);

        // team slot 2 - Sunday
        members[1] = new RotationMember();
        members[1].setAvatarId("1313");
        members[1].setTeamMemberDTO(teamMemberDTOGenerator(teamName, "1313", teamMemberDTO));
        members[1].setBuffInstances(new ArrayList<>());
        members[1].setUsesAltEnergy(false);
        members[1].setMaxEnergy(130.0);
        members[1].setMaxEnergyCapacity(130.0);
        members[1].setCurrentEnergy(65.0);

        // team slot 3 - welt
        members[2] = new RotationMember();
        members[2].setAvatarId("1004");
        members[2].setTeamMemberDTO(teamMemberDTOGenerator(teamName, "1004", teamMemberDTO));
        members[2].setBuffInstances(new ArrayList<>());
        members[2].setUsesAltEnergy(false);
        members[2].setMaxEnergy(120.0);
        members[2].setMaxEnergyCapacity(120.0);
        members[2].setCurrentEnergy(60.0);

        // team slot 4 - greg
        members[3] = new RotationMember();
        members[3].setAvatarId("1414");
        members[3].setTeamMemberDTO(teamMemberDTOGenerator(teamName, "1414", teamMemberDTO));
        members[3].setBuffInstances(new ArrayList<>());
        members[3].setUsesAltEnergy(false);
        members[3].setMaxEnergy(135.0);
        members[3].setMaxEnergyCapacity(135.0);
        members[3].setCurrentEnergy(67.5);

        team = new Team(Arrays.asList(members), 5);
        return team;
    }

    public TeamMemberDTO teamMemberDTOGenerator(String teamName, String avatarId, TeamMemberDTO himkin) {
        TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
        switch (teamName) {
            case "HimkinSunnyWeltGreg":{ // speed demon himkin
                switch (avatarId) {
                    case "1510": {
                        // im calling the weapon swap individually rn cuz theres only one swap requirement,
                        // but this could change later, in which case call the wep swap as intended in a batch
                        SwapWeaponsRequest swapWeaponsRequest = new SwapWeaponsRequest();
                        swapWeaponsRequest.setTeamMemberDTO(himkin);
                        Map<String, Integer> weapons = new HashMap<>();
                        weapons.put("23060", 1);
                        swapWeaponsRequest.setWeapons(weapons);

                        List<TeamMemberDTO> himkinSig = deltaClient.swapWeapons(swapWeaponsRequest);

                        return himkinSig.getFirst();
                    }
                    case "1313": { // sunday
                        Map<String, Double> stats = new HashMap<>(); // use same speed as himkin
                        stats.put("HPDelta", 925.7552599999999);
                        stats.put("HPAddedRatio", 0.55728);
                        stats.put("BaseAttack", 1169.5320000000002);
                        stats.put("AttackDelta", 412.07256499999994);
                        stats.put("BaseAggro", 100.0);
                        stats.put("DefenceAddedRatio", 0.3734);
                        stats.put("SpeedDelta", rotationBuilder.getStat(himkin, "SpeedDelta") - 1); // at equal speed, sunday might go first every alternate turn
                        stats.put("CriticalDamage", 1.92276);
                        stats.put("BaseDefence", 996.66);
                        stats.put("CriticalChance", 0.05);
                        stats.put("BaseSpeed", rotationBuilder.getStat(himkin, "BaseSpeed"));
                        stats.put("SPRatio", 1.3443939999999999);
                        stats.put("BreakDamageAddedRatio", 0.12312);
                        stats.put("BaseHP", 2406.096);
                        stats.put("DefenceDelta", 80.441338);
                        stats.put("AttackAddedRatio", 0.0864);
                        stats.put("SpeedAddedRatio", rotationBuilder.getStat(himkin, "SpeedAddedRatio"));
                        stats.put("StatusProbability", 0.12096);
                        stats.put("StatusResistance", 0.5428799999999999);

                        Map<Integer, Integer> relicSets = new HashMap<>();
//  TODO
//                         *  Lushaka, the Sunken Seas:
//                         *      2-Pieces: Increases the wearer's Energy Regeneration Rate by 5%. If the wearer is not the first character in the team lineup,
//                         *                then increases the ATK of the first character in the team lineup by 12%.
//                         *
//                         *  Sacerdos' Relived Ordeal:
//                         *      2-Pieces: Increases SPD by 6%.
//                         *      4-Pieces: When using Skill or Ultimate on one ally target, increases the ability target's CRIT DMG by 18%,
//                         *                lasting for 2 turn(s). This effect can stack up to 2 time(s).
//
                        relicSets.put(317, 2);
                        relicSets.put(121, 4);

                        String weaponId = "23003"; // bronya sig: 10% ER, 1SP after 2 ults, skill = target gains 1 turn (turn end expiry) 30% dmg boost
                        Integer weaponRank = 1;

                        teamMemberDTO.setStats(stats);
                        teamMemberDTO.setRelicSets(relicSets);
                        teamMemberDTO.setWeaponId(weaponId);
                        teamMemberDTO.setWeaponRank(weaponRank);
                        return teamMemberDTO;
                    }
                    case "1004": { // welt
                        Map<String, Double> stats = new HashMap<>();
                        stats.put("HPDelta", 811.4438749999999);
                        stats.put("ImaginaryAddedRatio", 0.14400000000000002);
                        stats.put("HPAddedRatio", 0.27648);
                        stats.put("BaseAttack", 1203.048);
                        stats.put("AttackDelta", 409.95568799999995);
                        stats.put("BaseAggro", 100.0);
                        stats.put("DefenceAddedRatio", 0.23220000000000002);
                        stats.put("SpeedDelta", 46.532);
                        stats.put("CriticalDamage", 0.73976);
                        stats.put("BaseDefence", 972.405);
                        stats.put("CriticalChance", 0.2768);
                        stats.put("BaseSpeed", 102.0);
                        stats.put("SPRatio", 1.244394);
                        stats.put("BaseHP", 2183.832);
                        stats.put("DefenceDelta", 57.155688000000005);
                        stats.put("AttackAddedRatio", 0.5054400000000001);
                        stats.put("WindAddedRatio", 0.1);
                        stats.put("StatusProbability", 0.7854400000000002);
                        stats.put("StatusResistance", 0.22096);
                        // Not present in the source build - RotationBuilder.calculateSpeed()
                        // reads this unconditionally and would NPE without it.
                        stats.put("SpeedAddedRatio", 0.0);

                        Map<Integer, Integer> relicSets = new HashMap<>(); // devious 2vonwacq + 4eagle combo
//  TODO
//                         *  Sprightly Vonwacq:
//                         *      2-Pieces: Increases the wearer's Energy Regeneration Rate by 5%. When the wearer's SPD reaches 120 or higher,
//                         *                the wearer's action is Advanced Forward by 40% immediately upon entering battle.
//                         *
//                         *  Eagle of Twilight Line:
//                         *      2-Pieces: Increases Wind DMG by 10%.
//                         *      4-Pieces: After the wearer uses their Ultimate, their action is Advanced Forward by 25%.
//
                        relicSets.put(308, 2);
                        relicSets.put(110, 4);

                        String weaponId = "23004"; // welt sig: wearer's DMG to debuffed enemies up 24%. Skill = EHR for this attack up 18%, ATK up 24%.
                        Integer weaponRank = 1;

                        teamMemberDTO.setStats(stats);
                        teamMemberDTO.setRelicSets(relicSets);
                        teamMemberDTO.setWeaponId(weaponId);
                        teamMemberDTO.setWeaponRank(weaponRank);
                        return teamMemberDTO;
                    }
                    case "1414": { // greg
                        Map<String, Double> stats = new HashMap<>();
                        stats.put("HPDelta", 870.716445);
                        stats.put("HPAddedRatio", 0.3024);
                        stats.put("BaseAttack", 900.4799999999999);
                        stats.put("AttackDelta", 352.79999999999995);
                        stats.put("BaseAggro", 150.0);
                        stats.put("DefenceAddedRatio", 0.6216);
                        stats.put("SpeedDelta", 65.03200000000001);
                        stats.put("CriticalDamage", 0.62312);
                        stats.put("BaseDefence", 1174.1100000000001);
                        stats.put("CriticalChance", 0.14072);
                        stats.put("BaseSpeed", 97.0);
                        stats.put("SPRatio", 1.244394);
                        stats.put("BreakDamageAddedRatio", 0.05832);
                        stats.put("BaseHP", 1866.4560000000001);
                        stats.put("DefenceDelta", 33.870038);
                        stats.put("AttackAddedRatio", 1.3427200000000004);
                        stats.put("StatusProbability", 0.16416);
                        stats.put("StatusResistance", 0.07776);
                        // Not present in the source build - see the same note on Welt above.
                        stats.put("SpeedAddedRatio", 0.0);

                        Map<Integer, Integer> relicSets = new HashMap<>();
//  TODO
//                         *  Lushaka, the Sunken Seas:
//                         *      2-Pieces: Increases the wearer's Energy Regeneration Rate by 5%. If the wearer is not the first character in the team lineup,
//                         *                then increases the ATK of the first character in the team lineup by 12%.
//                         *
//                         *  Self-Enshrouded Recluse:
//                         *      2-Pieces: The provided Shield Effect increases by 10%.
//                         *      4-Pieces: Increases the Shield Effect provided by the wearer by 12%.
//                         *                When an ally target holds a Shield provided by the wearer, the ally target's CRIT DMG increases by 15%.
//
                        relicSets.put(317, 2);
                        relicSets.put(128, 4);

                        String weaponId = "21002"; // day 1: DEF up 16%. At battle start, team's All-Type RES up 8%. cannot stack.
                        Integer weaponRank = 5;

                        teamMemberDTO.setStats(stats);
                        teamMemberDTO.setRelicSets(relicSets);
                        teamMemberDTO.setWeaponId(weaponId);
                        teamMemberDTO.setWeaponRank(weaponRank);
                        return teamMemberDTO;
                    }
                }
                break;
            }
        }
        return teamMemberDTO;
    }

    public RotationMember getSeedCycle(){
        RotationMember cycle = new RotationMember();
        cycle.setAvatarId("cycle");
        cycle.setIsCycleIndicator(true);
        cycle.setCycleNumber(1); // a cycle has 150 action value in cycle 1, 100 AV in every subsequent cycle
        cycle.setActionValue(150.0);
        cycle.setTeamSlot(-1);
        return cycle;
    }
}
