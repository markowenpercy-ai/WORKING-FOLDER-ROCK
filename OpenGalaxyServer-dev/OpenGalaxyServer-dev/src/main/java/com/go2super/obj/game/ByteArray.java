package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;

public class ByteArray extends BufferObject {

    private final byte[] array;

    public ByteArray(byte[] array) {

        this.array = array;
    }

    public ByteArray(int[] array) {

        this.array = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            this.array[i] = (byte) array[i];
        }
    }

    public ByteArray(int capacity) {

        this.array = new byte[capacity];
    }

    @Override
    public void read(Go2Buffer buffer) {

        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getBuffer().get();
        }

    }

    @Override
    public void write(Go2Buffer buffer) {

        for (byte value : array) {
            buffer.addByte(value);
        }

    }

}
