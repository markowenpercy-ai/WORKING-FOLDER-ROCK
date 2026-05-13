package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Sanction;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.SanctionType;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.custom.CustomWarnPacket;
import com.go2super.service.AccountService;
import com.go2super.service.LoginService;
import com.go2super.service.RiskService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;
import org.bson.types.ObjectId;

import java.util.*;

public class WarnCommand extends Command {

    public WarnCommand() {

        super("warn", "permission.warn");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'warn' need more arguments! (example: /warn 1 spam in the global chat)", user);
            return;

        }

        int toGuid = Integer.parseInt(parts[1]);
        String message = "";

        for (int i = 2; i < parts.length; i++) {
            message += parts[i] + " ";
        }

        if (toGuid < 0) {

            sendMessage("Invalid command arguments!", user);
            return;

        }

        if (message.length() > 32) {

            sendMessage("Message too long!", user);
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

            sendMessage("Can't perform this command because user has warn permissions!", user);
            return;

        }

        CustomWarnPacket response = new CustomWarnPacket();

        response.setSeqId(0);
        response.setSrcUserId(0);
        response.setObjUserId(0);
        response.setGuid(0);
        response.setObjGuid(0);
        response.setChannelType((short) 0);
        response.setSpecialType((short) 0);
        response.setPropsId(0);
        response.setName(WideString.of(user.getUsername(), 32));
        response.setToName(WideString.of(user.getUsername(), 32));
        response.setBuffer(WideString.of(
                "<strong><font size='15' face=\"Verdana\" color='#FFA200'>You have been warned!</font></strong><br/>" +
            "Reason: " + message.replaceAll("<.*?>", ""), 1024));

        sendMessage("User has been warned!", user);

        Sanction sanction = Sanction.builder()
            .id(ObjectId.get())
            .sanctionType(SanctionType.WARN)
            .label(label)
            .creation(new Date())
            .staffAccountId(user.getAccountId())
            .staffName(user.getUsername())
            .staffGuid(user.getGuid())
            .userAccountId(toUser.getAccountId())
            .userName(toUser.getUsername())
            .userGuid(toUser.getGuid())
            .build();

        RiskService.getInstance().getSanctionRepository().save(sanction);

        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(toGuid);
        if (gameUserOptional.isPresent()) {
            gameUserOptional.get().getSmartServer().send(response);
        }

    }

}
