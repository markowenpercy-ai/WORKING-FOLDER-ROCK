package com.go2super.obj.utility;

import lombok.Data;

@Data
public class WideString {

    private String value;
    private int size;

    public WideString(int size) {

        this.size = size;
    }

    public String noSpaces() {

        return value.replaceAll("\\s", "");
    }

    public WideString value(String value) {

        this.value = value;
        return this;
    }

    public String shrink(int length) {

        return value.substring(0, length);
    }

    public static WideString of(String value) {

        return new WideString(value.length()).value(value);
    }

    public static WideString of(String value, int size) {

        return new WideString(size).value(value);
    }

    public static WideString of(int size) {

        return new WideString(size);
    }

}
