package com.go2super.packet;

import com.go2super.buffer.Go2Buffer;
import com.go2super.logger.BotLogger;
import com.go2super.obj.BufferObject;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.utility.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.mina.core.buffer.IoBuffer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.*;

public abstract class Packet {

    public static final List<String> blacklist = Arrays.asList("TYPE", "blacklist", "size", "creationTime", "responseTime", "millis");

    @Getter
    public int size = 0;
    @Getter
    public SmartServer smartServer;
    @Getter
    public Socket socket;

    @Getter
    @Setter
    public long creationTime = System.currentTimeMillis();
    @Getter
    @Setter
    public long responseTime = System.currentTimeMillis();

    @Getter
    @Setter
    public long millis = 0L;

    @SneakyThrows
    public Packet map(int size, int type, Go2Buffer go2Buffer, Socket socket, SmartServer smartServer) {

        for (Field field : FieldUtils.getAllFields(this.getClass())) {
            if (!blacklist.contains(field.getName())) {

                field.setAccessible(true);

                if (field.getName().equals("smartServer")) {
                    field.set(this, smartServer);
                    continue;
                }

                if (field.getName().equals("socket")) {
                    field.set(this, socket);
                    continue;
                }

                switch (field.getType().getSimpleName()) {
                    case "int":
                        field.set(this, go2Buffer.getInt());
                        break;
                    case "short":
                        field.set(this, (short) go2Buffer.getShort());
                        break;
                    case "byte":
                        field.set(this, go2Buffer.getBuffer().get());
                        break;
                    case "char":
                        char value = go2Buffer.getBuffer().getChar();
                        // int value = go2Buffer.getChar();
                        field.set(this, value);
                        break;
                    case "long":
                        field.set(this, go2Buffer.getInt64());
                        break;
                    case "SmartString":
                        SmartString smartString = (SmartString) field.get(this);
                        smartString.setValue(go2Buffer.getString(smartString.getSize()));
                        break;
                    case "WideString":
                        WideString wideString = (WideString) field.get(this);
                        wideString.setValue(go2Buffer.getWideChar(wideString.getSize()));
                        break;
                    case "UnsignedInteger":
                        UnsignedInteger unsignedInteger = (UnsignedInteger) field.get(this);
                        unsignedInteger.setValue(go2Buffer.getInt()); // todo
                        break;
                    case "UnsignedShort":
                        UnsignedShort unsignedShort = (UnsignedShort) field.get(this);
                        unsignedShort.setValue((short) go2Buffer.getUnsignShort()); // todo
                        break;
                    case "UnsignedChar":
                        UnsignedChar unsignedChar = (UnsignedChar) field.get(this);
                        if (unsignedChar != null) {
                            unsignedChar.setValue((char) go2Buffer.getUnsignChar()); // todo
                        }
                        break;
                    case "List":
                        List<? extends BufferObject> objects = (List) field.get(this);
                        // todo
                        break;
                    default:

                        if (BufferObject.class.isAssignableFrom(field.getType())) {

                            BufferObject bufferObject = (BufferObject) field.get(this);
                            bufferObject.read(go2Buffer);
                            break;

                        }

                        BotLogger.error("(ERROR) Pre-processor not found to map : " + field.getType().getSimpleName());
                        break;
                }

                // BotLogger.log(field.getName() + ", " + field.getType().toString() + ", " + field.get(this));

            }
        }


        return this;

    }

    @SneakyThrows
    public Go2Buffer unmap() {

        IoBuffer allocator = IoBuffer.allocate(8);
        allocator.order(ByteOrder.LITTLE_ENDIAN);
        allocator.setAutoExpand(true);

        Go2Buffer buffer = new Go2Buffer(allocator, false);

        int packetType = FieldUtils.getField(this.getClass(), "TYPE").getInt(this);

        buffer.setPacketName(this.getClass().getSimpleName());
        buffer.setType(packetType);

        buffer.addShort(0); // Size
        buffer.addShort(packetType); // Type

        for (Field field : FieldUtils.getAllFields(this.getClass())) {

            field.setAccessible(true);

            if (field.getName().equals("smartServer")) {
                continue;
            }

            if (field.getName().equals("socket")) {
                continue;
            }

            if (blacklist.contains(field.getName())) {
                continue;
            }

            switch (field.getType().getSimpleName()) {
                case "int":

                    buffer.addInt(field.getInt(this));
                    break;

                case "short":

                    buffer.addShort(field.getShort(this));
                    break;

                case "long":

                    buffer.addLong(field.getLong(this));
                    break;

                case "char":

                    buffer.addChar(field.getChar(this));
                    break;

                case "byte":

                    buffer.addByte(field.getByte(this));
                    break;

                case "SmartString":

                    SmartString smartString = (SmartString) field.get(this);
                    buffer.addString(smartString.getValue(), smartString.getSize());
                    break;

                case "WideString":

                    WideString wideString = (WideString) field.get(this);
                    buffer.addWideChar(wideString.getValue(), wideString.getSize());
                    break;

                case "UnsignedInteger":

                    UnsignedInteger unsignedInteger = (UnsignedInteger) field.get(this);
                    buffer.addUnsignInt(unsignedInteger.getValue());
                    break;

                case "UnsignedShort":

                    UnsignedShort unsignedShort = (UnsignedShort) field.get(this);
                    buffer.addUnsignShort(unsignedShort.getValue());
                    break;

                case "UnsignedChar":

                    UnsignedChar unsignedChar = (UnsignedChar) field.get(this);
                    buffer.addUnsignChar(unsignedChar.getValue());
                    break;

                case "List":

                    List<BufferObject> objects = new CopyOnWriteArrayList<>((List) field.get(this));

                    if (field.isAnnotationPresent(Trash.class)) {
                        Trash trash = field.getAnnotation(Trash.class);
                        ParameterizedType type = (ParameterizedType) field.getGenericType();
                        Class<? extends BufferObject> clazz = (Class<? extends BufferObject>) type.getActualTypeArguments()[0];
                        BufferObject instance = clazz.newInstance();
                        while (objects.size() < trash.length()) {
                            objects.add(instance.trash());
                        }
                    }

                    for (BufferObject bufferObject : objects) {
                        bufferObject.write(buffer);
                    }

                    break;

                default:

                    if (BufferObject.class.isAssignableFrom(field.getType())) {

                        BufferObject bufferObject = (BufferObject) field.get(this);
                        bufferObject.write(buffer);
                        break;

                    }

                    BotLogger.error("(ERROR) Pre-processor not found to map : " + field.getType().getSimpleName());
                    break;

            }

        }

        buffer.getBuffer().putShort(0, (short) buffer.getCalculatedSize());

        Go2Buffer minimalisticPacket = new Go2Buffer(buffer.getCalculatedSize());
        byte[] array = buffer.getBuffer().array();

        for (int i = 0; i < buffer.getCalculatedSize(); i++) {
            minimalisticPacket.getBuffer().put(array[i]);
        }

        minimalisticPacket.setType(buffer.getType());
        minimalisticPacket.setPacketName(buffer.getPacketName());

        minimalisticPacket.getBuffer().position(0);
        return minimalisticPacket;

    }

    public void reply(List<? extends Packet> packets) {

        for (Packet packet : packets) {
            reply(packet);
        }
    }

    public void reply(Packet... packets) {

        for (Packet packet : packets) {
            reply(packet);
        }
    }

    public void reply(Packet packet) {

        packet.responseTime = System.currentTimeMillis() - creationTime;
        getSmartServer().send(packet);
    }

    @SneakyThrows
    public int getType() {

        return FieldUtils.getField(this.getClass(), "TYPE").getInt(this);
    }

    public int getCustomSize() {

        return 0;
    }

    public String getUserIp() {

        return socket.getRemoteSocketAddress().toString();
    }

}
