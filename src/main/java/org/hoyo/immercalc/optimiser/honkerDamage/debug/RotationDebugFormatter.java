package org.hoyo.immercalc.optimiser.honkerDamage.debug;

import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.RotationMember;
import org.hoyo.immercalc.optimiser.honkerDamage.model.Team;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TeamMemberDTO;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TurnRecord;

import java.util.List;
import java.util.Map;

// Plain-text dump of the inputs/outputs of a RotationBuilder.init() call, for
// eyeballing correctness - not a stable API shape, debug tooling only.
public class RotationDebugFormatter {

    public static String format(Team team, List<BuffInstance> initialBuffs, List<RotationMember> finalOrder) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== STARTING CONDITIONS ===\n");
        int slot = 1;
        for (RotationMember member : team.getTeamMembers()) {
            TeamMemberDTO dto = member.getTeamMemberDTO();
            sb.append(String.format("%d. %s%n", slot++, member.getAvatarId()));
            sb.append("   stats: ").append(formatStats(dto.getStats())).append("\n");
            sb.append("   relicSets: ").append(dto.getRelicSets()).append("\n");
            sb.append("   weapon: ").append(dto.getWeaponId()).append(" R").append(dto.getWeaponRank()).append("\n");
        }

        sb.append("\n=== BUFFS PROVIDED ===\n");
        for (BuffInstance buff : initialBuffs) {
            String recipient = BuffInstance.TEAM_RECIPIENT_ID.equals(buff.getRecipientId()) ? "TEAM" : buff.getRecipientId();
            sb.append(String.format(
                    "owner=%-8s recipient=%-8s key=%-16s value=%-8.4f lifetime=%-4s countsDownAtTurnEnd=%-6s (%s)%n",
                    buff.getBuffOwner(), recipient, buff.getBuffKey(), buff.getBuffValue(),
                    buff.getBuffLifetime(), buff.getCountsDownAtTurnEnd(), buff.getBuffName()
            ));
        }

        sb.append("\n=== FINAL ROTATION ORDER (turn -1) ===\n");
        int order = 1;
        for (RotationMember member : finalOrder) {
            Double originalSpeed = member.getTeamMemberDTO().getStats().get("BaseSpeed");
            sb.append(String.format(
                    "%d. %-8s actionGauge=%-12.4f actionValue=%-12.4f teamSlot=%d  |  originalSpeed=%-10.4f effectiveSpeed=%-10.4f finalActionAdvance=%-10.4f%n",
                    order++, member.getAvatarId(), member.getActionGauge(), member.getActionValue(), member.getTeamSlot(),
                    originalSpeed, member.getLastEffectiveSpeed(), member.getFinalActionAdvance()
            ));
        }

        return sb.toString();
    }

    // Plain-text dump of a full debugRunRotation() run: the initial buffs,
    // then one row per turn taken, with a centered "=" banner marking each
    // cycle boundary instead of a normal character row.
    public static String formatTurnOrder(List<BuffInstance> initialBuffs, List<TurnRecord> turnRecords) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== INITIAL BUFFS ===\n");
        for (BuffInstance buff : initialBuffs) {
            String recipient = BuffInstance.TEAM_RECIPIENT_ID.equals(buff.getRecipientId()) ? "TEAM" : buff.getRecipientId();
            sb.append(String.format(
                    "owner=%-8s recipient=%-8s key=%-16s value=%-8.4f lifetime=%-4s countsDownAtTurnEnd=%-6s (%s)%n",
                    buff.getBuffOwner(), recipient, buff.getBuffKey(), buff.getBuffValue(),
                    buff.getBuffLifetime(), buff.getCountsDownAtTurnEnd(), buff.getBuffName()
            ));
        }

        sb.append("\n=== TURN ORDER ===\n");
        for (TurnRecord record : turnRecords) {
            if (record.isCycleMarker()) {
                sb.append(centeredCycleBanner(record.getCycleNumber())).append("\n");
            } else {
                sb.append(String.format(
                        "%d. %-8s %-8s speed=%-10.4f actionValue=%-12.4f actionGauge=%-12.4f%n",
                        record.getTurnNumber(), record.getAvatarId(), nameFor(record.getAvatarId()),
                        record.getSpeedStat(), record.getActionValue(), record.getActionGauge()
                ));
                if (record.getActionSummary() != null && !record.getActionSummary().isEmpty()) {
                    sb.append("     -> ").append(record.getActionSummary()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    // Temp debug-only display names - avatarId is the only real identity the
    // engine itself knows about.
    private static String nameFor(String avatarId) {
        return switch (avatarId) {
            case "1510" -> "Himkin";
            case "1313" -> "Sunday";
            case "1004" -> "Welt";
            case "1414" -> "Greg";
            default -> avatarId;
        };
    }

    private static String centeredCycleBanner(int cycleNumber) {
        final int width = 80;
        String label = " CYCLE -" + cycleNumber + "- ";
        int padTotal = Math.max(0, width - label.length());
        int padLeft = padTotal / 2;
        int padRight = padTotal - padLeft;
        return "=".repeat(padLeft) + label + "=".repeat(padRight);
    }

    private static String formatStats(Map<String, Double> stats) {
        StringBuilder sb = new StringBuilder();
        stats.forEach((key, value) -> sb.append(key).append("=").append(String.format("%.4f", value)).append(", "));
        if (!sb.isEmpty()) sb.setLength(sb.length() - 2);
        return sb.toString();
    }
}
