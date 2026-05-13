package com.go2super.packet.mail;

import com.go2super.obj.game.EmailInfo;
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
public class ResponseEmailInfoPacket extends Packet {

    public static final int TYPE = 1609;

    private short dataLen;
    private short emailCount;

    private List<EmailInfo> data;


}
