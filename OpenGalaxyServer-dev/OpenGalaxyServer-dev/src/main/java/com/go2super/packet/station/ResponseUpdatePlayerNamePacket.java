package com.go2super.packet.station;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUpdatePlayerNamePacket extends Packet {

    public static final int TYPE = 1025;

    private byte errorCode;

}
