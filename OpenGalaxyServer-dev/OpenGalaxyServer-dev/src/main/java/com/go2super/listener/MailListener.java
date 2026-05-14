package com.go2super.listener;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.Email;
import com.go2super.database.entity.sub.EmailGood;
import com.go2super.database.entity.sub.UserEmailStorage;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.EmailInfo;
import com.go2super.obj.game.ReadEmail;
import com.go2super.obj.type.AuditType;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.VariableType;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.mail.*;
import com.go2super.service.DiscordService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MailListener implements PacketListener {

    @PacketProcessor
    public void onSendEmail(RequestSendEmailPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        User userToSendEmail = UserService.getInstance().getUserCache().findByGuid(packet.getSendGuid());

        if (userToSendEmail == null) {
            return;
        }
        if (userToSendEmail.getGuid() == user.getGuid()) {
            return;
        }
        if (packet.getSubject().noSpaces().equals("") || packet.getSubject().noSpaces() == null) {
            return;
        }
        if (packet.getContent().noSpaces().equals("") || packet.getSubject().noSpaces() == null) {
            return;
        }

        List<Integer> blockUsers = userToSendEmail.getBlockUsers();
        if (!CollectionUtils.isEmpty(blockUsers)) {
            for (Integer blockUser : blockUsers) {
                if (blockUser == user.getGuid()) {
                    return;
                }
            }
        }

        user.getMetrics().add("action:send.email", 1);
        user.update();
        user.save();

        if (userToSendEmail.getUserEmailStorage().getUserEmails().size() >= 49) {
            return;
        }

        Email newEmail = Email.builder()
                .autoId(userToSendEmail.getUserEmailStorage().nextAutoId())
                .type((byte) 0)
                .readFlag((byte) 0)
                .subject(packet.getSubject().noSpaces().replaceAll("[^a-zA-Z0-9 ]", ""))
                .emailContent(packet.getContent().noSpaces().replaceAll("[^a-zA-Z0-9 ]", ""))
                .name(user.getUsername())
                .guid(user.getGuid())
                .date(DateUtil.now())
                .build();

        userToSendEmail.getUserEmailStorage().addEmail(newEmail);

        if (userToSendEmail.isOnline()) {

            ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();

            userToSendEmail.getLoggedGameUser().get().getSmartServer().send(response);

        }

        userToSendEmail.update();
        userToSendEmail.save();

    }

    @PacketProcessor
    public void onEmailInfo(RequestEmailInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserEmailStorage userEmailStorage = user.getUserEmailStorage();
        List<Email> emails = userEmailStorage.getSortedEmails();

        List<EmailInfo> emailsInfo = new ArrayList<>();
        int type = packet.getKind();

        for (int i = packet.getPageId() * 9; i < ((packet.getPageId() + 1) * 9); i++) {

            if (userEmailStorage.getUserEmails().size() > i) {

                Email email = emails.get(i);

                if (type == 65280) {
                    if (email.getType() != 0) {
                        continue;
                    }
                }

                if (type == 65281) {
                    if (email.getType() != 1) {
                        continue;
                    }
                }

                if (type == 65282) {
                    if (email.getType() != 2 && email.getType() != 3 && email.getType() != 4) {
                        continue;
                    }
                }

                if (type == 65285) {
                    if (email.getType() != 5) {
                        continue;
                    }
                }

                if (type == 255) {
                    if (email.getReadFlag() != 0) {
                        continue;
                    }
                }

                String emailSubject = email.getSubject();
                if (emailSubject != null && emailSubject.length() > VariableType.MAX_NAME) {
                    emailSubject = emailSubject.substring(0, VariableType.MAX_NAME);
                }

                EmailInfo emailInfo = new EmailInfo(
                        emailSubject != null ? emailSubject : "",
                        email.getName(),
                        DateUtil.secondsFromOrigin(email.getDate()),
                        email.getGuid(),
                        email.getGuid(),
                        email.getAutoId(),
                        email.getFightGalaxyId(),
                        (byte) email.getType(), // 0 = user, 1 = war report, 2 = verde (auction prop), 3 = verde (auction shipmodel), 4 = verde (auction resource), 5 = system, 6 = user
                        (byte) email.getReadFlag(), // 0 = Unread, 1 = Read
                        (byte) (email.hasGoods() ? 1 : 0), // 0 = No has good, 1 = has good
                        (byte) (email.getMailType() == null ? 0 : email.getMailType().getTitleType())); // mail title ???)

                emailsInfo.add(emailInfo);

            }
        }

        ResponseEmailInfoPacket response = ResponseEmailInfoPacket.builder()
                .dataLen((short) emailsInfo.size())
                .emailCount((short) user.getUserEmailStorage().getUserEmails().size())
                .data(emailsInfo)
                .build();

        user.save();
        packet.reply(response);

    }

    @PacketProcessor
    public void onReadEmail(RequestReadEmailPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null && packet.getGuid() != -1) {
            return;
        }

        UserEmailStorage userEmailStorage = user.getUserEmailStorage();
        Email email = userEmailStorage.getEmail(packet.getAutoId());
        if (email == null) {
            return;
        }
        List<ReadEmail> goods = new ArrayList<>();

        if (packet.getFightFlag() == 1 || packet.getFightFlag() == 2) {
            email.setReadFlag(1);
            user.save();
            packet.reply(email.getFightResultPacket().to());
            return;
        }

        if (email.getGoods() != null && !email.getGoods().isEmpty()) {
            for (EmailGood emailGood : email.getGoods()) {
                if (email.getType() == 3) {

                    ShipModel shipModel = PacketService.getShipModel(emailGood.getGoodId());

                    if (shipModel == null) {
                        continue;
                    }

                    goods.add(new ReadEmail(shipModel.getShipModelId(), emailGood.getNum(), (short) 0, (short) shipModel.getBodyId()));
                    packet.reply(PacketService.getInstance().getTemporalShipModel(shipModel.getShipModelId()));

                } else {

                    goods.add(new ReadEmail(emailGood.getGoodId(), emailGood.getNum(), (short) emailGood.getLockNum(), (short) 0));

                }
            }
        }

        ResponseReadEmailPacket response = ResponseReadEmailPacket.builder()
                .autoId(packet.getAutoId())
                .content(SmartString.of(email.getEmailContent(), VariableType.MAX_EMAILCONTENT))
                .dataLen(goods.size())
                .data(goods)
                .build();

        email.setReadFlag(1);

        user.save();
        packet.reply(response);

    }

    @PacketProcessor
    public void onDeleteEmail(RequestDeleteEmailPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserEmailStorage emailStorage = user.getUserEmailStorage();
        List<Email> emails = emailStorage.getUserEmails();

        Email email = emailStorage.getEmail(packet.getAutoId());
        emails.remove(email);

        user.update();
        user.save();

        if (email.hasGoods()) {

            // Audit
            Account account = user.getAccount();

            String buffer = "**User:** `" + user.getUsername() + " (ID: " + user.getGuid() + ", EMAIL: " + account.getEmail() + ")`\n" +
                    "**Goods Quantity:** `" + email.getGoods().size();

            DiscordService.getInstance().getRayoBot().sendAudit("Email With Goods Delete", buffer, Color.decode("0xd0e33d"), AuditType.DELETE);

        }

    }

    @PacketProcessor
    public void onEmailGoodsPacket(RequestEmailGoodsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        // BotLogger.log(packet);

        UserEmailStorage emailStorage = user.getUserEmailStorage();
        Email email = emailStorage.getEmail(packet.getAutoId());
        if (email == null || email.getGoods() == null || email.getGoods().isEmpty()) {
            return;
        }

        // Email with Props
        if (email.getType() == 2) {

            if (packet.getPropsId() == -1) {

                List<EmailGood> goods = email.getGoods();

                for (EmailGood emailGood : goods) {

                    if (emailGood.getNum() > 0) {
                        user.getInventory().addProp(emailGood.getGoodId(), emailGood.getNum(), 0, false);
                    }

                    if (emailGood.getLockNum() > 0) {
                        user.getInventory().addProp(emailGood.getGoodId(), emailGood.getLockNum(), 0, true);
                    }

                }

                email.getGoods().clear();

            } else {

                EmailGood emailGood = email.getEmailGood(packet.getPropsId());

                if (emailGood.getNum() > 0) {
                    user.getInventory().addProp(emailGood.getGoodId(), emailGood.getNum(), 0, false);
                }

                if (emailGood.getLockNum() > 0) {
                    user.getInventory().addProp(emailGood.getGoodId(), emailGood.getLockNum(), 0, true);
                }

                email.getGoods().remove(emailGood);

            }

        }

        // Email with Ships
        if (email.getType() == 3) {

            if (packet.getPropsId() == -1) {

                List<EmailGood> goods = email.getGoods();

                for (EmailGood emailGood : goods) {

                    UserShips userShips = user.getShips();

                    if (userShips.countStoredShips() + emailGood.getNum() >= ShipFactoryListener.MAX_SHIPS) {
                        return;
                    }

                    userShips.addShip(emailGood.getGoodId(), emailGood.getNum());

                }

                email.getGoods().clear();

            } else {

                EmailGood emailGood = email.getEmailGood(packet.getPropsId());
                UserShips userShips = user.getShips();

                if (userShips.countStoredShips() + emailGood.getNum() >= ShipFactoryListener.MAX_SHIPS) {
                    return;
                }

                userShips.addShip(emailGood.getGoodId(), emailGood.getNum());
                email.getGoods().remove(emailGood);

            }

        }

        // Email with Resources
        if (email.getType() == 4) {

            if (packet.getPropsId() == -1) {

                List<EmailGood> goods = email.getGoods();

                for (EmailGood emailGood : goods) {

                    switch (emailGood.getGoodId()) {

                        case 0: // 0 -> gold

                            user.getResources().addGold(emailGood.getNum());
                            break;

                        case 1: //1 -> mallPoints

                            user.getResources().addMallPoints(emailGood.getNum());
                            break;

                        case 2: //2 -> metal

                            user.getResources().addMetal(emailGood.getNum());
                            break;

                        case 3: //3 -> he3

                            user.getResources().addHe3(emailGood.getNum());
                            break;

                        case 4: //4 -> vouchers

                            user.getResources().addVouchers(emailGood.getNum());
                            break;

                    }

                }

                email.getGoods().clear();

            } else {

                EmailGood emailGood = email.getEmailGood(packet.getPropsId());

                switch (emailGood.getGoodId()) {

                    case 0: // 0 -> gold

                        user.getResources().addGold(emailGood.getNum());
                        break;

                    case 1: //1 -> mallPoints

                        user.getResources().addMallPoints(emailGood.getNum());
                        break;

                    case 2: //2 -> metal

                        user.getResources().addMetal(emailGood.getNum());
                        break;

                    case 3: //3 -> he3

                        user.getResources().addHe3(emailGood.getNum());
                        break;

                    case 4: //4 -> vouchers

                        user.getResources().addVouchers(emailGood.getNum());
                        break;

                }

                email.getGoods().remove(emailGood);

            }

        }

        ResponseEmailGoodsPacket response = ResponseEmailGoodsPacket.builder()
                .autoId(emailStorage.getAutoId(email))
                .propsId(packet.getPropsId())
                .build();

        user.save();
        packet.reply(response);


    }

}
