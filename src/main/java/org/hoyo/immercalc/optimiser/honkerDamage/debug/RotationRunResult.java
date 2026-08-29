package org.hoyo.immercalc.optimiser.honkerDamage.debug;

import lombok.Data;
import org.hoyo.immercalc.optimiser.honkerDamage.model.BuffInstance;
import org.hoyo.immercalc.optimiser.honkerDamage.model.TurnRecord;

import java.util.List;

@Data
public class RotationRunResult {
    private List<BuffInstance> initialBuffs;
    private List<TurnRecord> turnRecords;
}
