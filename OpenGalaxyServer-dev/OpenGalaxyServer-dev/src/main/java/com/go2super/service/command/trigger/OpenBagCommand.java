package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.obj.entry.SmartServer;
import com.go2super.packet.science.ResponseAddPackPacket;
import com.go2super.service.command.Command;

public class OpenBagCommand extends Command {
    public OpenBagCommand() {
        super("openbag", "permission.openbag");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        UserResources userResources = user.getResources();
        UserInventory userInventory = user.getInventory();

        int canAffordSlots = (int) (userResources.getGold() / 1000);
        int slotsRemain = 200 - userInventory.getMaximumStacks();

        int buySlots = Math.min(canAffordSlots, slotsRemain);

        userResources.setGold(userResources.getGold() - buySlots * 1000L);
        userInventory.setMaximumStacks(userInventory.getMaximumStacks() + buySlots);

        user.getMetrics().add("action:buy.slot", buySlots);
        user.update();
        user.save();

        ResponseAddPackPacket response = new ResponseAddPackPacket();
        response.setPropsPack(userInventory.getMaximumStacks());

        for (int i = 0; i < buySlots; ++i) {
            smartServer.send(response);
        }
    }
}
