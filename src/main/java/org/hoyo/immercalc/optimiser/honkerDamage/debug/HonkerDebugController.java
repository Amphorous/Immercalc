package org.hoyo.immercalc.optimiser.honkerDamage.debug;

import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;
import org.hoyo.immercalc.optimiser.honkerDamage.service.characters.HimkinNova;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Debug/verification only - not a stable API, safe to delete once
// RotationBuilder's math has been eyeballed and trusted.
@RestController
@RequestMapping("/honker/debug")
public class HonkerDebugController {

    private final HimkinNova himkinNova;
    private final DebugActionDecisionMaker debugActionDecisionMaker;

    public HonkerDebugController(HimkinNova himkinNova, DebugActionDecisionMaker debugActionDecisionMaker) {
        this.himkinNova = himkinNova;
        this.debugActionDecisionMaker = debugActionDecisionMaker;
    }

    @GetMapping(value = "/himkin-rotation-test", produces = MediaType.TEXT_PLAIN_VALUE)
    public String himkinRotationTest() {
        Team team = HimkinDummyDataFactory.buildDummyTeam();
        List<BuffInstance> buffs = HimkinDummyDataFactory.buildDummyBuffs(team);
        List<RotationMember> finalOrder = himkinNova.debugInitRotation(team, buffs);
        return RotationDebugFormatter.format(team, buffs, finalOrder);
    }

    // Runs HimkinNova's full rotation loop (init + turn-by-turn
    // recalculation) with DebugActionDecisionMaker standing in for real
    // business logic - "action" is empty for everyone except Greg (1414),
    // whose turn scripts a 100% ActionAdvance onto Himkin (1510).
    @GetMapping(value = "/himkin-full-rotation-test/{rotationCycles}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String himkinFullRotationTest(@PathVariable int rotationCycles) {
        TeamMemberDTO himkinSeed = HimkinDummyDataFactory.buildHimkinSeedTeamMemberDTO();
        RotationRunResult result = himkinNova.debugRunRotation(himkinSeed, debugActionDecisionMaker, rotationCycles);
        return RotationDebugFormatter.formatTurnOrder(result.getInitialBuffs(), result.getTurnRecords());
    }
}
