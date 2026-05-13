package com.go2super.packet.construction;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseGetStorageResourcePacket extends Packet {

    public static final int TYPE = 1233;

    private int gas;
    private int metal;
    private int money;

}
