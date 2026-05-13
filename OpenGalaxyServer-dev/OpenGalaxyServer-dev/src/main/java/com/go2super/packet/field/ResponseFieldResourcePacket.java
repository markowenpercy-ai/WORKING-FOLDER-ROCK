package com.go2super.packet.field;

import com.go2super.obj.game.FieldResource;
import com.go2super.obj.game.IntegerArray;
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
public class ResponseFieldResourcePacket extends Packet {

    public static final int TYPE = 1801;

    private int galaxyMapId;
    private int galaxyId;
    private int consortiaPer;

    private byte friendFlag;
    private byte fieldCenterStatus;

    private byte techPerMetal;
    private byte techPerGas;
    private byte techPerMoney;

    private byte propsPerMetal;
    private byte propsPerGas;
    private byte propsPerMoney;

    private List<FieldResource> data = new ArrayList<>();

    private int fieldCenterTime;
    private IntegerArray helpGuid;

}
