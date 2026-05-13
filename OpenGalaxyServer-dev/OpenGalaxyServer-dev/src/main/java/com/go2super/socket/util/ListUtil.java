package com.go2super.socket.util;

import java.util.*;

public class ListUtil {

    public static int[] toArray(List<Integer> integers) {

        int[] array = new int[integers.size()];

        for (int i = 0; i < array.length; i++) {
            array[i] = integers.get(i);
        }

        return array;

    }

}
