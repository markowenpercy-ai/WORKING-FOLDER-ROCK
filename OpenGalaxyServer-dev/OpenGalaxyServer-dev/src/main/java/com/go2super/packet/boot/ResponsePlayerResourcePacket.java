package com.go2super.packet.boot;

import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponsePlayerResourcePacket extends Packet {

    public static final int TYPE = 1214;

    private UnsignedInteger userGas;
    private UnsignedInteger userMetal;
    private UnsignedInteger userMoney;
    private UnsignedInteger credit;

    private int level;
    private int exp;
    private int coins;
    private int outGas;
    private int outMetal;
    private int outMoney;
    private int maxSpValue;
    private int spValue;
    private int moneyBuyNum;
    private int defyEctypeNum;
    private int matchCount;
    private int tollGate;
    private int reserve;

}
