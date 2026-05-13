package com.go2super.packet.corp;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConsortiaPirateBroPacket extends Packet {

    public static final int TYPE = 1656;

    private int flag;
    private int galaxyId;
    private int pirateLevelId;
    private int consortiaId;

    private SmartString consortiaName = SmartString.of(32);

}
