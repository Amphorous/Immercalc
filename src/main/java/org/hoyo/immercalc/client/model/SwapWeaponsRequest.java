package org.hoyo.immercalc.client.model;

import lombok.Data;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;

import java.util.Map;

@Data
public class SwapWeaponsRequest {
    private TeamMemberDTO teamMemberDTO;
    private Map<String, Integer> weapons; // {weaponId: rank, ...}
}
