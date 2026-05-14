package com.go2super.listener;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.SameIPIncident;
import com.go2super.database.entity.sub.UserSameIPIncidentInfo;
import com.go2super.database.entity.type.UserRank;
import com.go2super.hooks.discord.RayoBot;
import com.go2super.logger.BotLogger;
import com.go2super.logger.data.UserActionLog;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.model.LoggedSessionUser;
import com.go2super.obj.type.AuditType;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.login.GameServerLoginPacket;
import com.go2super.packet.login.PlayerLoginServerValidatePacket;
import com.go2super.packet.login.PlayerLoginTogPacket;
import com.go2super.packet.login.PlayerLoginTolPacket;
import com.go2super.server.GameServerReceiver;
import com.go2super.service.*;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.SocketUtil;
import lombok.SneakyThrows;

import java.awt.*;
import java.util.Date;
import java.util.Optional;

public class LoginListener implements PacketListener {

    private static final int MAX_PLAYERS = 1000;

    private void sendAudit(String message, Color color, AuditType type) {
        try {
            DiscordService discord = DiscordService.getInstance();
            if (discord != null && discord.getRayoBot() != null) {
                discord.getRayoBot().sendAudit(message, "", color, type);
            }
        } catch (Exception e) {
            BotLogger.dev("Discord audit failed: " + e.getMessage());
        }
    }

    @SneakyThrows
    @PacketProcessor
    public void onLoginTol(PlayerLoginTolPacket packet) {

        LoginService loginService = LoginService.getInstance();
        Optional<LoggedSessionUser> sessionUser = loginService.getSession(packet.getUserId());

        if (sessionUser.isEmpty()) {
            cancel(packet);
            sendAudit("ID[`" + packet.getUserId() + "`] failed login: 1\nSession is not found for user. sessionKey=" + packet.getSessionKey(), Color.red, AuditType.LOGIN);
            return;
        }

        LoggedSessionUser session = sessionUser.get();
        String sessionKey = session.getSessionKey();

        if (!packet.getSessionKey().shrink(25).equals(sessionKey)) {
            cancel(packet);
            sendAudit("ID[`" + packet.getUserId() + "`] failed login: 2\nSession Key not found for user", Color.red, AuditType.LOGIN);
            return;
        }

        User user = session.getUser();
        if (user == null) {
            sendAudit("ID[`" + packet.getUserId() + "`] failed login: 3\nSession is not within this user", Color.red, AuditType.LOGIN);
            return;
        }

        Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findById(user.getAccountId());
        if (optionalAccount.isEmpty()) {
            sendAudit("ID[`" + packet.getUserId() + "`] failed login: 4\nAccount info not able to be fetched under this user", Color.red, AuditType.LOGIN);
            return;
        }

        loginService.disconnectGame(session);

        if (!PacketService.getInstance().isLogin()) {
            if (!PacketService.getInstance().getWhitelist().contains(user.getId().toString())) {
                cancel(packet);
                sendAudit("ID[`" + packet.getUserId() + "`] failed login: 5\nToken invalid for user", Color.red, AuditType.LOGIN);
                return;
            }
            // packet.reply();
        }

        PlayerLoginServerValidatePacket response = new PlayerLoginServerValidatePacket();

        response.setPort(5051);
        response.setUserId(packet.getUserId());
        response.getIp().setValue(PacketService.getInstance().getExternalIp());
        response.getSessionKey().setValue(sessionKey);

        packet.reply(response);
        BotLogger.login("Login (Name: " + user.getUsername() + ", Id: " + user.getGuid() + ", IP: " + packet.getUserIp() + ")");

        // Audit
        Account account = optionalAccount.get();

        String buffer = "**Login:** `" + user.getUsername() + " (ID: " + user.getGuid() + ", EMAIL: " + account.getEmail() + ")`\n";

        sendAudit(buffer, Color.green, AuditType.LOGIN);
    }

    @PacketProcessor
    public void onLoginTog(PlayerLoginTogPacket packet) {

        LoginService loginService = LoginService.getInstance();
        Optional<LoggedSessionUser> optionalSessionUser = loginService.getSession(packet.getUserId());

        if (!optionalSessionUser.isPresent()) {
            cancel(packet);
            return;
        }

        LoggedSessionUser sessionUser = optionalSessionUser.get();
        String sessionKey = sessionUser.getSessionKey();

        if (!packet.getSessionKey().shrink(25).equals(sessionKey)) {
            cancel(packet);
            return;
        }

        Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findById(sessionUser.getUser().getAccountId());
        if (optionalAccount.isEmpty()) {
            return;
        }

        // * Disconnect old session
        loginService.disconnectGame(sessionUser);

        Account account = optionalAccount.get();
        int error = 0;

        int userCount = LoginService.getInstance().getGameUsers().size();
        if (PacketService.getInstance().isMaintenance() && (account.getUserRank() != UserRank.ADMIN && (account.getMaintenanceBypass() == null || !account.getMaintenanceBypass()))) {
            error = 3;
        } else if (userCount + 1 > MAX_PLAYERS) {
            sendAudit("ID[`" + packet.getUserId() + "`] failed login: 6\nServer is full\n User Count" + userCount, Color.red, AuditType.LOGIN);
            error = 4;
        } else if (account.getBanUntil() != null) {
            if (DateUtil.now().before(account.getBanUntil())) {
                error = 7;
            } else {
                account.setBanUntil(null);
                AccountService.getInstance().getAccountCache().save(account);
            }
        }

        if (error > 0) {

            GameServerLoginPacket response = new GameServerLoginPacket();

            response.setError((byte) error);
            response.setGuid(0);
            response.setGuide(1);

            packet.reply(response);
            return;

        }

        // * Make new logged user
        LoggedGameUser gameUser = loginService.login(sessionUser, packet);
        User updatedUser = gameUser.getUpdatedUser();

        GameServerLoginPacket response = new GameServerLoginPacket();

        response.setGuid(gameUser.getGuid());
        response.setGuide(1);

        // * Put guid to the receiver
        GameServerReceiver serverReceiver = (GameServerReceiver) packet.getSmartServer();

        serverReceiver.setAccountId(account.getId().toString());
        serverReceiver.setAccountEmail(account.getEmail());
        serverReceiver.setAccountName(account.getUsername());

        serverReceiver.setHostname(serverReceiver.getSocket().getInetAddress().getHostName());
        serverReceiver.setIp(SocketUtil.getIpAddress(serverReceiver.getSocket().getInetAddress().getAddress()));
        serverReceiver.setPort(serverReceiver.getSocket().getPort());

        serverReceiver.setUserId(gameUser.getUserId());
        serverReceiver.setGuid(gameUser.getGuid());

        serverReceiver.setLoginTime(new Date());
        serverReceiver.setUserMaxPpt(updatedUser.getUserMaxPpt());

        Thread.currentThread().setName("game-receiver-guid-" + gameUser.getGuid());

        if (account.getDiscordHook() != null && account.getDiscordHook().getDiscordId() != null) {
            serverReceiver.setDiscordId(account.getDiscordHook().getDiscordId());
        }
        packet.reply(response);
        //deleted logging codes which originally used for checking alts by IP
    }

    @SneakyThrows
    public void cancel(PlayerLoginTolPacket packet) {

        packet.getSocket().close();
    }

    @SneakyThrows
    public void cancel(PlayerLoginTogPacket packet) {

        packet.getSocket().close();
    }

}
