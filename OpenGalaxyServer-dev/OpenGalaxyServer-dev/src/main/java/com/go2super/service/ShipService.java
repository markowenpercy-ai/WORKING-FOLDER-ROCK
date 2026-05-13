package com.go2super.service;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserShipUpgrades;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartLevelMeta;
import com.go2super.resources.json.ShipBodyJson;
import com.go2super.resources.json.ShipPartJson;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShipService {

    private static ShipService instance;

    private final ShipBodyJson bodyStats;
    private final ShipPartJson partStats;

    private final List<String> legalBody = List.of(
        "body:weikes", "body:typhoon", "body:estrella",
        "body:airWanderer", "body:bombardier", "body:nettle",
        "body:valkyrie", "body:duke", "body:diaz",
        "body:goGetter", "body:shuttler", "body:rv766TheExplorer",
        "body:spaceHunter", "body:watchman", "body:palenka",
        "body:sparrow", "body:spinner", "body:howler",
        "body:devourer", "body:wraith", "body:whirlpool",
        "body:polymesus", "body:encratos", "body:cerberus",
        "body:cybra", "body:nicholas", "body:genesis",
        "body:hamdar", "body:helena", "body:tiamat",
        "body:daybreak", "body:quickAssault", "body:aggressiveWarlord",
        "body:lastStand", "body:strikingSword", "body:allianceAdmiral",
        "body:shadowGuardian", "body:nihelbet", "body:presidoOfGlory",

        "body:industrialShip",
        "body:fleetfoot",
        "body:hedgehog",
        "body:bolenciaWarship",
        "body:independence",
        "body:blackHole",

        "body:intrepidNexus",
        "body:grimReaper",
        "body:shadowTrojan",
        "body:mercuryWing",
        "body:firecat",
        "body:gForcesDreadnaught",
        "body:arbiter",
        "body:gfsVengeance",
        "body:zeframMk42",
        "body:kazati"
    );
    private final int replaceWithBody;
    private final List<Integer> replaceWithBodyParts;

    private final List<List<String>> illegalParts = List.of(
        List.of( // heat bali
            "part:gladiusArc",
            "part:mercurius",
            "part:ancientArtillery",
            "part:helioSword",
            "part:voidmaker",
            "part:cinderspireHowitzer"
        ),
        List.of( // kin bali
            "part:blackWidow",
            "part:debellatio",
            "part:ionstormer",
            "part:direBrand",
            "part:fissureCannon",
            "part:nmrBallista"
        ),
        List.of( // heat dir
            "part:trackingBolt",
            "part:forceMajoris",
            "part:ashbringer",
            "part:oddEye",
            "part:ionFlare",
            "part:munroeArrow"
        ),
        List.of( // mag dir
            "part:warpBeam",
            "part:frostbite",
            "part:arcticCrescent",
            "part:darkHorizon",
            "part:plasmaBeacon",
            "part:overclockPike"
        ),
        List.of( // A track missile
            "part:apate",
            "part:landeel",
            "part:bosonCondensator",
            "part:zeroEvent",
            "part:venomLauncher",
            "part:thunderbird"
        ),
        List.of( // B track missile
            "part:spitfire",
            "part:thermiteNest",
            "part:nanoTrojans",
            "part:zeusExMachina",
            "part:patriotLauncher",
            "part:galeforce"
        ),
        List.of( // A track sb
            "part:duskInterceptor",
            "part:spectre",
            "part:hellhorn",
            "part:blockbuster",
            "part:cormack",
            "part:carinaSwarm"
        ),
        List.of( // B track sb
            "part:foxTail",
            "part:mindCustom",
            "part:stormsingers",
            "part:tyrannos",
            "part:novaStorm",
            "part:bullwark"
        ),
        List.of( // shields
            "part:saganKineticsShield",
            "part:saganHeatShield",
            "part:saganMagneticShield",
            "part:saganAntiExplosiveShield"
        )
    );
    private final String[] replaceWithPart = new String[]{
        "part:bloodspur",
        "part:shootingStar",
        "part:phantomRay",
        "part:scorpionStinger",
        "part:shadowflare",
        "part:redComet",
        "part:darkSpecter",
        "part:chronusWing",
        "part:eosPhaseShiftShield"
    };

    public ShipService() {

        instance = this;

        this.bodyStats = ResourceManager.getShipBodies();
        this.partStats = ResourceManager.getShipParts();

        this.replaceWithBody = this.bodyStats.findByBodyName("body:weikes").getLevels().get(0).getId();

        this.replaceWithBodyParts = new ArrayList<>();

        int laser = this.partStats.findByPartName("part:clusterLaserTransmitter").getLevels().get(0).getId();
        for (int i = 0; i < 5; ++i) {
            this.replaceWithBodyParts.add(laser);
        }
        this.replaceWithBodyParts.add(this.partStats.findByPartName("part:teamCombatEngine").getLevels().get(0).getId());
    }

    public boolean validatePhaseOneShipModel(int bodyId, List<Integer> partIds) {

        String bodyName = this.bodyStats.findByBodyId(bodyId).getName();
        if (!this.legalBody.contains(bodyName)) {
            return false;
        }

        for (int part : partIds) {
            String partName = this.partStats.findByPartId(part).getName();
            for (List<String> illegalPart : this.illegalParts) {
                if (illegalPart.contains(partName)) {
                    return false;
                }
            }
        }

        return true;
    }

    public ShipModel updatePhaseOneShipModel(ShipModel model) {

        String bodyName = this.bodyStats.findByBodyId(model.getBodyId()).getName();
        if (!this.legalBody.contains(bodyName)) {
            model.setBodyId(this.replaceWithBody);
            model.setParts(this.replaceWithBodyParts);
            return model;
        }

        boolean hasChanges = false;

        List<Integer> parts = model.getParts();
        for (int i = 0; i < parts.size(); ++i) {
            String partName = this.partStats.findByPartId(parts.get(i)).getName();
            for (int j = 0; j < this.illegalParts.size(); ++j) {
                if (this.illegalParts.get(j).contains(partName)) {
                    hasChanges = true;

                    int newPart = this.partStats
                        .findByPartName(this.replaceWithPart[j])
                        .getLevels()
                        .get(2)
                        .getId();
                    parts.set(i, newPart);
                }
            }
        }

        return hasChanges ? model : null;
    }

    public boolean validateShipModel(User user, int bodyId, List<Integer> partIds) {

        UserShipUpgrades upgrades = user.getShipUpgrades();

        if (!upgrades.hasBodyUpgrade(bodyId)) {
            return false;
        }

        int moduleRemaining = (int) this.bodyStats
            .getMeta(bodyId)
            .getModuleStorage();

        Map<Integer, Integer> groupCount = new HashMap<>();

        for (int partId : partIds) {
            if (!upgrades.hasPartUpgrade(partId)) {
                return false;
            }

            ShipPartData partGroup = this.partStats.findByPartId(partId);
            if (partGroup.getLimit() != -1) {
                int count = groupCount.getOrDefault(partGroup.getGroupId(), 0) + 1;
                if (count > partGroup.getLimit()) {
                    return false;
                }
                groupCount.put(partGroup.getGroupId(), count);
            }

            PartLevelMeta partLevel = partGroup.getLevel(partId);
            int partSize = partLevel.getModuleUsage();
            /*
             * TODO: These are hardcoded for now, but they should be converted to enums.
             *     Also, the size reduction multipliers should also be calculated from the science json.
             *     This is a quick fix to get the server back online, so shortcuts were made.
             */
            // Not checking for research levels is a deliberate choice to save server performance
            if (partGroup.getPartType().equals("attack")) {
                switch (partGroup.getPartSubType()) {
                    case "ballistic":
                    case "shipBased": {
                        partSize *= 0.90;
                        break;
                    }
                    case "directional": {
                        partSize *= 0.91;
                        break;
                    }
                    case "missile": {
                        partSize *= 0.80;
                        break;
                    }
                }
            }

            moduleRemaining -= partSize;
        }

        return moduleRemaining >= 0;
    }

    public static ShipService getInstance() {

        return instance;
    }
}
