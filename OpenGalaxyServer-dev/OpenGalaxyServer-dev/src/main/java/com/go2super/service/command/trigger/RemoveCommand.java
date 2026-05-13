package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.AccountService;
import com.go2super.service.command.Command;

public class RemoveCommand extends Command {

    public RemoveCommand() {

        super("remove", "permission.discord");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (account.getDiscordHook() == null || !account.getDiscordHook().isLinkedDiscordBefore()) {

            sendMessage("You don't have a discord linked account!", user);
            return;

        }

        account.getDiscordHook().setDiscordId(null);
        account.getDiscordHook().setDiscordCode(null);
        account.getDiscordHook().setLinkedDiscordBefore(true);

        AccountService.getInstance().getAccountCache().save(account);

        sendMessage("Your discord account has been removed!", user);

    }

}
