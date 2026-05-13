package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.DiscordHook;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.AccountService;
import com.go2super.service.command.Command;
import com.go2super.socket.util.DateUtil;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

// kinda wanna delete it? kinda wanna use it to track who the fuck is linking to the discord server? but also its a pain in the ass to maintain and i dont want to deal with it

public class DiscordCommand extends Command {

    public DiscordCommand() {

        super("discord", "permission.discord", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        DiscordHook discordHook = account.getDiscordHook();
        if (discordHook == null) {
            account.setDiscordHook(DiscordHook.builder().build());
            discordHook = account.getDiscordHook();
        }

        if (discordHook.getDiscordId() != null && !discordHook.getDiscordId().isEmpty()) {

            sendMessage("You already have a discord linked account! If you want to remove it please use '/remove discord' command.", user);
            return;

        }

        if (discordHook.getDiscordCode() != null && discordHook.getDiscordCodeExpiration() != null) {

            Date generationDate = discordHook.getDiscordCodeExpiration();
            String discordCode;

            if (generationDate.before(DateUtil.now())) {

                discordCode = RandomStringUtils.randomAlphanumeric(4).toUpperCase();

                while (AccountService.getInstance().getAccountCache().findByDiscordCode(discordCode).isPresent()) {
                    discordCode = RandomStringUtils.randomAlphanumeric(4).toUpperCase();
                }

                sendMessage("Your discord code has been expired! The system is generating a new one...", user);
                sendMessage("Your new discord code is: " + discordCode, user);

            } else {

                discordCode = discordHook.getDiscordCode();
                sendMessage("Your current discord code is: " + discordCode, user);

            }

            discordHook.setDiscordCode(discordCode);
            discordHook.setDiscordCodeExpiration(DateUtil.now(3600));

            AccountService.getInstance().getAccountCache().save(account);

            sendMessage("Please follow the next steps:", user);
            sendMessage("1. Open the discord server", user);
            sendMessage("2. Go to the #bots channel.", user);
            sendMessage("3. Type: /link " + discordCode, user);
            return;

        }

        String discordCode = RandomStringUtils.randomAlphanumeric(4).toUpperCase();

        while (AccountService.getInstance().getAccountCache().findByDiscordCode(discordCode).isPresent()) {
            discordCode = RandomStringUtils.randomAlphanumeric(4).toUpperCase();
        }

        discordHook.setDiscordCode(discordCode);
        discordHook.setDiscordCodeExpiration(DateUtil.now(3600));

        AccountService.getInstance().getAccountCache().save(account);

        sendMessage("Your discord code is: " + discordCode, user);
        sendMessage("Please follow the next steps:", user);
        sendMessage("1. Open the discord server", user);
        sendMessage("2. Go to the #bots channel.", user);
        sendMessage("3. Type: /link " + discordCode, user);

    }

}
