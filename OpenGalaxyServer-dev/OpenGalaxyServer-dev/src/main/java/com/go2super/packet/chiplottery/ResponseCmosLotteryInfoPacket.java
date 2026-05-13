package com.go2super.packet.chiplottery;

import com.go2super.obj.game.CmosInfo;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseCmosLotteryInfoPacket extends Packet {

    public static final int TYPE = 1903;

    private int pirateMoney;
    private byte lotteryPhase;
    private UnsignedChar cmosPackCount = UnsignedChar.of(0);
    private short dataLen;

    private List<CmosInfo> data = new ArrayList<>();

}
