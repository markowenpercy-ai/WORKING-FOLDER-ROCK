package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaAuthUser;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConsortiaAuthUserPacket extends Packet {

    public static final int TYPE = 1575;

    private int dataLen;
    private int pageCount;

    private List<ConsortiaAuthUser> data;

}
