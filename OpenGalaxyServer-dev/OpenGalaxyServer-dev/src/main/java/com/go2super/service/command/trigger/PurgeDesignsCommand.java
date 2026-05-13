package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.PacketService;
import com.go2super.service.ShipService;
import com.go2super.service.command.Command;

import java.util.*;



public class PurgeDesignsCommand extends Command {

    public PurgeDesignsCommand() {

        super("purgedesigns", "permission.maintenance");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        sendMessage("Clearing designs...", user);

        int count = 0;
        List<ShipModel> models = PacketService.getInstance().getShipModelCache().findAll();
        for (ShipModel model : models) {
            if (model.getGuid() == -1) {
                continue;
            }

            ShipModel changed = ShipService.getInstance().updatePhaseOneShipModel(model);
            if (changed == null) {
                continue;
            }

            ++count;
            PacketService.getInstance().getShipModelCache().save(changed);
        }

        sendMessage("Found " + count + " designs past Phase One limits.", user);
    }
}
