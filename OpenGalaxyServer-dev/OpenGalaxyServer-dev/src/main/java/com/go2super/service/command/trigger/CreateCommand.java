package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.AccountStatus;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.AccountService;
import com.go2super.service.command.Command;
import com.go2super.socket.util.Crypto;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.RandomUtil;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

public class CreateCommand extends Command {

    public CreateCommand() {

        super("create", "permission.create");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length != 2) {

            sendMessage("Command 'create' has invalid arguments!", user);
            return;

        }

        String email = RandomUtil.getRandomInt(500000, 800000) + "@supergo2.com";
        String username = parts[1];
        String password = RandomStringUtils.randomAlphanumeric(8);

        Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findByEmail(email)
            .or(() -> AccountService.getInstance().getAccountCache().findByUsername(username));

        if (optionalAccount.isPresent()) {
            if (email.equals(optionalAccount.get().getEmail())) {
                sendMessage("ERROR= Email taken!", user);
                return;
            } else if (username.equals(optionalAccount.get().getUsername())) {
                sendMessage("ERROR= Username taken!", user);
                return;
            }
        }

        Account newAccount = Account.builder()
            .email(email)
            .username(username)
            .password(Crypto.encrypt(password))
            .accountStatus(AccountStatus.REGISTER)
            .userRank(UserRank.USER)
            .registerDate(DateUtil.now())
            .vip(0)
            .build();

        AccountService.getInstance().getAccountCache().save(newAccount);

        // Continue
        // AccountService.getInstance().randomTest(newAccount);

        // Send credentials
        sendMessage("New user account created!", user);
        sendMessage("Email= " + email, user);
        sendMessage("Username= " + username, user);
        sendMessage("Password= " + password, user);

    }

}
