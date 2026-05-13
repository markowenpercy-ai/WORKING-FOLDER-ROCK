package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.service.AccountService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.util.*;

public class DeleteCommand extends Command {

    public DeleteCommand() {

        super("delete", "permission.delete");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length != 2) {

            sendMessage("Command 'delete' has invalid arguments!", user);
            return;

        }

        String username = parts[1];

        Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findByUsername(username);

        if (!optionalAccount.isPresent()) {
            sendMessage("ERROR= Account not found!", user);
            return;
        }

        Account selectedAccount = optionalAccount.get();
        AccountService.getInstance().getAccountCache().delete(selectedAccount);

        List<User> users = UserService.getInstance().getUserCache().findByAccountId(selectedAccount.getId().toString());

        for (User selectedUser : users) {

            Optional<LoggedGameUser> loggedGameUser = selectedUser.getLoggedGameUser();

            if (loggedGameUser.isPresent()) {
                loggedGameUser.get().getSmartServer().close();
            }

            PacketService.getInstance().getPlanetCache().delete(selectedUser.getPlanet());
            UserService.getInstance().getUserCache().delete(selectedUser);

        }

        sendMessage("Account deleted!", user);

    }

}
