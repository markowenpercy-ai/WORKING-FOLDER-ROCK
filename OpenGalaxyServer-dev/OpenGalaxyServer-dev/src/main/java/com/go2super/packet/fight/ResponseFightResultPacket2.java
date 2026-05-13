package com.go2super.packet.fight;

import com.go2super.obj.game.FightRobResource;
import com.go2super.obj.game.FightTotalExp;
import com.go2super.obj.game.FightTotalKill;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.Trash;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseFightResultPacket2 {

    private short kind;
    private short galaxyMapId;

    private long topAssaultUserId;
    private int galaxyId;

    private SmartString topAssaultModelName = SmartString.of("", 32);
    private SmartString topAssaultCommander = SmartString.of("", 32);
    private SmartString topAssaultOwner = SmartString.of("", 32);

    private int topAssaultValue;
    private short topAssaultBodyId;

    private short victory;

    private int attackShipNumber;
    private int attackLossNumber;
    private int defendShipNumber;
    private int defendLossNumber;

    @Trash(length = 10)
    private List<FightTotalKill> kill = new ArrayList<>();
    @Trash(length = 10)
    private List<FightTotalExp> exp = new ArrayList<>();
    @Trash(length = 10)
    private List<FightRobResource> prize = new ArrayList<>();

    public ResponseFightResultPacket to() {
        ResponseFightResultPacket responseFightResultPacket2 = new ResponseFightResultPacket();
        responseFightResultPacket2.setKind(getKind());
        responseFightResultPacket2.setGalaxyMapId(getGalaxyMapId());
        responseFightResultPacket2.setTopAssaultUserId(getTopAssaultUserId());
        responseFightResultPacket2.setGalaxyId(getGalaxyId());
        responseFightResultPacket2.setTopAssaultModelName(getTopAssaultModelName());
        responseFightResultPacket2.setTopAssaultCommander(getTopAssaultCommander());
        responseFightResultPacket2.setTopAssaultOwner(getTopAssaultOwner());
        responseFightResultPacket2.setTopAssaultValue(getTopAssaultValue());
        responseFightResultPacket2.setTopAssaultBodyId(getTopAssaultBodyId());
        responseFightResultPacket2.setVictory(getVictory());
        responseFightResultPacket2.setAttackShipNumber(getAttackShipNumber());
        responseFightResultPacket2.setAttackLossNumber(getAttackLossNumber());
        responseFightResultPacket2.setDefendShipNumber(getDefendShipNumber());
        responseFightResultPacket2.setDefendLossNumber(getDefendLossNumber());
        responseFightResultPacket2.setKill(getKill());
        responseFightResultPacket2.setExp(getExp());
        responseFightResultPacket2.setPrize(getPrize());
        return responseFightResultPacket2;
    }

    public static ResponseFightResultPacket2 from(ResponseFightResultPacket packet) {
        ResponseFightResultPacket2 responseFightResultPacket2 = new ResponseFightResultPacket2();
        responseFightResultPacket2.setKind(packet.getKind());
        responseFightResultPacket2.setGalaxyMapId(packet.getGalaxyMapId());
        responseFightResultPacket2.setTopAssaultUserId(packet.getTopAssaultUserId());
        responseFightResultPacket2.setGalaxyId(packet.getGalaxyId());
        responseFightResultPacket2.setTopAssaultModelName(packet.getTopAssaultModelName());
        responseFightResultPacket2.setTopAssaultCommander(packet.getTopAssaultCommander());
        responseFightResultPacket2.setTopAssaultOwner(packet.getTopAssaultOwner());
        responseFightResultPacket2.setTopAssaultValue(packet.getTopAssaultValue());
        responseFightResultPacket2.setTopAssaultBodyId(packet.getTopAssaultBodyId());
        responseFightResultPacket2.setVictory(packet.getVictory());
        responseFightResultPacket2.setAttackShipNumber(packet.getAttackShipNumber());
        responseFightResultPacket2.setAttackLossNumber(packet.getAttackLossNumber());
        responseFightResultPacket2.setDefendShipNumber(packet.getDefendShipNumber());
        responseFightResultPacket2.setDefendLossNumber(packet.getDefendLossNumber());
        responseFightResultPacket2.setKill(packet.getKill());
        responseFightResultPacket2.setExp(packet.getExp());
        responseFightResultPacket2.setPrize(packet.getPrize());
        return responseFightResultPacket2;
    }

}