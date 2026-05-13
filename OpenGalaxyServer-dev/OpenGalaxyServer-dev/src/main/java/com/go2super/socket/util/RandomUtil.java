package com.go2super.socket.util;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class RandomUtil {

    private static final Collector<?, ?, ?> SHUFFLER = Collectors.collectingAndThen(
        Collectors.toCollection(ArrayList::new),
        list -> {
            Collections.shuffle(list);
            return list;
        }
    );

    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, List<T>> toShuffledList() {

        return (Collector<T, ?, List<T>>) SHUFFLER;
    }

    public static int getRandomInt() {

        return ThreadLocalRandom.current().nextInt(0, Integer.MAX_VALUE);
    }

    public static int getRandomInt(int max) {

        return ThreadLocalRandom.current().nextInt(0, max);
    }

    public static int getSmartRandomInt(int min, int max) {

        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static int getRandomInt(int min, int max) {

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static long getRandomLong(long max) {

        return ThreadLocalRandom.current().nextLong(0, max);
    }

    public static long getRandomLong() {

        return ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
    }

}
