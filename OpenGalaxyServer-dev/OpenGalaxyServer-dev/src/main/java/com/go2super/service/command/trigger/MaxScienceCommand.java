package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserTech;
import com.go2super.database.entity.sub.UserTechs;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ResearchData;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.util.ArrayList;
import java.util.List;

// DONT FUCKING RUN THIS ON A USER THAT HAS AN UPGRADE IN PROCESS
// TODO - Unfuck this

public class MaxScienceCommand extends Command {

    public MaxScienceCommand() {
        super("maxscience", "permission.giveuser");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (account.getUserRank() != UserRank.ADMIN) {
            sendMessage("This command is only available to ADMIN users!", user);
            return;
        }

        if (parts.length < 2) {
            sendMessage("Usage: /maxscience <userId>", user);
            return;
        }

        int userId = Integer.parseInt(parts[1]);

        User targetUser = UserService.getInstance().getUserCache().findByGuid(userId);
        if (targetUser == null) {
            sendMessage("User not found: " + userId, user);
            return;
        }

        UserTechs userTechs = targetUser.getTechs();
        List<UserTech> newTechs = new ArrayList<>();

        for (ResearchData researchData : ResourceManager.getScience().getResearchData()) {
            if (researchData.getLevels() == null || researchData.getLevels().isEmpty()) {
                continue;
            }

            int maxLv = researchData.getLevels().stream()
                .mapToInt(lv -> lv.getLv())
                .max()
                .orElse(1);

            UserTech userTech = UserTech.builder()
                .id(researchData.getId())
                .level(maxLv)
                .build();

            newTechs.add(userTech);
        }

        userTechs.setTechs(newTechs);
        targetUser.save();

        sendMessage("Maxed " + newTechs.size() + " science research for user " + userId, user);
        sendMessage("Your science has been maxed! Please restart your client.", targetUser);
    }
}
