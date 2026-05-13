package com.go2super.obj.utility;

import lombok.Data;

@Data
public class UnsignedShort {

    private short value;

    public UnsignedShort(short value) {

        this.value = value;
    }

    public UnsignedShort(int value) {

        this.value = (short) value;
    }

    public int toInt() {

        return value;
    }

    public static UnsignedShort of(int value) {

        return new UnsignedShort(value);
    }

    public static UnsignedShort of(short value) {

        return new UnsignedShort(value);
    }

}
