package com.go2super.resources.data.meta;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Commander;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.logger.BotLogger;
import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.ShipPartData;
import com.go2super.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequirementMeta extends JsonData {

    private String type;
    private String identifier;
    private int value;

    public boolean hasCompleted(User user) {

        UserMetrics metrics = user.getMetrics();
        UserBuildings buildings = user.getBuildings();
        UserShipUpgrades upgrades = user.getShipUpgrades();
        UserTechs techs = user.getTechs();

        switch (type) {

            case "action":
            case "instance":

                Metric metric = metrics.getMetric(identifier);
                return metric.getValue() >= value;

            case "science":

                UserTech tech = techs.getTech(identifier);

                return tech != null && tech.getLevel() >= value;

            case "current":

                switch (identifier) {

                    case "current:friends":
                        return user.getFriends().size() >= value;

                    case "current:ships":
                        return user.getShips().countTotalShips(user.getGuid()) >= value;

                    case "current:resources":

                        long he3 = user.getResources().getHe3();
                        long metal = user.getResources().getMetal();
                        long gold = user.getResources().getGold();

                        if (he3 >= value && metal >= value && gold >= value) {
                            return true;
                        }

                    case "current:commander.stars":

                        List<Commander> commanders = user.getCommanders();
                        for (Commander commander : commanders) {
                            if (commander.getStars() >= value) {
                                return true;
                            }
                        }

                }

                return false;

            case "output":

                switch (identifier) {

                    case "resource:he3":
                        return user.getStorage().getHe3Production() >= value;

                    case "resource:metal":
                        return user.getStorage().getMetalProduction() >= value;

                    case "resource:gold":
                        return user.getStorage().getGoldProduction() >= value;

                }

                return false;

            case "build":

                boolean built = buildings.getBuildings(identifier).stream().anyMatch(build -> build.getLevelId() >= 0);
                return built;

            case "upgrade":

                String[] split = identifier.split(":");
                String upgradeType = split[0];

                switch (upgradeType) {

                    case "ship":

                        Metric shipMetric = metrics.getMetric("action:ship.upgrade");
                        return shipMetric.getValue() >= value;

                    case "part":

                        Metric partMetric = metrics.getMetric("action:part.upgrade");
                        return partMetric.getValue() >= value;

                    case "build":

                        List<UserBuilding> builds = buildings.getBuildings(identifier);
                        for (UserBuilding build : builds) {
                            if (build.getLevelId() >= value) {
                                return true;
                            }
                        }

                        return false;

                }

                return false;

            case "body":

                List<Integer> currentBodies = upgrades.getCurrentBodies();
                for (ShipBodyData bodyData : ResourceManager.getShipBodies().getShipBody()) {
                    if (bodyData.getName().equals(identifier)) {
                        for (BodyLevelMeta level : bodyData.getLevels()) {
                            if (currentBodies.contains(level.getId())) {
                                return true;
                            }
                        }
                    }
                }

                return false;

            case "part":

                List<Integer> currentParts = upgrades.getCurrentParts();
                for (ShipPartData partData : ResourceManager.getShipParts().getShipPart()) {
                    if (partData.getName().equals(identifier)) {
                        for (PartLevelMeta level : partData.getLevels()) {
                            if (currentParts.contains(level.getId())) {
                                return true;
                            }
                        }
                    }
                }

                return false;

            case "link":

                Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findById(user.getAccountId());
                if (optionalAccount.isEmpty()) {
                    return false;
                }

                Account account = optionalAccount.get();

                if ("link:discord".equals(identifier)) {
                    return account.getDiscordHook() != null && account.getDiscordHook().isLinkedDiscordBefore();
                }

                return false;

            default:

                BotLogger.error("Not found requirement logic for (type: " + type + ", identifier: " + identifier + ", value: " + value + ")");
                return false;

        }

    }

}
