package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.AccountService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;
import lombok.SneakyThrows;

import java.util.*;

public class UnbanCommand extends Command {

    public UnbanCommand() {

        super("unban", "permission.unban");
    }

    @SneakyThrows
    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {

            sendMessage("Command 'unban' need more arguments! (example: /unban 1)", user);
            return;

        }

        int toGuid = Integer.parseInt(parts[1]);

        if (toGuid < 0) {

            sendMessage("Invalid command arguments!", user);
            return;

        }

        if (toGuid == user.getGuid()) {

            sendMessage("You can't use this command in yourself!", user);
            return;

        }

        User toUser = UserService.getInstance().getUserCache().findByGuid(toGuid);

        if (toUser == null) {

            sendMessage("User not exists!", user);
            return;

        }

        Optional<Account> optionalToAccount = AccountService.getInstance().getAccountCache().findById(toUser.getAccountId());
        if (optionalToAccount.isEmpty()) {

            sendMessage("User not have an account!", user);
            return;

        }

        Account toAccount = optionalToAccount.get();
        if (toAccount.getUserRank().hasAnyPermission(getPermission())) {

            sendMessage("Can't perform this command because user has unban permissions!", user);
            return;

        }

        toAccount.setBanUntil(null);
        AccountService.getInstance().getAccountCache().save(toAccount);

        sendMessage("User has been un-banned!", user);

    }

}
