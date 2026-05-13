package com.go2super.packet.corp;

import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConsortiaProclaimPacket extends Packet {

    public static final int TYPE = 1553;

    private int consortiaId;

    private long consortiaLeadUserId;
    private long cent;

    private int consortiaLeadGuid;

    private SmartString consortiaLead = SmartString.of(32);
    private SmartString proclaim = SmartString.of(256);

    private UnsignedChar memberCount = UnsignedChar.of(0);
    private UnsignedChar maxMemberCount = UnsignedChar.of(0);
    private UnsignedChar consortiaLevel = UnsignedChar.of(0);
    private UnsignedChar HeadId = UnsignedChar.of(0);

    private byte limitJoin;

}
