package com.go2super.service.battle.calculator;

import com.go2super.service.battle.BattleFleetCell;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipPosition {

    @Transient
    private BattleFleetCell battleFleetCell;

    private int direction;

    private int segmentedPosIndex;
    private int posIndex;
    private int pos;

    public boolean equals(ShipPosition other) {

        return direction == other.getDirection() && segmentedPosIndex == other.getSegmentedPosIndex() && posIndex == other.getPosIndex() && pos == other.getPos();
    }

}
