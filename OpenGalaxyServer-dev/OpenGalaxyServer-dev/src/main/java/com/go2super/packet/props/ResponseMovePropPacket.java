package com.go2super.packet.props;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMovePropPacket extends Packet {

    public static final int TYPE = 1053;

    private int kind;
    private int propId;
    private int propNum;
    private int lockFlag;


}
