package com.go2super.obj.utility;

import lombok.Data;

@Data
public class UnsignedInteger {

    private int value;

    public UnsignedInteger(int value) {

        this.value = value;
    }

    public static UnsignedInteger of(int value) {

        return new UnsignedInteger(value);
    }

    public static UnsignedInteger of(long value) {

        return new UnsignedInteger(Long.valueOf(value).intValue());
    }

}
