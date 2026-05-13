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
public class ResponseDeletePropsPacket extends Packet {

    public static final int TYPE = 1060;

    private int propsId;
    private byte lockFlag;

}
