package com.go2super.service.battle.calculator;

import com.go2super.database.entity.sub.battle.meta.AssaultCellAttackMeta;
import com.go2super.database.entity.sub.battle.meta.ShipCellAttackMeta;
import com.go2super.service.battle.module.ShipModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipUsage {

    private LinkedList<PacketUsage> usages = new LinkedList<>();
    private LinkedList<ModuleUsage> playback = new LinkedList<>();

    public boolean isUsed(ShipModule shipModule) {

        return playback.stream()
            .filter(module -> module.getModuleIndex() == shipModule.getModuleIndex())
            .findAny()
            .isPresent();
    }

    public void add(ShipModule module, double hitChance, int effectiveStack) {

        if (playback.stream().anyMatch(pb -> pb.getModuleIndex() == module.getModuleIndex())) {
            return;
        }

        playback.add(ModuleUsage.builder()
            .moduleIndex(module.getModuleIndex())
            .effectiveStack(effectiveStack)
            .fuelUsage(module.getFuelUsage())
            .reload(module.getReload())
            .lastShoot(module.getLastShoot())
            .build());

        Optional<PacketUsage> optionalUsage = usages.stream().filter(usage -> usage.getModuleId() == module.getModuleId()).findFirst();

        if (optionalUsage.isPresent()) {

            PacketUsage packetUsage = optionalUsage.get();

            packetUsage.setModuleIndex(module.getModuleIndex());
            packetUsage.setModuleNum(packetUsage.getModuleNum() + 1);
            packetUsage.setHitChance(hitChance);
            return;

        }

        PacketUsage packetUsage = PacketUsage.builder()
            .moduleIndex(module.getModuleIndex())
            .moduleId(module.getModuleId())
            .moduleNum(1)
            .hitChance(hitChance)
            .build();

        usages.add(packetUsage);

    }

    public int getSupply() {

        double supply = 0;
        for (ModuleUsage module : playback) {
            supply += module.getFuelUsage() * module.getEffectiveStack();
        }
        return (int) Math.ceil(supply);
    }

    public void map(ShipCellAttackMeta attackMeta, boolean target) {

        int partIndex = 0;

        LinkedList<PacketUsage> packetUsages = new LinkedList<>(usages);
        Collections.sort(packetUsages);
        // if(target) Collections.reverse(packetUsages);

        for (PacketUsage packetUsage : packetUsages) {

            if (partIndex > 6) {
                break;
            }

            if (target) {

                attackMeta.targetPartId[partIndex] = packetUsage.getModuleId();
                attackMeta.targetPartNum[partIndex] = packetUsage.getModuleNum();

            } else {

                attackMeta.sourcePartId[partIndex] = packetUsage.getModuleId();
                attackMeta.sourcePartRate[partIndex] = (int) (packetUsage.getHitChance() * 100.0);
                attackMeta.sourcePartNum[partIndex] = packetUsage.getModuleNum();

            }

            partIndex++;

        }

    }

    public void map(AssaultCellAttackMeta attackMeta) {

        int partIndex = 0;

        LinkedList<PacketUsage> packetUsages = new LinkedList<>(usages);
        // if(target) Collections.reverse(packetUsages);

        for (PacketUsage packetUsage : packetUsages) {

            if (partIndex > 6) {
                break;
            }

            attackMeta.sourcePartId[partIndex] = packetUsage.getModuleId();
            attackMeta.sourcePartRate[partIndex] = (int) (packetUsage.getHitChance() * 100.0);
            attackMeta.sourcePartNum[partIndex] = packetUsage.getModuleNum();

            partIndex++;

        }

    }

}
