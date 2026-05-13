package com.go2super.packet.ship;

import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestEditShipTeamPacket extends Packet {

    public static final int TYPE = 1320;

    private int seqId;
    private int guid;

    private int shipTeamId;

    private SmartString name = SmartString.of(32);
    private ShipTeamBody teamBody = new ShipTeamBody();

    private int commanderId;

    private byte preference;
    private byte range;

    private byte editType;

}
