package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class LongArray extends BufferObject {

    private long[] array;

    public LongArray(long[] array) {

        this.array = array;
    }

    public LongArray(int capacity) {

        this.array = new long[capacity];
    }

    public void add(long... values) {

        System.arraycopy(values, 0, array, 0, values.length);
    }

    public void set(int pos, long value) {

        array[pos] = value;
    }

    @Override
    public void read(Go2Buffer buffer) {

        for (int i = 0; i < array.length; i++) {
            array[i] = buffer.getInt64();
        }

    }

    @Override
    public void write(Go2Buffer buffer) {

        for (long value : array) {
            buffer.addLong(value);
        }

    }

}
