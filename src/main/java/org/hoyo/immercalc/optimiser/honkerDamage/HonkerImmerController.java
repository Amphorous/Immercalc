package org.hoyo.immercalc.optimiser.honkerDamage;

import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;
import org.hoyo.immercalc.optimiser.honkerDamage.service.Objective;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/honker")
public class HonkerImmerController {

    private final Objective objective;

    public HonkerImmerController(Objective objective) {
        this.objective = objective;
    }

    @GetMapping("/get-list")
    public ResponseEntity<List<String>> getList(){
        // returns list of available avatarIds for dmg calc
        return ResponseEntity.ok(new ArrayList<>(objective.getSupportedAvatarIds()));
    }

    @PostMapping("/dmg-calc/{avatarId}/{rotation-cycles}")
    public ResponseEntity<Double> dmgCalc(@RequestBody TeamMemberDTO teamMemberDTO, @PathVariable("avatarId") String avatarId, @PathVariable("rotation-cycles") Integer rotationCycles) {
        // temp endpoint
        return ResponseEntity.ok(objective.dmgCalc(teamMemberDTO, avatarId, rotationCycles));
    }

}
