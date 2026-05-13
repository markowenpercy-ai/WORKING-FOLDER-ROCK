package com.go2super.obj.entry;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@AllArgsConstructor
public class SmartPacket {

    private int type;
    private Class<? extends Packet> packetClass;

    @SneakyThrows
    public <T extends Packet> T create() {

        return (T) packetClass.newInstance();
    }

    public static SmartPacket of(int type, Class<? extends Packet> packetClass) {

        return new SmartPacket(type, packetClass);
    }

}