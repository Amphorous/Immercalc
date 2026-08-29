package org.hoyo.immercalc.optimiser.honkerDamage.model;

import lombok.Data;
import lombok.Generated;

@Data
public class BuffInstance {
    // Sentinel recipientId meaning "applies to the whole team" - a List can
    // hold any number of buffs sharing this same marker, unlike the old
    // Map<String, BuffInstance>-keyed-by-recipient design this replaced,
    // which could only ever hold one team-wide buff at a time.
    public static final String TEAM_RECIPIENT_ID = "TEAM";
    private String buffId; // turn_no+"-"+buffInstances.size() (index this buff occupies once added)

    private String buffName; // Buff's origin name i.e, Divine Provision from HuoHuo
    private String buffKey; // which stat gets buffed
    private Double buffValue;
    private Integer buffLifetime; // if '-1' then buff lasts forever
    private Boolean countsDownAtTurnEnd;

    private String buffOwner; // maintain a new HashMap<String avatarId, List<BuffInstance>>
    private String recipientId; // avatarId of the receiver, or TEAM_RECIPIENT_ID

    // for buffs like action advance which tick down after each use
    private Boolean consumeOnApplication = false;
    private Integer usesLeft;

    // Value copy, not a reference - callers snapshot a member's buffs at a
    // point in time (e.g. TurnRecord) while the live BuffInstance objects
    // keep getting mutated in place by turnStartBuffStep/turnEndBuffStep.
    public BuffInstance copy() {
        BuffInstance copy = new BuffInstance();
        copy.setBuffId(buffId);
        copy.setBuffName(buffName);
        copy.setBuffKey(buffKey);
        copy.setBuffValue(buffValue);
        copy.setBuffLifetime(buffLifetime);
        copy.setCountsDownAtTurnEnd(countsDownAtTurnEnd);
        copy.setBuffOwner(buffOwner);
        copy.setRecipientId(recipientId);
        copy.setConsumeOnApplication(consumeOnApplication);
        copy.setUsesLeft(usesLeft);
        return copy;
    }
}

// certain buffs tick down in another unit's turns (ex: Sunday, robin etc buffs)
// we give the reference of the same buff instance obj to all units who have it, and tick it down at the end of the buffOwner's turn
