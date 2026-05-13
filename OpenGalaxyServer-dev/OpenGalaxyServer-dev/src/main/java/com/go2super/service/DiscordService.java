package com.go2super.service;

import com.go2super.hooks.discord.RayoBot;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiscordService {

    private static DiscordService instance;

    @Getter
    private final String discordToken;
    @Getter
    private final String staffChannel;
    @Getter
    private final String cmdChannel;
    @Getter
    private final String guild;
    @Getter
    private final String owner;

    @Getter
    private final String auditChannel;
    @Getter
    private final String incidentAuditChannel;
    @Getter
    private final String chatAuditChannel;
    @Getter
    private final String tradeAuditChannel;
    @Getter
    private final String mergeAuditChannel;
    @Getter
    private final String deleteAuditChannel;
    @Getter
    private final String loginAuditChannel;
    @Getter
    private final String commanderAuditChannel;

    @Getter
    private final RayoBot rayoBot;

    private final List<Thread> bots = new ArrayList<>();

    public DiscordService(@Value("${application.discord.discord-token}") String discordToken,
                          @Value("${application.discord.staff-channel}") String staffChannel,
                          @Value("${application.discord.cmd-channel}") String cmdChannel,
                          @Value("${application.discord.guild}") String guild,
                          @Value("${application.discord.owner}") String owner,
                          @Value("${application.discord.audit-channel}") String auditChannel,
                          @Value("${application.discord.incident-audit-channel}") String incidentAuditChannel,
                          @Value("${application.discord.chat-audit-channel}") String chatAuditChannel,
                          @Value("${application.discord.trade-audit-channel}") String tradeAuditChannel,
                          @Value("${application.discord.merge-audit-channel}") String mergeAuditChannel,
                          @Value("${application.discord.delete-audit-channel}") String deleteAuditChannel,
                          @Value("${application.discord.login-audit-channel}") String loginAuditChannel,
                          @Value("${application.discord.commander-audit-channel}") String commanderAuditChannel) {

        instance = this;
        rayoBot = new RayoBot();

        this.discordToken = discordToken;
        this.staffChannel = staffChannel;
        this.cmdChannel = cmdChannel;
        this.guild = guild;
        this.owner = owner;

        this.auditChannel = auditChannel;

        this.incidentAuditChannel = incidentAuditChannel;
        this.chatAuditChannel = chatAuditChannel;
        this.tradeAuditChannel = tradeAuditChannel;
        this.mergeAuditChannel = mergeAuditChannel;
        this.deleteAuditChannel = deleteAuditChannel;
        this.loginAuditChannel = loginAuditChannel;
        this.commanderAuditChannel = commanderAuditChannel;

        Thread rayoThread = new Thread(() -> rayoBot.start(discordToken));

        rayoThread.setName("rayo-bot-thread");
        rayoThread.start();

        bots.add(rayoThread);

    }

    public static DiscordService getInstance() {

        return instance;
    }

}
