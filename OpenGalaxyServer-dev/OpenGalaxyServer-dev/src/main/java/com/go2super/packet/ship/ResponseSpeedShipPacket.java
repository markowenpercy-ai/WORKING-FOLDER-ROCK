package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSpeedShipPacket extends Packet {


    public static final int TYPE = 1367;

    private int errorCode;

    private int indexSpareTime;
    private int spareTime;


}
