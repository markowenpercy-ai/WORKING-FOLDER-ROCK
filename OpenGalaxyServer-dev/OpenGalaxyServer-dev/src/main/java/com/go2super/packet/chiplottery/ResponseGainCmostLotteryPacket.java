package com.go2super.packet.chiplottery;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseGainCmostLotteryPacket extends Packet {

    public static final int TYPE = 1901;

    private int guid;

    private int lotteryId;
    private int propsId;
    private int type;
    private int credit;

    private byte lotteryPhase;
    private byte broFlag;

    private SmartString name = SmartString.of(32);

}
