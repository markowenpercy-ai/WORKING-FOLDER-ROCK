package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaJobName;
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
public class ResponseConsortiaMySelfPacket extends Packet {

    public static final int TYPE = 1557;

    private int consortiaId;

    private long consortiaLeadUserId;
    private long cent;

    private SmartString name = SmartString.of(32);
    private SmartString notice = SmartString.of(256);
    private SmartString proclaim = SmartString.of(256);
    private SmartString consortiaLead = SmartString.of(32);

    private ConsortiaJobName jobName;

    private int consortiaGuid;
    private int sortId;
    private int wealth;
    private int repairWealth;

    private UnsignedChar memberCount = UnsignedChar.of(0);
    private UnsignedChar maxMemberCount = UnsignedChar.of(0);
    private UnsignedChar headId = UnsignedChar.of(0);
    private UnsignedChar level = UnsignedChar.of(0);
    private UnsignedChar holdGalaxy = UnsignedChar.of(0);
    private UnsignedChar maxHoldGalaxy = UnsignedChar.of(0);

    private byte storageLevel;
    private byte unionLevel;

    private int myWealth;
    private int upgradeTime;

    private byte upgradeType;
    private byte piratePassLevel;

    private UnsignedChar attackUserLevel = UnsignedChar.of(0);

    private byte pirateNum;

    private SmartString attackUser = SmartString.of(32);

    private int attackUserGalaxyId;
    private int attackUserAssault;

}
