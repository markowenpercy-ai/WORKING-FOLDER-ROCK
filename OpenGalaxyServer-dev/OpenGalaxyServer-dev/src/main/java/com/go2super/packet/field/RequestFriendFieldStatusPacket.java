package com.go2super.packet.field;

import com.go2super.obj.game.LongArray;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestFriendFieldStatusPacket extends Packet {

    public static final int TYPE = 1814;

    private int seqId;
    private int guid;

    // Kind:
    // 0 = Game Friends, that means data will be guid's
    // 1 = FB Friends, that means data will be userId's
    private short kind;
    private short dataLen;

    private LongArray data = new LongArray(6);


}
