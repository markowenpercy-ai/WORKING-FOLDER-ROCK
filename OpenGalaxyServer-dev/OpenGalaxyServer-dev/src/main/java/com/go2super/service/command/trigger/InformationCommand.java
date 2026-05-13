package com.go2super.service.command.trigger;

import com.go2super.Go2SuperApplication;
import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.command.Command;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.*;

// why the fuck is there a command for this

public class InformationCommand extends Command {

    public InformationCommand() {

        super("info", "permission.info", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        long millis = new Date().getTime() - Go2SuperApplication.UPTIME_DATE.getTime();

        sendMessage("[Server information]", user);
        sendMessage("Version = " + Go2SuperApplication.VIRTUAL_VERSION, user);
        sendMessage("Uptime = " + DurationFormatUtils.formatDurationHMS(millis), user);

    }

}
