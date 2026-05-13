package com.go2super.packet.fight;

import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseWarFieldPlayerList extends Packet {
    public static final int TYPE = 1464;
    public UnsignedChar roomId;
    public UnsignedChar reserve;
    public UnsignedChar dataLen;
    public UnsignedChar attackerNum;
    public List<WarfieldPlayer> data = new ArrayList<>();
}
