package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.Email;
import com.go2super.database.entity.sub.EmailGood;
import com.go2super.database.entity.sub.UserEmailStorage;
import com.go2super.database.entity.type.UserRank;
import com.go2super.listener.PropListener;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.service.DiscordService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;
import com.go2super.socket.util.DateUtil;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_ID;

public class GiveMailCommand extends Command {

    public GiveMailCommand() {
        super("givemail", "permission.gift");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (parts.length < 3) {
            sendMessage("Command 'givemail' need more arguments!", user);
            return;
        }

        String target = parts[1];

        int propId = Integer.parseInt(parts[2]);
        if (propId < 0) {
            sendMessage("Invalid prop id for 'givemail' command! (got " + propId + ")", user);
            return;
        }
        PropData propData = ResourceManager.getProps().getData(propId);
        if (propId > MAX_COMMANDER_ID && propData == null) {
            sendMessage("Invalid prop id for 'givemail' command! (got " + propId + ")", user);
            return;
        }

        int amount = 1;
        if (parts.length >= 4) {
            amount = Integer.parseInt(parts[3]);
        }
        if (amount < 0 || amount > 9999) {
            sendMessage("Invalid quantity for 'givemail' command! (expected 0-9999, got " + amount + ")", user);
            return;
        }

        boolean isLock = true;
        if (parts.length >= 5) {
            String lock = parts[4];
            isLock = lock.equalsIgnoreCase("LOCK")
                    || lock.equalsIgnoreCase("TRUE")
                    || lock.equals("1");
        }

        String emailBody = "Dear Commander %s, \n" +
                "Thank you for being part of the dark galaxy, here's a gift for you. We hope it will be very useful for you!";
        if (parts.length >= 6) {
            switch (parts[5].toUpperCase()) {
                case "MAINT": {
                    emailBody = "Dear Commander %s, \n" +
                            "The game has been updated. Please enjoy this gift from us in exchange!";
                    break;
                }
                case "VIP": {
                    emailBody = "Dear Commander %s, \n" +
                            "Here's an exclusive VIP gift for you, we hope it will be very useful for you!";
                    break;
                }
                case "MVP": {
                    emailBody = "Dear Commander %s, \n" +
                            "Here's an exclusive MVP gift for you, we hope it will be very useful for you!";
                    break;
                }
            }
        }

        int[] propIds = new int[10];
        int[] propNums = new int[10];

        propIds[0] = propId;
        propNums[0] = amount;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String formattedDate = dateFormat.format(new Date());

        sendMessage(String.format("Sent propId=%s %dx to target=%s on %s", propId, amount, target, formattedDate), user);

        ResponseNewEmailNoticePacket noticePacket = ResponseNewEmailNoticePacket.builder()
                .errorCode(0)
                .build();

        if (target.matches("^\\d+$")) {
            int targetId = Integer.parseInt(target);
            User player = UserService.getInstance().getUserCache().findByGuid(targetId);

            UserEmailStorage userEmailStorage = player.getUserEmailStorage();

            Email email = Email.builder()
                    .autoId(userEmailStorage.nextAutoId())
                    .type(2)
                    .name("System")
                    .subject("\uD83C\uDF81 Council Gift")
                    .emailContent(String.format(emailBody, player.getUsername()))
                    .readFlag(0)
                    .date(DateUtil.now())
                    .goods(new ArrayList<>())
                    .guid(-1)
                    .build();

            email.addGood(EmailGood.builder()
                    .goodId(propId)
                    .lockNum(isLock ? amount : 0)
                    .num(isLock ? 0 : amount)
                    .build());

            userEmailStorage.addEmail(email);

            player.update();
            player.save();

            Optional<LoggedGameUser> optionalGameUser = player.getLoggedGameUser();
            if (optionalGameUser.isPresent()) {
                optionalGameUser.get().getSmartServer().send(noticePacket);
                sendMessage("You have received a gift from the council!", player);
            }
            String propN = propData == null || propData.getName() == null ? "Commander" : propData.getName();
            String s = String.format("GiveMail: %s sent %s with propId=%s %dx to target=%s on %s", user.getUsername(), propN, propId, amount, target, formattedDate);
            DiscordService.getInstance().getRayoBot().sendAudit("System", s, Color.MAGENTA, AuditType.INCIDENT);
            return;
        }

        boolean validTarget = target.equalsIgnoreCase("ALL")
                || target.equalsIgnoreCase("MVP")
                || target.equalsIgnoreCase("VIP");
        if (!validTarget) {
            sendMessage("Invalid target for 'givemail' command! (expected \"ALL\", \"MVP\", \"VIP\", or player id, got " + target + ")", user);
            return;
        }

        for (User player : UserService.getInstance().getUserCache().findAll()) {
            if (player.getAccount() == null) {
                BotLogger.error("GiveMailCommand: Player=" + player.getUsername() + " id=" + player.getGuid() + " has no account!");
                continue;
            }
            if (target.equalsIgnoreCase("VIP") && player.getAccount().getUserRank() != UserRank.VIP) {
                continue;
            }
            if (target.equalsIgnoreCase("MVP") && player.getAccount().getUserRank() != UserRank.MVP) {
                continue;
            }
            var localAmount = amount;
            if(player.getAccount().getUserRank() == UserRank.ALT || player.getAccount().getUserRank() == UserRank.PALT){
                localAmount = (int)Math.floor(localAmount / 2f);
            }
            if(localAmount > 0){
                UserEmailStorage userEmailStorage = player.getUserEmailStorage();

                Email email = Email.builder()
                        .autoId(userEmailStorage.nextAutoId())
                        .type(2)
                        .name("System")
                        .subject("\uD83C\uDF81 Council Gift")
                        .emailContent(String.format(emailBody, player.getUsername()))
                        .readFlag(0)
                        .date(DateUtil.now())
                        .goods(new ArrayList<>())
                        .guid(-1)
                        .build();

                email.addGood(EmailGood.builder()
                        .goodId(propId)
                        .lockNum(isLock ? localAmount : 0)
                        .num(isLock ? 0 : localAmount)
                        .build());

                userEmailStorage.addEmail(email);

                player.update();
                player.save();

                Optional<LoggedGameUser> optionalGameUser = player.getLoggedGameUser();
                if (optionalGameUser.isPresent()) {
                    optionalGameUser.get().getSmartServer().send(noticePacket);
                    sendMessage("You have received a gift from the council!", player);
                }
            }

        }
        String propN = propData == null || propData.getName() == null ? "Unknown" : propData.getName();
        String s = String.format("GiveMail: %s sent %s with propId=%s %dx to target=%s on %s", user.getUsername(), propN, propId, amount, target, formattedDate);
        DiscordService.getInstance().getRayoBot().sendAudit("System", s, Color.MAGENTA, AuditType.INCIDENT);

    }
}
