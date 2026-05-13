package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

import java.util.*;

@Data
public class ShortArray extends BufferObject {

    private int[] array;

    public ShortArray(int capacity, int fill) {

        this.array = new int[capacity];
        for (int i = 0; i < this.array.length; i++) {
            this.array[i] = fill;
        }
    }

    public ShortArray(int length) {

        this.array = new int[length];
    }

    public ShortArray(int[] array) {

        this.array = array;
    }

    public int get(int position) {

        return array[position];
    }

    public List<Integer> saveList() {

        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }

        return list;

    }

    public int getLength() {

        return array.length;
    }

    @Override
    public void read(Go2Buffer buffer) {

        for (int i = 0; i < array.length; i++) {
            array[i] = (short) buffer.getShort();
        }

    }

    @Override
    public void write(Go2Buffer buffer) {

        for (int i = 0; i < array.length; i++) {
            buffer.addShort(array[i]);
        }

    }

}
