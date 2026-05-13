package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.BattleService;
import com.go2super.service.command.Command;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class PerformanceCommand extends Command {

    public PerformanceCommand() {

        super("performance", "permission.performance");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long usedMemory = heapMemoryUsage.getUsed();
        long maxMemory = heapMemoryUsage.getMax();

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double loadAverage = osBean.getSystemLoadAverage();

        smartServer.sendMessage("[Server Performance]");
        smartServer.sendMessage("Peak: " + maxPeak());
        smartServer.sendMessage("Battles = " + BattleService.getInstance().getBattles().size());
        smartServer.sendMessage("Threads = " + Thread.activeCount());
        smartServer.sendMessage(String.format("LAV: %.2f", loadAverage)); //fuck i lovelav
        smartServer.sendMessage("Memory = " + humanReadableByteCountSI(usedMemory) + "/" + humanReadableByteCountSI(maxMemory));

    }

    public String maxPeak() {

        return SmartServer.maxPeak.getTaskName() + " (" + SmartServer.maxPeak.getTime() + " ms)";
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
