package com.go2super.packet.corp;

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
public class ResponseConsortiaUpgradeCompletePacket extends Packet {

    public static final int TYPE = 1595;

    private int kind;
    private int consortiaId;
    private int propsCorpsPack;

    private UnsignedChar level = UnsignedChar.of(0);

    private byte storageLevel;
    private byte unionLevel;
    private byte shopLevel;

}
