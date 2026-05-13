package com.go2super.logger;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BionicChip;
import com.go2super.logger.lookup.UserCommanderLookup;
import com.go2super.logger.lookup.UserInventoryLookup;
import com.go2super.logger.lookup.sub.*;
import com.go2super.obj.game.Prop;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.service.CommanderService;

import java.util.*;

public class Lookup {

    public static UserCommanderLookup getUserCommanders(User user) {

        UserCommanderLookup userCommanderLookup = UserCommanderLookup.builder()
            .commanders(new ArrayList<>())
            .build();

        List<Commander> commanders = CommanderService.getInstance().getCommanders(user);
        for (Commander commander : commanders) {
            userCommanderLookup.getCommanders().add(getCommander(commander));
        }

        return userCommanderLookup;

    }

    public static CommanderLookup getCommander(Commander commander) {

        CommanderLookup commanderLookup = CommanderLookup.builder()
            .userId(commander.getUserId())
            .commanderId(commander.getCommanderId())
            .shipTeamId(commander.getShipTeamId())
            .commanderName(commander.getName())
            .level(commander.getLevel().getLevel())
            .experience(commander.getExperience())
            .stars(commander.getStars())
            .variance(commander.getVariance())
            .growthElectron(commander.getGrowthElectron())
            .growthAccuracy(commander.getGrowthAim())
            .growthSpeed(commander.getGrowthSpeed())
            .growthDodge(commander.getGrowthDodge())
            .chips(new ArrayList<>())
            .gems(new ArrayList<>())
            .build();

        for (BionicChip bionicChip : commander.getChips()) {

            CommanderChipLookup chipLookup = CommanderChipLookup.builder()
                .chipName("unknown")
                .chipId(bionicChip.getChipId())
                .chipSlot(bionicChip.getHoleId())
                .build();

            PropData propData = bionicChip.getPropData();
            if (propData != null) {
                chipLookup.setChipName(propData.getName());
            }

            commanderLookup.getChips().add(chipLookup);

        }

        for (Integer gem : commander.getGems()) {

            CommanderGemLookup gemLookup = CommanderGemLookup.builder()
                .gemName("unknown")
                .gemId(gem)
                .build();

            PropData propData = ResourceManager.getProps().getData(gem);
            if (propData != null) {
                gemLookup.setGemName(propData.getName());
            }

            commanderLookup.getGems().add(gemLookup);

        }

        return commanderLookup;

    }

    public static UserInventoryLookup getUserInventories(User user) {

        InventoryLookup userInventory = InventoryLookup.builder()
            .propLookups(new ArrayList<>())
            .build();

        InventoryLookup corpInventory = InventoryLookup.builder()
            .propLookups(new ArrayList<>())
            .build();

        if (user.getInventory() != null && user.getInventory().getPropList() != null) {
            for (Prop prop : user.getInventory().getPropList()) {

                PropData propData = prop.getData();
                String propName = propData != null ? propData.getName() : "unknown";

                PropLookup propLookup = PropLookup.builder()
                    .propName(propName)
                    .propId(prop.getPropId())
                    .propUnlockedNum(prop.getPropNum())
                    .propLockedNum(prop.getPropLockNum())
                    .build();

                userInventory.getPropLookups().add(propLookup);

            }
        }

        if (user.getCorpInventory() != null && user.getCorpInventory().getCorpPropList() != null) {
            for (Prop prop : user.getCorpInventory().getCorpPropList()) {

                PropData propData = prop.getData();
                String propName = propData != null ? propData.getName() : "unknown";

                PropLookup propLookup = PropLookup.builder()
                    .propName(propName)
                    .propId(prop.getPropId())
                    .propUnlockedNum(prop.getPropNum())
                    .propLockedNum(prop.getPropLockNum())
                    .build();

                corpInventory.getPropLookups().add(propLookup);

            }
        }

        UserInventoryLookup userInventoryLookup = UserInventoryLookup.builder()
            .userInventory(userInventory)
            .corpInventory(corpInventory)
            .build();

        return userInventoryLookup;

    }

}
