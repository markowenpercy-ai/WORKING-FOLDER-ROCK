package com.go2super.socket.util;

import org.apache.commons.lang3.RandomUtils;

public class MathUtil {

    public static int toRealGuid(int guid) {

        return Long.valueOf(((long) guid * 2) / 512).intValue();
    }

    public static int toGuid(int guid) {

        return Long.valueOf(((long) guid * 512) / 2).intValue();
    }

    public static short[] toShortArray(int[] intArray) {

        short[] shortArray = new short[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            shortArray[i] = (short) intArray[i];
        }
        return shortArray;
    }

    public static int random(int from, int to) {

        return (int) (Math.random() * (to - from) + from);
    }

    public static int randomInclusive(int from, int to) {

        return (int) (Math.random() * (to - from + 1) + from);
    }

    public static boolean random() {

        return RandomUtils.nextBoolean();
    }

    public static long random64(long from, long to) {

        return RandomUtils.nextLong(from, to);
    }

    public static boolean isInRange(int value, int from, int to) {

        return value >= from && value <= to;
    }

}
