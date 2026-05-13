package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.LoginService;
import com.go2super.service.command.Command;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class StatusCommand extends Command {

    public StatusCommand() {

        super("status", "permission.status");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long usedMemory = heapMemoryUsage.getUsed();
        long maxMemory = heapMemoryUsage.getMax();

        sendMessage("[Server status]", user);
        sendMessage("Online Users = " + LoginService.getInstance().getGameUsers().size(), user);
        sendMessage("Active Threads = " + Thread.activeCount(), user);
        sendMessage("Memory = " + humanReadableByteCountSI(usedMemory) + "/" + humanReadableByteCountSI(maxMemory), user);

    }

    public String humanReadableByteCountSI(long bytes) {

        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

}
