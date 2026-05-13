package com.go2super.listener;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.CorpMember;
import com.go2super.logger.BotLogger;
import com.go2super.logger.data.UserActionLog;
import com.go2super.obj.game.Prop;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.chat.ChatMessagePacket;
import com.go2super.service.*;
import com.go2super.service.exception.BadGuidException;
import com.go2super.service.jobs.other.DefendJob;
import com.go2super.socket.util.DateUtil;
import org.bson.types.ObjectId;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class ChatListener implements PacketListener {

    public static final int CHANNEL_GLOBAL = 0;
    public static final int CHANNEL_PLANET = 1;
    public static final int CHANNEL_CORPS = 2;
    public static final int CHANNEL_PRIVATE = 3;
    public static final int CHANNEL_TEAM = 4;
    public static final int CHANNEL_MEDUSA = 7;

    public static final int CHAT_COOLDOWN = 1;

    public static final Map<Integer, Integer> corpSpy = new HashMap<>();
    public static final Map<Integer, ObjectId> blockSpy = new HashMap<>();

    public static final Map<Integer, Date> canSendMessage = new HashMap<>();
    public static final Map<Integer, Date> muteList = new HashMap<>();

    private static final String pattern = "MM-dd-yyyy HH:mm:ss";
    private static final DateFormat dateFormat = new SimpleDateFormat(pattern);

    @PacketProcessor
    public void onChat(ChatMessagePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        BotLogger.chat(packet.getGuid() + " :: [CHAT] " + user.getUsername() + " :: " + packet.getBuffer().getValue());

        UserActionLog action = UserActionLog.builder()
            .action("user-chat")
            .message("[Chat] " + user.getUsername() + ": " + packet.getBuffer().getValue())
            .build();

        // EventLogger.sendUserAction(action, user, (GameServerReceiver) packet.getSmartServer());

        User userToSend = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());
        String message = packet.getBuffer().getValue();

        boolean executedCommand = ChatService.getInstance().checkCommand(user, packet.getSmartServer(), message);
        if (executedCommand) {
            return;
        }

        if (muteList.containsKey(user.getGuid())) {
            if (DateUtil.now().before(muteList.get(user.getGuid()))) {
                packet.getSmartServer().sendMessage("You have been muted until " + dateFormat.format(muteList.get(user.getGuid())) + " (Server Time).");
                return;
            }
        }

        Boolean value = packet.getBuffer().getValue().matches("/[\\xCC\\xCD]/");

        if (value) {

            packet.getSmartServer().sendMessage("The text is invalid.");
            return;

        }

        ChatMessagePacket response = new ChatMessagePacket();

        response.setSeqId(packet.getSeqId() + 1);
        response.setSrcUserId(packet.getSrcUserId());
        response.setObjUserId(packet.getObjUserId());
        response.setGuid(packet.getGuid());
        response.setObjGuid(packet.getObjGuid());
        response.setAcronym(-1);
        response.setRank(-1);
        response.setCorpAcronym(WideString.of("", 64));
        response.setChannelType(packet.getChannelType());
        response.setSpecialType(packet.getSpecialType());
        response.setPropsId(packet.getPropsId());
        response.setName(WideString.of(user.getUsername(), 32));
        response.setToName(packet.getToName());
        response.setBuffer(packet.getBuffer());
        response.setResponseTime(System.currentTimeMillis() - packet.getResponseTime());

        Corp userCorp = user.getCorp();

        if (userCorp != null && userCorp.getAcronym() != null && userCorp.getAcronym().length() == 3) {

            response.setAcronym(userCorp.getCorpId());
            response.setCorpAcronym(WideString.of("[" + userCorp.getAcronym() + "]", 64));

        }

        Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findById(user.getAccountId());

        if (optionalAccount.isEmpty()) {

            BotLogger.error("Account not found for user " + user.getGuid());
            return;

        }

        Account account = optionalAccount.get();

        if (account.getUserRank().getPrefix() != -1) {
            response.setRank((short) account.getUserRank().getPrefix());
        }

        switch (packet.getChannelType()) {
            case CHANNEL_GLOBAL:

                if (message.equalsIgnoreCase("/enlist")) {

                    if (!DefendJob.getInstance().isRegister()) {

                        packet.getSmartServer().sendMessage("There is no register opened yet.");
                        return;

                    }

                    if (DefendJob.getInstance().getParticipants().contains(packet.getGuid())) {

                        packet.getSmartServer().sendMessage("You are already enlisted!");
                        return;

                    }

                    if (userCorp == null) {

                        packet.getSmartServer().sendMessage("You need to have a corp to get enlisted!");
                        return;

                    }

                    for (CorpMember corpMember : userCorp.getMembers().getMembers()) {

                        User member = UserService.getInstance().getUserCache().findByGuid(corpMember.getGuid());
                        if (member == null) {
                            continue;
                        }

                        member.sendMessage(">> Corp mate '" + user.getUsername() + "' has been enlisted.");

                    }

                    DefendJob.getInstance().getParticipants().add(packet.getGuid());

                }

                if (canSendMessage.containsKey(user.getGuid())) {
                    if (DateUtil.now().before(canSendMessage.get(user.getGuid()))) {

                        packet.getSmartServer().sendMessage("Exceeded messaging speed limit. Pls try again.");
                        return;

                    }
                }

                Prop prop = user.getInventory().getProp(921);

                if (prop == null || !user.getInventory().removeProp(prop, 1).getKey()) {
                    return;
                }

                user.getMetrics().add("action:send.message", 1);
                user.update();
                user.save();

                canSendMessage.put(user.getGuid(), DateUtil.now(CHAT_COOLDOWN));

                LoginService.getInstance()
                    .getGameUsers()
                    .stream()
                    .forEach(u -> {
                        if (u == null) {
                            return;
                        }
                        u.getSmartServer().send(response);
                    });

                // Audit
                DiscordService.getInstance().getRayoBot().sendAudit("[World] " + user.getUsername() + " ~ " + user.getGuid(), packet.getBuffer().getValue(), Color.decode("0xfafafa"), AuditType.CHAT);

                break;

            case CHANNEL_PLANET:

                try {

                    LoggedGameUser loggedUser = LoginService.getInstance().getGame(packet.getGuid()).get();
                    LoginService.getInstance()
                        .getGameUsers()
                        .stream()
                        .filter(u -> u.getViewing() == loggedUser.getViewing())
                        .forEach(u -> u.getSmartServer().send(response));

                } catch (NoSuchElementException ex) {
                }

                // Audit
                DiscordService.getInstance().getRayoBot().sendAudit("[Planet] " + user.getUsername() + " ~ " + user.getGuid(), packet.getBuffer().getValue(), Color.decode("0xf2c479"), AuditType.CHAT);

                break;

            case CHANNEL_CORPS:

                if (user.getConsortiaId() == -1) {
                    break;
                }

                if (corpSpy.containsKey(user.getGuid())) {

                    Optional<Account> optionalSpyAccount = AccountService.getInstance().getAccountCache().findById(user.getAccountId());
                    if (optionalSpyAccount.isPresent() && optionalSpyAccount.get().getUserRank().hasPermission("permission.spy")) {

                        LoginService.getInstance()
                            .getGameUsers()
                            .stream()
                            .filter(u -> u.getConsortiaId() == corpSpy.get(user.getGuid()))
                            .forEach(u -> u.getSmartServer().send(response));

                        packet.reply(response);
                        return;

                    }

                }

                LoginService.getInstance()
                    .getGameUsers()
                    .stream()
                    .filter(u -> u.getConsortiaId() == user.getConsortiaId())
                    .forEach(u -> u.getSmartServer().send(response));

                for (Map.Entry<Integer, Integer> spy : corpSpy.entrySet()) {

                    if (user.getConsortiaId() != spy.getValue()) {
                        continue;
                    }

                    User spyUser = UserService.getInstance().getUserCache().findByGuid(spy.getKey());
                    if (spyUser == null) {
                        continue;
                    }

                    Optional<LoggedGameUser> loggedGameUser = spyUser.getLoggedGameUser();
                    if (!loggedGameUser.isPresent()) {
                        continue;
                    }

                    Optional<Account> optionalSpyAccount = AccountService.getInstance().getAccountCache().findById(spyUser.getAccountId());
                    if (optionalSpyAccount.isPresent() && optionalSpyAccount.get().getUserRank().hasPermission("permission.spy")) {
                        loggedGameUser.get().getSmartServer().send(response);
                    }

                }

                // Audit
                Corp corpFetch = CorpService.getInstance().getCorpCache().findByCorpId(user.getConsortiaId());

                String corpName = "Unknown";
                if (corpFetch != null) {
                    corpName = corpFetch.getName();
                }

                DiscordService.getInstance().getRayoBot().sendAudit("[Corp / " + corpName + "] " + user.getUsername() + " ~ " + user.getGuid(), packet.getBuffer().getValue(), Color.decode("0xb4ff69"), AuditType.CHAT);

                break;

            case CHANNEL_PRIVATE:

                if (userToSend.isNotDisturb()) {

                    packet.getSmartServer().sendMessage("The user is in do not disturb mode!");
                    return;

                }

                if (userToSend.getBlockUsers() != null) {
                    if (!userToSend.getBlockUsers().isEmpty()) {
                        for (int i = 0; i < userToSend.getBlockUsers().size(); i++) {
                            if (userToSend.getBlockUsers().get(i).intValue() == user.getGuid()) {
                                packet.getSmartServer().sendMessage("The user has blocked you");
                                return;
                            }
                        }
                    }
                }

                LoginService.getInstance()
                    .getGameUsers()
                    .stream()
                    .filter(u -> u.getGuid() == packet.getObjGuid())
                    .limit(1) //should only be one anyways
                    .forEach(u -> {
                        packet.reply(response);
                        u.getSmartServer().send(response);
                    });

                // Audit
                DiscordService.getInstance().getRayoBot().sendAudit("[Private] " + user.getUsername() + " ~ " + user.getGuid() + " > " + userToSend.getUsername() + " ~ " + userToSend.getGuid(), packet.getBuffer().getValue(), Color.decode("0xfd69ff"), AuditType.CHAT);

                break;

            case CHANNEL_TEAM:

                // todo
                if (userCorp == null) {

                    packet.getSmartServer().sendMessage("You are not in a corp!");
                    return;

                }

                ObjectId blockId = null;

                if (blockSpy.containsKey(user.getGuid())) {
                    Optional<Account> optionalSpyAccount = AccountService.getInstance().getAccountCache().findById(user.getAccountId());
                    if (optionalSpyAccount.isPresent() && optionalSpyAccount.get().getUserRank().hasPermission("permission.spy")) {
                        blockId = blockSpy.get(user.getGuid());
                    }
                } else if (userCorp.getBlocId() != null) {
                    blockId = userCorp.getBlocId();
                }


                if (blockId == null) {

                    packet.getSmartServer().sendMessage("Your corp is not in a bloc!");
                    return;

                }

                List<Corp> corps = CorpService.getInstance().getCorpCache().findByBlocId(blockId);

                if (corps.isEmpty()) {
                    packet.getSmartServer().sendMessage("Your corp is not in a bloc!");
                    return;
                }

                CorpMember corpMember = userCorp.getMembers().getMember(user.getGuid());

                if (corpMember.getRank() != 1 && corpMember.getRank() != 2) {
                    packet.getSmartServer().sendMessage("You can't send messages to the bloc!");
                    return;
                }

                String roleName = "Lt.Colonel";

                if (corpMember.getRank() == 1) {
                    roleName = userCorp.getConsortiaJobName().getName1();
                } else if (corpMember.getRank() == 2) {
                    roleName = userCorp.getConsortiaJobName().getName2();
                }

                if (userCorp != null && userCorp.getAcronym() != null && userCorp.getAcronym().length() == 3) {
                    roleName = "[" + userCorp.getAcronym() + "]" + " (" + SmartString.noSpaces(roleName) + ")";
                } else {
                    roleName = "(" + SmartString.noSpaces(roleName) + ")";
                }

                response.setAcronym(userCorp.getCorpId());
                response.setCorpAcronym(WideString.of(roleName, 64));

                corps.stream().forEach(corp -> {
                    corp.getMembers().getMembers().forEach(member -> {
                        if (member.getRank() == 1 || member.getRank() == 2) {
                            LoginService.getInstance()
                                .getGameUsers()
                                .stream()
                                .filter(u -> u.getGuid() == member.getGuid())
                                .forEach(u -> u.getSmartServer().send(response));
                        }
                    });
                });

                for (Map.Entry<Integer, ObjectId> spy : blockSpy.entrySet()) {

                    if (!spy.getValue().equals(blockId)) {
                        continue;
                    }

                    User spyUser = UserService.getInstance().getUserCache().findByGuid(spy.getKey());
                    if (spyUser == null) {
                        continue;
                    }

                    Optional<LoggedGameUser> loggedGameUser = spyUser.getLoggedGameUser();
                    if (!loggedGameUser.isPresent()) {
                        continue;
                    }

                    Optional<Account> optionalSpyAccount = AccountService.getInstance().getAccountCache().findById(spyUser.getAccountId());
                    if (optionalSpyAccount.isPresent() && optionalSpyAccount.get().getUserRank().hasPermission("permission.spy")) {
                        loggedGameUser.get().getSmartServer().send(response);
                    }

                }

                // Audit
                DiscordService.getInstance().getRayoBot().sendAudit("[Bloc] " + user.getUsername() + " ~ " + user.getGuid(), packet.getBuffer().getValue(), Color.decode("0x5adbf2"), AuditType.CHAT);

                break;

        }

    }

}
