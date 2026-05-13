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
public class ResponseRefreshWallPacket extends Packet {

    public static final int TYPE = 1088;

    private int kind; //<- type

    private int propsId;
    private int num;

}
