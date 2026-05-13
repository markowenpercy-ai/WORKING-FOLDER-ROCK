package com.go2super.packet.igl;

import com.go2super.obj.game.RacingEnemyInfo;
import com.go2super.obj.game.RacingReportInfo;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;


@Data
public class ResponseRacingInformationPacket extends Packet {

    public static final int TYPE = 1851;

    private UnsignedInteger rankId;
    private int rewardValue;
    private UnsignedChar racingNum;
    private byte racingRewardFlag;
    private UnsignedChar enemyLen;
    private UnsignedChar reportLen;
    private long userId;

    private List<RacingEnemyInfo> enemyInfo = new ArrayList<>();
    private List<RacingReportInfo> reportInfo = new ArrayList<>();

}
