package com.go2super.obj.entry;

import com.go2super.packet.Packet;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
public class SmartListener {

    private Class<? extends Packet> packetClass;
    private PacketProcessor packetProcessor;

    private Method packetMethod;
    private PacketListener instance;

    public static SmartListener of(Class<? extends Packet> packetClass, PacketProcessor processor, Method packetMethod, PacketListener instance) {

        return new SmartListener(packetClass, processor, packetMethod, instance);
    }

}
