package com.go2super.packet.science;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseGainLotteryPacket extends Packet {

    public static final int TYPE = 1083;

    private int guid;
    private long userId;
    private SmartString name = SmartString.of(32);
    private int kind;
    private int lotteryId;
    private int lotteryType;
    private int propsId;
    private int num;
    private int coins;
    private int metal;
    private int gas;
    private int money;
    private int broFlag;
    private int lockFlag;

}
