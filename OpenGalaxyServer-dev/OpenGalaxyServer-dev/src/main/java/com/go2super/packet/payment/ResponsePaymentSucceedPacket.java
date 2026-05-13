package com.go2super.packet.payment;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponsePaymentSucceedPacket extends Packet {

    public static final int TYPE = 1910;

    private int credit;

}
