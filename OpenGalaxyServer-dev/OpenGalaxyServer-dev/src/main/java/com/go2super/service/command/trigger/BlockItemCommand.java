package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.RestrictedItemsService;
import com.go2super.service.command.Command;
import com.go2super.service.ChatService;

import java.io.IOException;

public class BlockItemCommand extends Command {

    public BlockItemCommand() {
        super("blockitem", "permission.blockItem", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) throws IOException, InterruptedException {

        if (parts.length < 1) {
            ChatService.getInstance().sendMessage("Usage: /blockitem <check|block|unblock> <propId>", user);
            return;
        }

        String action = parts.length > 1 ? parts[1].toLowerCase() : "";

        if (action.equals("check")) {
            if (parts.length < 3) {
                ChatService.getInstance().sendMessage("Usage: /blockitem check <propId>", user);
                return;
            }
            try {
                int propId = Integer.parseInt(parts[2]);
                boolean restricted = RestrictedItemsService.getInstance().isRestricted(propId);
                String name = RestrictedItemsService.getInstance().getPrettyName(propId);
                if (restricted) {
                    ChatService.getInstance().sendMessage("Prop " + propId + " (" + name + ") is BLOCKED", user);
                } else {
                    ChatService.getInstance().sendMessage("Prop " + propId + " (" + name + ") is NOT blocked", user);
                }
            } catch (NumberFormatException e) {
                ChatService.getInstance().sendMessage("Invalid prop ID: " + parts[1], user);
            }
        } else if (action.equals("block")) {
            if (parts.length < 3) {
                ChatService.getInstance().sendMessage("Usage: /blockitem block <propId>", user);
                return;
            }
            try {
                int propId = Integer.parseInt(parts[2]);
                String name = RestrictedItemsService.getInstance().getPrettyName(propId);
                boolean added = RestrictedItemsService.getInstance().addRestrictedId(propId);
                if (added) {
                    ChatService.getInstance().sendMessage("Prop " + propId + " (" + name + ") has been BLOCKED", user);
                } else {
                    ChatService.getInstance().sendMessage("Prop " + propId + " (" + name + ") is already blocked", user);
                }
            } catch (NumberFormatException e) {
                ChatService.getInstance().sendMessage("Invalid prop ID: " + parts[2], user);
            }
        } else if (action.equals("unblock")) {
            if (parts.length < 3) {
                ChatService.getInstance().sendMessage("Usage: /blockitem unblock <propId>", user);
                return;
            }
            try {
                int propId = Integer.parseInt(parts[2]);
                String name = RestrictedItemsService.getInstance().getPrettyName(propId);
                boolean removed = RestrictedItemsService.getInstance().removeRestrictedId(propId);
                if (removed) {
                    ChatService.getInstance().sendMessage("Prop " + propId + " (" + name + ") has been UNBLOCKED", user);
                } else {
                    ChatService.getInstance().sendMessage("Prop " + propId + " (" + name + ") was not blocked", user);
                }
            } catch (NumberFormatException e) {
                ChatService.getInstance().sendMessage("Invalid prop ID: " + parts[1], user);
            }
        } else {
            ChatService.getInstance().sendMessage("Usage: /blockitem <check|block|unblock> <propId>", user);
        }
    }
}