package org.hoyo.immercalc.client;

import org.hoyo.immercalc.client.model.SwapWeaponsRequest;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// updateLeaderboardList() used to live here (POST /load/leaderboard-list)
// but was removed - LeaderboardPushRunner now writes the avatarId set to
// Redis directly instead of pushing it to Delta over Feign. That switch is
// what fixes the multi-instance gap: a Feign call through the load balancer
// only ever reached one Delta instance, Redis pub/sub reaches all of them.
@FeignClient(name = "delta-me13")
public interface DeltaClient {

    @PostMapping("/fightprop/swap-weapons")
    List<TeamMemberDTO> swapWeapons(@RequestBody SwapWeaponsRequest request);
}
