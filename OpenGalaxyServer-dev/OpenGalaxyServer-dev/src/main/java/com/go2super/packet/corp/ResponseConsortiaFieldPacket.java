package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaField;
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
public class ResponseConsortiaFieldPacket extends Packet {

    public static final int TYPE = 1581;

    private int dataLen;

    private List<ConsortiaField> data;

}
