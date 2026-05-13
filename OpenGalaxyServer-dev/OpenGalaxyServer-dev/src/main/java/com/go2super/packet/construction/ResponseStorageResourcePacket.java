package com.go2super.packet.construction;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseStorageResourcePacket extends Packet {

    public static final int TYPE = 1237;

    private int gas;
    private int storageGas;

    private int metal;
    private int storageMetal;

    private int money;
    private int storageMoney;

}
