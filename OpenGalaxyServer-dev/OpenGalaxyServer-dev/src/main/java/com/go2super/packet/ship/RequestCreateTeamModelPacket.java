package com.go2super.packet.ship;

import com.go2super.obj.game.TeamModel;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCreateTeamModelPacket extends Packet {

    public static final int TYPE = 1373;

    private int seqId;
    private int guid;

    private int indexId;
    private int delFlag;

    private TeamModel teamModel = new TeamModel();

}
