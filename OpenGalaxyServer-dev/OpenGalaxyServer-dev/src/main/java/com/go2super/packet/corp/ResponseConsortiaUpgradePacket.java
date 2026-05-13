package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConsortiaUpgradePacket extends Packet {

    public static final int TYPE = 1584;

    private int kind;

    private int consortiaId;
    private int needTime;

}
