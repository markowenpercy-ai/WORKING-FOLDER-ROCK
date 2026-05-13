package com.go2super.database.entity.sub.battle.meta;

import com.go2super.obj.game.IntegerArray;
import com.go2super.service.battle.calculator.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import java.util.*;

@ToString
@Setter
@Getter
public class AssaultCellAttackMeta {

    public int sourceReduceSupply;
    public int sourceReduceStorage;

    public int sourceReduceHp;
    public int sourceReduceShipNum;

    public int targetReduceHealth;

    public int[] sourcePartId = new int[7];
    public int[] sourcePartNum = new int[7];
    public int[] sourcePartRate = new int[7];

    // Source commander skill usage animation
    public int sourceSkill;

    // Target commander skill usage animation
    public int targetSkill;

    // Source/Target shooting animations
    // 0 = Attacker normal hit
    // 1 = Attacker critical attack
    // 2 = Attacker normal hit and Defender critical
    // 3 = Attacker critical attack and Defender critical
    // 4 = Attacker double hits
    // 5 = Attacker double hits - critical attack
    // 6 = Attacker double hits and Defender critical
    // 7 = Attacker double hits - critical attack and Defender critical
    public int targetBlast;

    // For internal use purpose
    private int fromShipTeamId;
    private int toFortId;

    private int fromUserGuid;
    private int toUserGuid;

    private int fromAmount;
    private int toAmount;

    private int fromId;
    private int toId;

    private boolean attack;

    // For matrix utility
    private int attackerDirection;
    private int attackerSegmentedPosIndex;
    private int attackerPosIndex;
    private int attackerPos;

    // For playback utility
    private ShipReduction reflectionReduction;

    private List<FortReduction> fortReductions;
    private List<ShipShootdowns> shipShootdowns;

    private List<ModuleUsage> moduleUsages;

    @Transient
    private List<ShipAttackPacketAction> shipAttackPacketActions = new ArrayList<>();

    public AssaultCellAttackMeta() {

        Arrays.fill(sourcePartId, -1);
        Arrays.fill(sourcePartNum, -1);
        Arrays.fill(sourcePartRate, -1);

    }

    public IntegerArray sourcePartIdBuffer() {

        return new IntegerArray(sourcePartId);
    }

    public IntegerArray sourcePartNumBuffer() {

        return new IntegerArray(sourcePartNum);
    }

    public IntegerArray sourcePartRateBuffer() {

        return new IntegerArray(sourcePartRate);
    }

}
