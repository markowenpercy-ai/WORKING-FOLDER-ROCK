package com.go2super.service;

import com.go2super.database.cache.CommanderCache;
import com.go2super.database.entity.Commander;
import com.go2super.hooks.discord.RayoBot;
import com.go2super.logger.BotLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// Can't believe I gave these fuckers a variance command then immediately had to audit it because some idiot thought a 1mil variance dict was a good idea
// it was me, im the idiot
// this shit doesnt work for teh stats only the variance

@Slf4j
@Component
public class CommanderAuditor implements ApplicationRunner {

    private final CommanderCache commanderCache;
    private final DiscordService discordService;

    public CommanderAuditor(CommanderCache commanderCache, DiscordService discordService) {
        this.commanderCache = commanderCache;
        this.discordService = discordService;
    }

    @Override
    public void run(ApplicationArguments args) {
        auditCommanders();
    }

    public void auditCommanders() {
        List<String> fixes = new ArrayList<>();
        int totalChecked = 0;
        int varianceFixed = 0;
        int growthAimFixed = 0;
        int growthDodgeFixed = 0;
        int growthSpeedFixed = 0;
        int growthElectronFixed = 0;

        for (Commander commander : commanderCache.findAll()) {
            totalChecked++;
            boolean needsSave = false;

            if (commander.getVariance() > CommanderService.MAX_VARIANCE || commander.getVariance() < -CommanderService.MAX_VARIANCE) {
                int oldVariance = commander.getVariance();
                commander.setVariance(oldVariance);
                varianceFixed++;
                needsSave = true;
                fixes.add(String.format("Commander %s (ID: %d): Variance %d -> %d",
                    commander.getName(), commander.getCommanderId(), oldVariance, commander.getVariance()));
            }

            if (commander.getGrowthAim() < 0 || commander.getGrowthAim() > CommanderService.MAX_GROWTH) {
                int oldValue = commander.getGrowthAim();
                commander.setGrowthAim(commander.getGrowthAim());
                growthAimFixed++;
                needsSave = true;
                fixes.add(String.format("Commander %s (ID: %d): GrowthAim %d -> %d",
                    commander.getName(), commander.getCommanderId(), oldValue, commander.getGrowthAim()));
            }

            if (commander.getGrowthDodge() < 0 || commander.getGrowthDodge() > CommanderService.MAX_GROWTH) {
                int oldValue = commander.getGrowthDodge();
                commander.setGrowthDodge(commander.getGrowthDodge());
                growthDodgeFixed++;
                needsSave = true;
                fixes.add(String.format("Commander %s (ID: %d): GrowthDodge %d -> %d",
                    commander.getName(), commander.getCommanderId(), oldValue, commander.getGrowthDodge()));
            }

            if (commander.getGrowthSpeed() < 0 || commander.getGrowthSpeed() > CommanderService.MAX_GROWTH) {
                int oldValue = commander.getGrowthSpeed();
                commander.setGrowthSpeed(commander.getGrowthSpeed());
                growthSpeedFixed++;
                needsSave = true;
                fixes.add(String.format("Commander %s (ID: %d): GrowthSpeed %d -> %d",
                    commander.getName(), commander.getCommanderId(), oldValue, commander.getGrowthSpeed()));
            }

            if (commander.getGrowthElectron() < 0 || commander.getGrowthElectron() > CommanderService.MAX_GROWTH) {
                int oldValue = commander.getGrowthElectron();
                commander.setGrowthElectron(commander.getGrowthElectron());
                growthElectronFixed++;
                needsSave = true;
                fixes.add(String.format("Commander %s (ID: %d): GrowthElectron %d -> %d",
                    commander.getName(), commander.getCommanderId(), oldValue, commander.getGrowthElectron()));
            }

            if (needsSave) {
                commander.save();
            }
        }

        int totalFixes = varianceFixed + growthAimFixed + growthDodgeFixed + growthSpeedFixed + growthElectronFixed;

        StringBuilder message = new StringBuilder();
        message.append("**Commander Audit Report**\n\n");
        message.append("Total Commanders Checked: ").append(totalChecked).append("\n");
        message.append("Total Fixes Applied: ").append(totalFixes).append("\n\n");
        message.append("Fixes breakdown:\n");
        message.append("- Variance fixes: ").append(varianceFixed).append("\n");
        message.append("- GrowthAim fixes: ").append(growthAimFixed).append("\n");
        message.append("- GrowthDodge fixes: ").append(growthDodgeFixed).append("\n");
        message.append("- GrowthSpeed fixes: ").append(growthSpeedFixed).append("\n");
        message.append("- GrowthElectron fixes: ").append(growthElectronFixed).append("\n\n");
        message.append("Limits enforced:\n");
        message.append("- MAX_VARIANCE: ").append(CommanderService.MAX_VARIANCE).append("\n");
        message.append("- MAX_GROWTH: ").append(CommanderService.MAX_GROWTH).append("\n");
        message.append("- MAX_RESET: ").append(CommanderService.MAX_RESET).append("\n");

        if (!fixes.isEmpty()) {
            message.append("\n**Fixed Commanders:**\n");
            for (String fix : fixes) {
                message.append("- ").append(fix).append("\n");
            }
        }

        BotLogger.info("CommanderAuditor: Checked " + totalChecked + " commanders, applied " + totalFixes + " fixes");

        try {
            RayoBot rayoBot = discordService.getRayoBot();
            if (rayoBot != null) {
                rayoBot.sendAudit("Commander Audit", message.toString(), Color.GREEN, com.go2super.obj.type.AuditType.INCIDENT);
            }
        } catch (Exception e) {
            BotLogger.error("Failed to send commander audit to Discord: " + e.getMessage());
        }
    }
}