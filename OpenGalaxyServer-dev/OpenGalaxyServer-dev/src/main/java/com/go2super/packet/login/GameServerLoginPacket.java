package com.go2super.packet.login;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class GameServerLoginPacket extends Packet {

    public static final int TYPE = 505;

    // 0 = Server enabled (please end login tol)
    // 1 = Server not exists
    // 2 = Max registrations reached
    // 3 = Maintenance
    // 4 = Full
    // 5 = Not Activated
    // 6 = Server change processing data
    // 7 = Ban
    private byte error;

    private short reserve;
    private byte trash;

    private int guid;
    private int guide;

}
