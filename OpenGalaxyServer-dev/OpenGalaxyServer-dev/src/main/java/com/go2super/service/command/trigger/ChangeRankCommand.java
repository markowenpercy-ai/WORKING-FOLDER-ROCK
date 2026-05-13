package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.AccountService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

public class ChangeRankCommand extends Command {

    public ChangeRankCommand() {

        super("changerank", "permission.changerank");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'changerank' has invalid arguments! (changerank <guid> <rank>)", user);
            return;

        }

        int guid = Integer.parseInt(parts[1]);
        String rank = parts[2];

        if (rank.length() > 32) {
            sendMessage("Rank name is too long!", user);
            return;
        }

        User receiver = UserService.getInstance().getUserCache().findByGuid(guid);

        if (receiver == null) {

            sendMessage("Receiver with id " + guid + " does not exists!", user);
            return;

        }

        UserRank found = null;

        for (UserRank userRank : UserRank.values()) {
            if (userRank.name().equalsIgnoreCase(rank)) {
                found = userRank;
                break;
            }
        }

        if (found == null) {

            sendMessage("Rank " + rank + " does not exists!", user);
            return;

        }

        Account receiverAccount = receiver.getAccount();
        receiverAccount.setUserRank(found);

        AccountService.getInstance().getAccountCache().save(receiverAccount);
        sendMessage("Rank of " + receiver.getUsername() + " (" + receiverAccount.getEmail() + ") changed to " + rank + "!", user);

    }

}
