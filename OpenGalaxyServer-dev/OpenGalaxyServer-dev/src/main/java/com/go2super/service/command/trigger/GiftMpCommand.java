package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.Email;
import com.go2super.database.entity.sub.EmailGood;
import com.go2super.database.entity.sub.UserEmailStorage;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;
import com.go2super.socket.util.DateUtil;

import java.util.*;

public class GiftMpCommand extends Command {

    public GiftMpCommand() {

        super("giftmp", "permission.giftmp");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'giftmp' need more arguments!", user);
            return;

        }

        int amount = Integer.parseInt(parts[1]);
        String type = parts[2];

        if (!Arrays.asList("MAINT", "MVP", "VIP").contains(type)) {

            sendMessage("Invalid arguments for 'giftmp' command! (giftmp <amount> <MAINT|MVP|VIP>)", user);
            return;

        }

        String emailBody = "Dear Commander %s, \n" +
                           "Thank you for being part of the Alpha, here's a gift for you, we hope it will be very useful for you!";

        switch (type) {
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

        ResponseNewEmailNoticePacket noticePacket = ResponseNewEmailNoticePacket.builder()
            .errorCode(0)
            .build();

        for (User player : UserService.getInstance().getUserCache().findAll()) {

            if (type.equalsIgnoreCase("VIP") && player.getAccount().getUserRank() != UserRank.VIP) {
                continue;
            }

            if (type.equalsIgnoreCase("MVP") && player.getAccount().getUserRank() != UserRank.MVP) {
                continue;
            }

            UserEmailStorage userEmailStorage = player.getUserEmailStorage();

            Email email = Email.builder()
                .autoId(userEmailStorage.nextAutoId())
                .type(4)
                .name("System")
                .subject("\uD83C\uDF81 MP Gift")
                .emailContent(String.format(emailBody, player.getUsername()))
                .readFlag(0)
                .date(DateUtil.now())
                .goods(new ArrayList<>())
                .guid(-1)
                .build();

            email.addGood(EmailGood.builder()
                .goodId(1)
                .num(amount)
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

}
