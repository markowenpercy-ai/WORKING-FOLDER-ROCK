package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.packet.login.GameServerLoginPacket;
import com.go2super.service.*;
import com.go2super.service.command.Command;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.TimeReader;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class MaintenanceCommand extends Command {

    public ScheduledFuture nextScheduledMaintenance;
    private int leftSeconds;

    public static final TimeReader timeReader =
        new TimeReader()
            .addUnit("h", 3600000L)
            .addUnit("m", 60000L)
            .addUnit("s", 1000L);

    private static final String pattern = "MM-dd-yyyy HH:mm:ss";
    private static final DateFormat dateFormat = new SimpleDateFormat(pattern);

    public MaintenanceCommand() {

        super("maintenance", "permission.maintenance");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 1) {
            
            PacketService.DYNAMIC_MAINTENANCE = true;
            ChatService.getInstance().broadcastMessage("The server is shutting down due to maintenance...");
            String s = String.format("A maintenance was issued now by user=%s id=%s", user.getUsername(), user.getGuid());
            DiscordService.getInstance().getRayoBot().sendAudit("[MAINTENANCE ISSUED]", s, Color.decode("0xfafafa"), AuditType.INCIDENT);

            for (LoggedGameUser loggedGameUser : LoginService.getInstance().getGameUsers()) {

                GameServerLoginPacket maintenanceScreenPacket = new GameServerLoginPacket();

                maintenanceScreenPacket.setError((byte) 3);
                maintenanceScreenPacket.setGuid(0);
                maintenanceScreenPacket.setGuide(1);

                loggedGameUser.getSmartServer().send(maintenanceScreenPacket);

            }

            JobService.getInstance().getTaskScheduler().scheduleWithFixedDelay(() -> {
                BattleService.getInstance().stopAllMatches();
                System.exit(0);
            }, Duration.ofSeconds(5));
            return;

        }

        if (nextScheduledMaintenance != null) {

            nextScheduledMaintenance.cancel(false);

            leftSeconds = 0;
            nextScheduledMaintenance = null;

            ChatService.getInstance().broadcastMessage("Scheduled maintenance has been cancelled.");
            String s = String.format("Scheduled maintenance was cancelled by user=%s id=%s", user.getUsername(), user.getGuid());
            DiscordService.getInstance().getRayoBot().sendAudit("[MAINTENANCE CANCELLED]", s, Color.decode("0xfafafa"), AuditType.INCIDENT);
            return;

        }

        String delayToRestart = "";
        for (int i = 1; i < parts.length; i++) {
            delayToRestart += parts[i];
        }

        long parsed = timeReader.parse(delayToRestart);
        leftSeconds = (int) (parsed / 1000);

        if (leftSeconds <= 0) {

            PacketService.DYNAMIC_MAINTENANCE = true;

            for (LoggedGameUser loggedGameUser : LoginService.getInstance().getGameUsers()) {

                GameServerLoginPacket maintenanceScreenPacket = new GameServerLoginPacket();

                maintenanceScreenPacket.setError((byte) 3);
                maintenanceScreenPacket.setGuid(0);
                maintenanceScreenPacket.setGuide(1);

                loggedGameUser.getSmartServer().send(maintenanceScreenPacket);

            }

            JobService.getInstance().getTaskScheduler().scheduleWithFixedDelay(() -> {
                BattleService.getInstance().stopAllMatches();
                System.exit(0);
            }, Duration.ofSeconds(5));
            return;

        }

        Date until = DateUtil.now(leftSeconds);
        ChatService.getInstance().broadcastMessage("A maintenance is scheduled for " + dateFormat.format(until) + " (Server Time)!");
        String s = String.format("A maintenance was scheduled for %s by user=%s id=%s", dateFormat.format(until), user.getUsername(), user.getGuid());
        DiscordService.getInstance().getRayoBot().sendAudit("[MAINTENANCE ISSUED]", s, Color.decode("0xfafafa"), AuditType.INCIDENT);

        nextScheduledMaintenance = JobService.getInstance().getTaskScheduler().scheduleAtFixedRate(() -> {

            leftSeconds--;

            if (leftSeconds == 60 * 5) {
                ChatService.getInstance().broadcastMessage("Shutting down server in 5 minutes...");
            } else if (leftSeconds == 60) {
                ChatService.getInstance().broadcastMessage("Shutting down server in 1 minute...");
            } else if (leftSeconds <= 5 && leftSeconds > 0) {
                if (leftSeconds == 1) {
                    ChatService.getInstance().broadcastMessage("Shutting down server in 1 second...");
                } else {
                    ChatService.getInstance().broadcastMessage("Shutting down server in " + leftSeconds + " seconds...");
                }
            } else if (leftSeconds == 0) {

                PacketService.DYNAMIC_MAINTENANCE = true;
                ChatService.getInstance().broadcastMessage("The server is shutting down due to maintenance...");

                for (LoggedGameUser loggedGameUser : LoginService.getInstance().getGameUsers()) {

                    GameServerLoginPacket maintenanceScreenPacket = new GameServerLoginPacket();

                    maintenanceScreenPacket.setError((byte) 3);
                    maintenanceScreenPacket.setGuid(0);
                    maintenanceScreenPacket.setGuide(1);

                    loggedGameUser.getSmartServer().send(maintenanceScreenPacket);

                }

                JobService.getInstance().getTaskScheduler().scheduleWithFixedDelay(() -> {
                    BattleService.getInstance().stopAllMatches();
                    System.exit(0);
                }, Duration.ofSeconds(5));

            }

        }, Duration.ofSeconds(1));

    }

}
