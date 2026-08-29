package org.hoyo.immercalc.optimiser.honkerDamage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hoyo.immercalc.optimiser.honkerDamage.service.Objective;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;

// Immercalc always starts last (see control_panel.ps1 / docker-compose.yml),
// so this is what makes Delta's leaderboard avatarId set correct on a full
// boot. Redis - not a Feign call to Delta - is the source of truth:
// "leaderboard:avatarIds" is written here as a Redis Set (fully replacing
// whatever was there with the current SUPPORTED_AVATAR_IDS), and
// "leaderboard-updates" is published with the same list so any Celestia
// instance already running refreshes immediately too - see Celestia's
// LeaderboardUpdateListener/LeaderboardStartupSync. This is what fixes the
// old design's actual bug: a Feign call to Delta only ever reached one
// instance behind the load balancer, Redis pub/sub reaches all of them. Both
// string literals below must match Celestia's copies exactly - there's no
// compiler link between the two repos.
@Slf4j
@Component
public class LeaderboardPushRunner implements ApplicationRunner {

    private static final String LEADERBOARD_AVATAR_IDS_KEY = "leaderboard:avatarIds";
    private static final String LEADERBOARD_UPDATES_CHANNEL = "leaderboard-updates";

    private final RedisTemplate<String, String> redisTemplate;
    private final Objective objective;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LeaderboardPushRunner(RedisTemplate<String, String> redisTemplate, Objective objective) {
        this.redisTemplate = redisTemplate;
        this.objective = objective;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Set<String> avatarIds = objective.getSupportedAvatarIds();

            redisTemplate.delete(LEADERBOARD_AVATAR_IDS_KEY);
            if (!avatarIds.isEmpty()) {
                redisTemplate.opsForSet().add(LEADERBOARD_AVATAR_IDS_KEY, avatarIds.toArray(new String[0]));
            }

            String payload = objectMapper.writeValueAsString(new ArrayList<>(avatarIds));
            redisTemplate.convertAndSend(LEADERBOARD_UPDATES_CHANNEL, payload);

            log.info("Wrote leaderboard avatarId list to Redis and published update ({} avatarIds).", avatarIds.size());
        } catch (Exception e) {
            log.warn("Could not update leaderboard avatarId list in Redis at startup: {}", e.getMessage());
        }
    }
}
