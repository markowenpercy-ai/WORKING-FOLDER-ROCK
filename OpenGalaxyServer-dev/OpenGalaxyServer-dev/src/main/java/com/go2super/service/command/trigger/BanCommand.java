package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Sanction;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.SanctionType;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.custom.CustomWarnPacket;
import com.go2super.packet.login.GameServerLoginPacket;
import com.go2super.service.*;
import com.go2super.service.command.Command;
import com.go2super.socket.util.DateUtil;
import lombok.SneakyThrows;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.util.*;

public class BanCommand extends Command {

    public BanCommand() {

        super("ban", "permission.ban");
    }

    @SneakyThrows
    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'ban' need more arguments! (example: /ban 1 scam)", user);
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

        if (message.length() > 27) {

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

            sendMessage("Can't perform this command because user has ban permissions!", user);
            return;

        }

        Date banUntil = DateUtil.offsetYears(100);

        toAccount.setBanUntil(banUntil);
        AccountService.getInstance().getAccountCache().save(toAccount);

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
                "<strong><font size='15' face=\"Verdana\" color='#FC0303'>You have been banned!</font></strong><br/>" +
            "Reason: " + message.replaceAll("<.*?>", "") + "<br/>" +
            "Until: PERMANENT", 1024));

        sendMessage("User has been banned!", user);
        ChatService.getInstance().medusaMessage("The user [" + toUser.getUsername() + "] has been PERMANENTLY banned!");

        Sanction sanction = Sanction.builder()
            .id(ObjectId.get())
            .sanctionType(SanctionType.PERMANENT_BAN)
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
            JobService.getInstance().getTaskScheduler().scheduleWithFixedDelay(() -> {

                GameServerLoginPacket banScreenPacket = new GameServerLoginPacket();

                banScreenPacket.setError((byte) 7);
                banScreenPacket.setGuid(0);
                banScreenPacket.setGuide(1);

                gameUserOptional.get().getSmartServer().send(banScreenPacket);

            }, Duration.ofSeconds(5));

        }

    }

}
