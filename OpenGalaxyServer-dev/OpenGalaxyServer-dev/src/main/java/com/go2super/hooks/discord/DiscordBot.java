package com.go2super.hooks.discord;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class DiscordBot extends ListenerAdapter {

    public abstract void start(String discordToken);

    public abstract void stop();

}
