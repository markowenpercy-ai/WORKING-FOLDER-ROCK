package com.go2super.packet.ship;

import com.go2super.obj.game.ViewTeamModel;
import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseViewJumpShipTeam extends Packet {

    public static final int TYPE = 1408;

    private ViewTeamModel teamModel = new ViewTeamModel();

    private long userId;
    private long commanderUserId;

    private SmartString teamName = SmartString.of(32);
    private SmartString teamOwner = SmartString.of(32);

    private int shipTeamId;

    private short aim;
    private short blench;
    private short priority;
    private short electron;
    private short skillId;

    private byte cardLevel;

}
