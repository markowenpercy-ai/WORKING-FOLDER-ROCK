package com.go2super.packet.props;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseUsePropsPacket extends Packet {

    public static final int TYPE = 1056;

    private int propsId;

    private byte lockFlag;
    private byte errorCode;

    private byte awardFlag;
    private byte awardLockFlag;

    private int awardGas;
    private int awardMoney;
    private int awardMetal;

    private int awardPropsLen;
    private IntegerArray awardPropsId = new IntegerArray(10);
    private IntegerArray awardPropsNum = new IntegerArray(10);

    private int moveHomeFlag;
    private int toGalaxyMapId = -1;
    private int toGalaxyId = -1;

    private int number;
    private int spValue;

    private int awardCoins;
    private int awardBadge;
    private int awardHonor;
    private int awardActiveCredit;
    private int pirateMoney;

}
