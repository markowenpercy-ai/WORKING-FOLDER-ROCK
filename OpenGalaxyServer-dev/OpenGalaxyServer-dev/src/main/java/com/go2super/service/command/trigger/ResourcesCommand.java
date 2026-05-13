package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.service.ChatService;
import com.go2super.service.DiscordService;
import com.go2super.service.ResourcesService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.awt.*;
import java.util.*;

public class ResourcesCommand extends Command {

    public ResourcesCommand() {

        super("resources", "permission.resources", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {

            sendMessage("Command 'resources' has invalid arguments! (resources <guid>)", user);
            return;

        }

        int guid = Integer.parseInt(parts[1]);
        User receiver = UserService.getInstance().getUserCache().findByGuid(guid);

        if (receiver == null) {

            sendMessage("Receiver with id " + guid + " does not exists!", user);
            return;

        }

        Long gold = Math.abs(receiver.getResources().getGold() - 900_000_000);
        Long he3 = Math.abs(receiver.getResources().getHe3() - 900_000_000);
        Long metal = Math.abs(receiver.getResources().getMetal() - 900_000_000);
        Long vouchers = Math.abs(receiver.getResources().getVouchers() - 900_000_000);

        String s = String.format("A user with guid=%s were given resources by guid=%s", receiver.getGuid(), user.getGuid());
        DiscordService.getInstance().getRayoBot().sendAudit("[RESOURCES CMD]", s, Color.decode("0xfafafa"), AuditType.INCIDENT);

        if (gold == 0 && he3 == 0 && metal == 0 && vouchers == 0) {
            sendMessage("Resources sent!", user);
            return;
        }

        receiver.getResources().setGold(900_000_000);
        receiver.getResources().setHe3(900_000_000);
        receiver.getResources().setMetal(900_000_000);
        receiver.getResources().setVouchers(900_000_000);

        receiver.save();

        Optional<LoggedGameUser> optional = receiver.getLoggedGameUser();

        if (optional.isPresent()) {

            ResponseUsePropsPacket response = ResourcesService.getInstance().genericUseProps(-1, 0, metal.intValue(), he3.intValue(), gold.intValue(), vouchers.intValue(), 0, 0, 0, 0, 1, 0);
            optional.get().getSmartServer().send(response);

            sendMessage("You have received resources!", receiver);

        }

        sendMessage("Resources sent!", user);

    }

}
