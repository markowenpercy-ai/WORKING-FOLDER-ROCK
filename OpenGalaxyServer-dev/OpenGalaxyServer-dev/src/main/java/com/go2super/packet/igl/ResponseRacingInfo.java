package com.go2super.packet.igl;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseRacingInfo extends Packet {

    private int rankId;
    private int rewardValue;
    public byte racingNum;
    public byte racingRewardFlag;
    public byte enemyLen;
    public byte reportLen;
    public long userId;

}
