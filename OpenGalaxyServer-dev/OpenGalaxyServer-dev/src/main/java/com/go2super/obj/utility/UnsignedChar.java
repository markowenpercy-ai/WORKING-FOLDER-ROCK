package com.go2super.obj.utility;

import lombok.Data;

@Data
public class UnsignedChar {

    private char value;

    public UnsignedChar(char value) {

        this.value = value;
    }

    public UnsignedChar(int value) {

        this.value = (char) value;
    }

    public String toString() {

        return String.valueOf((int) value);
    }

    public static UnsignedChar of(int value) {

        return new UnsignedChar(value);
    }

    public static UnsignedChar of(short value) {

        return new UnsignedChar(value);
    }

}
