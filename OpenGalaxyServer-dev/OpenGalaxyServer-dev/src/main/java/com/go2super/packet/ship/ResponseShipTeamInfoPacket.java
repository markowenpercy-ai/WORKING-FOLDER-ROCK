package com.go2super.packet.ship;

import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseShipTeamInfoPacket extends Packet {

    public static final int TYPE = 1372;

    private int shipTeamId;
    private long userId;

    private UnsignedInteger gas;
    private int commanderId;

    private SmartString teamName = SmartString.of(32);
    private SmartString commanderName = SmartString.of(32);
    private SmartString teamOwner = SmartString.of(32);
    private SmartString consortia = SmartString.of(32);

    private ShipTeamBody teamBody;

    private short skillId;

    private byte attackObjInterval;
    private byte attackObjType;

    private UnsignedChar levelId;
    private byte cardLevel;

}
