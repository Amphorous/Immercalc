package org.hoyo.immercalc.optimiser.honkerDamage.service;

import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class Objective {

    private final Map<String, CharacterDmgCalculator> calculatorsByAvatarId;

    public Objective(List<CharacterDmgCalculator> calculators) {
        this.calculatorsByAvatarId = calculators.stream().collect(Collectors.toMap(CharacterDmgCalculator::getAvatarId, Function.identity()));
    }

    // Every avatarId with a real CharacterDmgCalculator bean - this is what
    // /honker/get-list reports and what gets pushed to Delta on startup
    // (LeaderboardPushRunner), so Delta only ever calls dmgCalc for avatarIds
    // actually implemented here. Derived from the injected beans instead of
    // hand-maintained, so it can't go stale the way a separate constant list
    // could - adding a new character means writing its CharacterDmgCalculator
    // implementation and nothing else.
    public Set<String> getSupportedAvatarIds() {
        return calculatorsByAvatarId.keySet();
    }

    // 1) delta calls damage calc from subloader <- team member dto is provided
    public Double dmgCalc(TeamMemberDTO teamMemberDTO, String avatarId, Integer rotationCycles) {
        CharacterDmgCalculator calculator = calculatorsByAvatarId.get(avatarId);
        if (calculator == null) {
            throw new IllegalArgumentException("No dmg calc implemented for avatarId " + avatarId);
        }
        return calculator.dmgCalc(teamMemberDTO, rotationCycles);
    }

}
