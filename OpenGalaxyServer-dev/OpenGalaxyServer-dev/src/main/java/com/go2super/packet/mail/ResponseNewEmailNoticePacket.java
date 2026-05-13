package com.go2super.packet.mail;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseNewEmailNoticePacket extends Packet {

    public static final int TYPE = 1622;

    private int errorCode;

}
