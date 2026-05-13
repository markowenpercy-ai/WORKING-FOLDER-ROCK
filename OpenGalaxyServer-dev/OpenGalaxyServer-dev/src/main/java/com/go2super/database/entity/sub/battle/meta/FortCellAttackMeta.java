package com.go2super.database.entity.sub.battle.meta;

import com.go2super.obj.game.IntegerArray;
import com.go2super.service.battle.calculator.FortShootdowns;
import com.go2super.service.battle.calculator.ShipAttackPacketAction;
import com.go2super.service.battle.calculator.ShipReduction;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import java.util.*;

@ToString
@Setter
@Getter
public class FortCellAttackMeta {

    public int targetShipTeamId;
    public int targetReduceSupply;
    public int targetReduceStorage;

    public int[] targetReduceHp = new int[9];
    public int[] targetReduceShipNum = new int[9];

    // For internal use purpose
    private int fromFortId;
    private int toShipTeamId;

    private int fromUserGuid;
    private int toUserGuid;

    private int toAmount;
    private int toId;

    private boolean attack;

    // For matrix utility
    private int defenderDirection;
    private int defenderSegmentedPosIndex;
    private int defenderPosIndex;
    private int defenderPos;

    // For playback utility
    private List<ShipReduction> shipReductions;
    private List<FortShootdowns> fortShootdowns;

    @Transient
    private List<ShipAttackPacketAction> shipAttackPacketActions = new ArrayList<>();

    public FortCellAttackMeta() {

        Arrays.fill(targetReduceHp, 0);
        Arrays.fill(targetReduceShipNum, 0);

    }

    public IntegerArray targetReduceHp() {

        return new IntegerArray(targetReduceHp);
    }

    public IntegerArray targetReduceShipNum() {

        return new IntegerArray(targetReduceShipNum);
    }

}
