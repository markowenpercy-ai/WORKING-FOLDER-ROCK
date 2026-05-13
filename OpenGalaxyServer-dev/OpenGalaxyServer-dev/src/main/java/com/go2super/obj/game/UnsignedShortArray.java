package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class UnsignedShortArray extends BufferObject {

    private int[] array;

    public UnsignedShortArray(int[] array) {

        this.array = array;
    }

    public UnsignedShortArray(int capacity) {

        this.array = new int[capacity];
    }

    public void add(int... values) {

        System.arraycopy(values, 0, array, 0, values.length);
    }

    public void set(int pos, int value) {

        array[pos] = value;
    }

    @Override
    public void read(Go2Buffer buffer) {

        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getUnsignShort();
        }

    }

    @Override
    public void write(Go2Buffer buffer) {

        for (int value : array) {
            buffer.addUnsignShort(value);
        }

    }

}
