package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Sanction;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.SanctionType;
import com.go2super.listener.ChatListener;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.custom.CustomWarnPacket;
import com.go2super.service.*;
import com.go2super.service.command.Command;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.TimeReader;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MuteCommand extends Command {

    public static final TimeReader timeReader =
        new TimeReader()
            .addUnit("d", 86400000L)
            .addUnit("h", 3600000L)
            .addUnit("m", 60000L)
            .addUnit("s", 1000L);

    private static final String pattern = "MM-dd-yyyy HH:mm:ss";
    private static final DateFormat dateFormat = new SimpleDateFormat(pattern);

    public MuteCommand() {

        super("mute", "permission.mute");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 4) {

            sendMessage("Command 'mute' need more arguments! (example: /mute 1 1m spam in the global chat)", user);
            return;

        }

        int toGuid = Integer.parseInt(parts[1]);
        long parsed = timeReader.parse(parts[2]);
        String message = "";

        for (int i = 3; i < parts.length; i++) {
            message += parts[i] + " ";
        }

        if (toGuid < 0 || parsed <= 0) {

            sendMessage("Invalid command arguments!", user);
            return;

        }

        if (message.length() > 27) {

            sendMessage("Message too long!", user);
            return;

        }

        if (parsed > 28800000) {

            sendMessage("Mute time too long!", user);
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

            sendMessage("Can't perform this command because user has mute permissions!", user);
            return;

        }

        Date until = DateUtil.nowMillis(Long.valueOf(parsed).intValue());
        ChatListener.muteList.put(toGuid, until);

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
                "<strong><font size='15' face=\"Verdana\" color='#FC0303'>You have been silenced!</font></strong><br/>" +
            "Reason: " + message.replaceAll("<.*?>", "") + "<br/>" +
            "Until: " + dateFormat.format(until) + " (Server Time)", 1024));

        sendMessage("User has been silenced!", user);
        ChatService.getInstance().medusaMessage("The user [" + toUser.getUsername() + "] has been silenced for " + (parsed / 1000) + " seconds!");

        Sanction sanction = Sanction.builder()
            .id(ObjectId.get())
            .sanctionType(SanctionType.MUTE)
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
