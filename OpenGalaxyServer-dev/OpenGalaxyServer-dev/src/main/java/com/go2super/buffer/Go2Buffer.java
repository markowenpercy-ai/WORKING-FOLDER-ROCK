package com.go2super.buffer;

import com.go2super.obj.utility.SmartString;
import com.go2super.socket.util.BufferUtil;
import lombok.Data;
import org.apache.mina.core.buffer.IoBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

@Data
public class Go2Buffer {

    public static boolean WEIRD_LOG = true;
    private IoBuffer buffer;

    private String packetName;

    private int size;
    private int type;

    private int calculatedSize = 0;

    public Go2Buffer(int allocate) {

        this.size = allocate;
        this.buffer = IoBuffer.allocate(allocate);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public Go2Buffer(IoBuffer ioBuffer, boolean fetchHeader) {

        this.buffer = ioBuffer;
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);

        if (!fetchHeader) {
            return;
        }

        this.size = getMsgSize();
        this.type = getMsgType();

    }

    public Go2Buffer(ByteBuffer byteBuffer, boolean fetchHeader) {

        this.buffer = IoBuffer.wrap(byteBuffer);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);

        if (!fetchHeader) {
            return;
        }

        this.size = getMsgSize();
        this.type = getMsgType();

    }

    public int position() {

        return this.getBuffer().position();
    }

    public int limit() {

        return size();
    }

    public int size() {

        return this.getBuffer().limit();
    }

    public Go2Buffer setId(int id) {

        this.type = id;
        return addShort(this.size).addShort(id);
    }

    public Go2Buffer addByte(byte number) {

        this.buffer.put(number);
        this.calculatedSize++;
        return this;
    }

    public Go2Buffer addInt(int number) {

        this.buffer.putInt(number);
        this.calculatedSize += 4;
        return this;
    }

    public Go2Buffer addUnsignInt(int number) {

        buffer.putUnsignedInt(number);
        this.calculatedSize += 4;
        return this;
    }

    public Go2Buffer addUnsignChar(int number) {

        buffer.put(buffer.position(), (byte) number);
        buffer.position(buffer.position() + 1);
        this.calculatedSize++;
        return this;
    }

    public Go2Buffer addChar(char number) {

        buffer.put(buffer.position(), (byte) number);
        buffer.position(buffer.position() + 1);
        this.calculatedSize++;
        return this;
    }

    public Go2Buffer addChar(int number) {

        char c = (char) number;
        buffer.put(buffer.position(), (byte) c);
        buffer.position(buffer.position() + 1);
        this.calculatedSize++;
        return this;
    }

    public Go2Buffer addShort(int number) {
        // buffer.putShort((short) number);
        int _loc3_ = buffer.position();
        buffer.put(_loc3_, (byte) (number & 0x00FF));
        buffer.put(_loc3_ + 1, (byte) (number >> 8));
        buffer.position(buffer.position() + 2);
        this.calculatedSize += 2;
        return this;
    }

    public Go2Buffer addLong(long number) {

        this.buffer.putLong(number);
        this.calculatedSize += 8;
        return this;
    }

    public int getChar() {

        return this.buffer.get();
    }


    public Go2Buffer addWideChar(String str, int limit) {

        int _loc4_ = 0;
        while (_loc4_ < str.length() && _loc4_ < limit) {
            addShort(str.charAt(_loc4_));
            _loc4_++;
        }
        this.pushByte((limit - str.length()) * 2);
        return this;
    }

    public Go2Buffer pushByte(int value) {

        if (value <= 0) {
            return this;
        }
        int _loc3_ = buffer.position();
        int _loc4_ = 0;
        while (_loc4_ < value) {
            this.calculatedSize++;
            buffer.put(_loc3_ + _loc4_, (byte) 0);
            _loc4_++;
        }
        buffer.position(buffer.position() + value);
        return this;
    }

    public Go2Buffer addString(SmartString str) {

        return addString(str.getValue(), str.getSize());
    }

    public Go2Buffer addString(String str, int limit) {

        byte[] bytes = str.getBytes();
        int length = bytes.length;
        buffer.put(bytes);
        this.calculatedSize += bytes.length;
        if (length < limit) {
            for (int i = length; i < limit; i++) {
                buffer.put((byte) 0);
                this.calculatedSize++;
            }
        }
        return this;
    }

    public int getShort() {

        return this.buffer.getShort();
    }

    public Go2Buffer addUnsignShort(int number) {

        int _loc3_ = buffer.position();
        buffer.put(_loc3_, (byte) (number & 255));
        buffer.put(_loc3_ + 1, (byte) (number >> 8 & 255));
        buffer.position(buffer.position() + 2);
        this.calculatedSize += 2;
        return this;
    }

    public int getUnsignShort() {

        int _loc2_ = buffer.get();
        int _loc3_ = buffer.get();
        if (_loc2_ < 0) {
            _loc2_ = _loc2_ + 256;
        }
        if (_loc3_ < 0) {
            _loc3_ = _loc3_ + 256;
        }
        return (_loc3_ & 255) << 8 | _loc2_ & 255;
    }

    public int getInt() {
        // BotLogger.log("BUFFER");
        return this.buffer.getInt();
    }

    public long getInt64() {

        return this.buffer.getLong();
    }

    public int getMsgSize() {

        if (this.buffer.limit() <= 2) {
            return 0;
        }
        return getUnsignShort();
    }

    public int getMsgType() {

        if (this.buffer.limit() - this.buffer.position() < 2) {
            return 0;
        }
        return getUnsignShort();
    }

    public int getUnsignChar() {

        int _loc2_ = buffer.get();
        if (_loc2_ < 0) {
            _loc2_ = _loc2_ + 256;
        }
        return _loc2_;
    }

    public String getWideChar(int limit) {

        int _loc3_ = 0;
        String _loc4_ = "";
        int _loc5_ = 0;
        while (_loc5_ < limit) {
            _loc3_ = this.getShort();
            if (_loc3_ == 0) {
                break;
            }
            if (_loc3_ < 0) {
                continue;
            }
            _loc4_ = _loc4_ + fromCharCode(_loc3_);
            _loc5_++;
        }
        _loc5_++;
        while (_loc5_ < limit) {
            this.getShort();
            _loc5_++;
        }
        return _loc4_;
    }

    public String getString(int limit) {

        boolean finish = false;
        byte[] bytes = new byte[limit];
        for (int i = 0; i < limit; i++) {
            byte get = buffer.get();
            if (get == 0) {
                // BotLogger.log(get);
                finish = true;
                continue;
            }
            bytes[i] = get;
        }
        // BufferUtil.printBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public Go2Buffer getByte(int param2) {

        int _loc3_ = 0;
        while (_loc3_ < param2) {
            buffer.get();
            _loc3_++;
        }
        return this;
    }

    public static String fromCharCode(int... codePoints) {

        return new String(codePoints, 0, codePoints.length);
    }

}
