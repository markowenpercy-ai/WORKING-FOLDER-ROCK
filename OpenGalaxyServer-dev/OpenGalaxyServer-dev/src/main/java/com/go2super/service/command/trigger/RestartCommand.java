package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.type.AuditType;
import com.go2super.service.ChatService;
import com.go2super.service.DiscordService;
import com.go2super.service.JobService;
import com.go2super.service.command.Command;
import com.go2super.socket.util.TimeReader;

import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.*;

public class RestartCommand extends Command {

    public ScheduledFuture nextScheduledRestart;
    private int leftSeconds;

    public static final TimeReader timeReader =
        new TimeReader()
            .addUnit("h", 3600000L)
            .addUnit("m", 60000L)
            .addUnit("s", 1000L);

    public RestartCommand() {

        super("restart", "permission.restart");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 1) {
            String s = String.format("A restart was issued now by user=%s id=%s", user.getUsername(), user.getGuid());
            DiscordService.getInstance().getRayoBot().sendAudit("[RESTART ISSUED]", s, Color.decode("0xfafafa"), AuditType.INCIDENT);
            ChatService.getInstance().broadcastMessage("Restarting server...");
            try {
                Runtime.getRuntime().exec("cmd /c start \"\" start.bat");
            } catch (IOException e) {
                //do nothing
            }
            System.exit(0);

            return;

        }

        if (nextScheduledRestart != null) {

            nextScheduledRestart.cancel(false);

            leftSeconds = 0;
            nextScheduledRestart = null;
            String s = String.format("Scheduled maintenance was cancelled by user=%s id=%s", user.getUsername(), user.getGuid());
            DiscordService.getInstance().getRayoBot().sendAudit("[RESTART ISSUED]", s, Color.decode("0xfafafa"), AuditType.INCIDENT);
            ChatService.getInstance().broadcastMessage("Scheduled restart has been cancelled.");
            return;

        }

        String delayToRestart = "";
        for (int i = 1; i < parts.length; i++) {
            delayToRestart += parts[i];
        }

        long parsed = timeReader.parse(delayToRestart);
        leftSeconds = (int) (parsed / 1000);

        if (leftSeconds <= 0) {
            String s = String.format("Scheduled maintenance was cancelled by user=%s id=%s", user.getUsername(), user.getGuid());
            DiscordService.getInstance().getRayoBot().sendAudit("[RESTART ISSUED]", s, Color.decode("0xfafafa"), AuditType.INCIDENT);
            ChatService.getInstance().broadcastMessage("Restarting server...");
            try {
                Runtime.getRuntime().exec("cmd /c start \"\" start.bat");
            } catch (IOException e) {
                //do nothing
            }
            System.exit(0);
            return;

        }
        String s = String.format("Scheduled maintenance was cancelled by user=%s id=%s", user.getUsername(), user.getGuid());
        DiscordService.getInstance().getRayoBot().sendAudit("[RESTART ISSUED]", s, Color.decode("0xfafafa"), AuditType.INCIDENT);
        nextScheduledRestart = JobService.getInstance().getTaskScheduler().scheduleAtFixedRate(() -> {

            leftSeconds--;

            if (leftSeconds == 60 * 5) {
                ChatService.getInstance().broadcastMessage("Restarting server in 5 minutes...");
            } else if (leftSeconds == 60) {
                ChatService.getInstance().broadcastMessage("Restarting server in 1 minute...");
            } else if (leftSeconds <= 5 && leftSeconds > 0) {
                if (leftSeconds == 1) {
                    ChatService.getInstance().broadcastMessage("Restarting server in 1 second...");
                } else {
                    ChatService.getInstance().broadcastMessage("Restarting server in " + leftSeconds + " seconds...");
                }
            } else if (leftSeconds == 0) {

                ChatService.getInstance().broadcastMessage("Restarting server...");
                try {
                    Runtime.getRuntime().exec("cmd /c start \"\" start.bat");
                } catch (IOException e) {
                    //do nothing
                }
                System.exit(0);

            }

        }, Duration.ofSeconds(1));

    }

}
