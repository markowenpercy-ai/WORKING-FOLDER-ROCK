package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserBuilding;
import com.go2super.database.entity.sub.UserBuildings;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.meta.BuildLevelMeta;
import com.go2super.resources.data.meta.BuildRequirementMeta;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.util.HashMap;
import java.util.Map;

public class MaxBuildingsCommand extends Command {

    public MaxBuildingsCommand() {
        super("maxbuildings", "permission.giveuser");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (account.getUserRank() != UserRank.ADMIN) {
            sendMessage("This command is only available to ADMIN users!", user);
            return;
        }

        if (parts.length < 2) {
            sendMessage("Usage: /maxbuildings <userId>", user);
            return;
        }

        int userId = Integer.parseInt(parts[1]);

        User targetUser = UserService.getInstance().getUserCache().findByGuid(userId);
        if (targetUser == null) {
            sendMessage("User not found: " + userId, user);
            return;
        }

        UserBuildings userBuildings = targetUser.getBuildings();
        if (userBuildings == null || userBuildings.getBuildings() == null) {
            sendMessage("No buildings found for user: " + userId, user);
            return;
        }

        Map<Integer, Integer> buildingLevels = new HashMap<>();
        for (UserBuilding building : userBuildings.getBuildings()) {
            BuildData data = building.getData();
            if (data == null) continue;
            buildingLevels.put(data.getId(), building.getLevelId());
        }

        int count = 0;
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < userBuildings.getBuildings().size(); i++) {
                UserBuilding building = userBuildings.getBuildings().get(i);

                if (building.getUpdating() != null && building.getUpdating()) {
                    continue;
                }
                if (building.getRepairing() != null && building.getRepairing()) {
                    continue;
                }

                BuildData buildData = building.getData();
                if (buildData == null || buildData.getLevels() == null || buildData.getLevels().isEmpty()) {
                    continue;
                }

                int currentLevel = building.getLevelId();
                int maxPossibleLevel = currentLevel;

                for (BuildLevelMeta levelMeta : buildData.getLevels()) {
                    int targetLv = levelMeta.getLv();
                    if (targetLv <= currentLevel) {
                        continue;
                    }

                    if (levelMeta.getRequire() == null || levelMeta.getRequire().isEmpty()) {
                        maxPossibleLevel = targetLv;
                    } else {
                        boolean requirementsMet = true;
                        for (BuildRequirementMeta req : levelMeta.getRequire()) {
                            int requiredLevel = req.getLv();
                            BuildData requiredBuildingData = ResourceManager.getBuilds().getBuild(req.getBuild());
                            if (requiredBuildingData == null) {
                                requirementsMet = false;
                                break;
                            }
                            Integer currentRequiredLevel = buildingLevels.get(requiredBuildingData.getId());
                            if (currentRequiredLevel == null || currentRequiredLevel < requiredLevel) {
                                requirementsMet = false;
                                break;
                            }
                        }
                        if (requirementsMet) {
                            maxPossibleLevel = targetLv;
                        }
                    }
                }

                if (maxPossibleLevel > currentLevel) {
                    building.setLevelId(maxPossibleLevel);
                    buildingLevels.put(buildData.getId(), maxPossibleLevel);
                    count++;
                    changed = true;
                }
            }
        } while (changed);

        targetUser.save();

        sendMessage("Maxed " + count + " building upgrades for user " + userId, user);
        if (targetUser.isOnline()) {
            sendMessage("Your buildings have been maxed! Please restart your client.", targetUser);
        }
    }
}
