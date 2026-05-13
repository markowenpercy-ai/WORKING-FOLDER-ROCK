package com.go2super.database.entity.sub;

import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class BattleMetadata {

    private Map<String, Integer> integers;
    private Map<String, String> strings;
    private Map<String, Boolean> booleans;
    private Map<String, Double> doubles;
    private Map<String, Float> floats;
    private Map<String, Long> longs;
    private Map<String, Short> shorts;
    private Map<String, Object> objects;

    public BattleMetadata() {

        integers = new java.util.HashMap<>();
        strings = new java.util.HashMap<>();
        booleans = new java.util.HashMap<>();
        doubles = new java.util.HashMap<>();
        floats = new java.util.HashMap<>();
        longs = new java.util.HashMap<>();
        shorts = new java.util.HashMap<>();
        objects = new java.util.HashMap<>();
    }

}
