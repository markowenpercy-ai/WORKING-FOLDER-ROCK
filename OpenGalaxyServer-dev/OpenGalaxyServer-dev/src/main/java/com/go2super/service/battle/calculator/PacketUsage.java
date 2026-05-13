package com.go2super.service.battle.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacketUsage implements Comparable<PacketUsage> {

    private int moduleIndex;
    private int moduleId;
    private int moduleNum;

    private double hitChance;

    @Override
    public int compareTo(PacketUsage other) {

        return other.getModuleIndex() > moduleIndex ? -1 : 1;
    }

}
