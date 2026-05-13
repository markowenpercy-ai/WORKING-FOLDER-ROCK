package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseCommanderChangeCardPacket extends Packet {

    public static final int TYPE = 1525;

    private int commanderId;
    private int propsId;
    private int lockFlag;

    private int usePropsId;
    private int useLockFlag;


}
