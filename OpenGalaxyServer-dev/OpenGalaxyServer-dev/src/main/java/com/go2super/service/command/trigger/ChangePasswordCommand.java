package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.AccountService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;
import com.go2super.socket.util.Crypto;

public class ChangePasswordCommand extends Command {

    public ChangePasswordCommand() {

        super("changepassword", "permission.changepassword");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'changepassword' has invalid arguments! (changepassword <guid> <password>)", user);
            return;

        }

        int guid = Integer.parseInt(parts[1]);
        String password = parts[2];

        if (password.length() > 32) {
            sendMessage("Password is too long!", user);
            return;
        }

        User receiver = UserService.getInstance().getUserCache().findByGuid(guid);

        if (receiver == null) {

            sendMessage("Receiver with id " + guid + " does not exists!", user);
            return;

        }

        Account receiverAccount = receiver.getAccount();
        receiverAccount.setPassword(Crypto.encrypt(password));

        AccountService.getInstance().getAccountCache().save(receiverAccount);
        sendMessage("Password of " + receiver.getUsername() + " (" + receiverAccount.getEmail() + ") changed to " + password + "!", user);

        receiver.getLoggedGameUser().ifPresent(x -> {
            LoginService.getInstance().disconnectWeb(x.getLoggedSessionUser().getSessionKey());
            LoginService.getInstance().disconnectGame(x.getLoggedSessionUser());
        });



    }

}
