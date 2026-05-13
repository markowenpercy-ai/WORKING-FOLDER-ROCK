package com.go2super.packet.ship;

import com.go2super.obj.game.TeamModel;
import com.go2super.obj.utility.VariableType;
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
public class ResponseTeamModelInfoPacket extends Packet {

    public static final int TYPE = 1374;

    private int dataLen;

    private List<TeamModel> teamModel = new ArrayList<>(VariableType.MAX_TEAMMODEL);


}
