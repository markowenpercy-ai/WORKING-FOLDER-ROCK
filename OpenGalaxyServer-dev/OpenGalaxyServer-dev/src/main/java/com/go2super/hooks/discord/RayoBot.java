package com.go2super.hooks.discord;

import com.go2super.database.entity.*;
import com.go2super.database.entity.sub.*;
import com.go2super.hooks.discord.claim.RayoPackage;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.Packet;
import com.go2super.packet.custom.CustomWarnPacket;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.server.GameServer;
import com.go2super.service.*;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.TimeUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class RayoBot extends DiscordBot {

    private static final Set<RayoPackage> rayoPackages = new HashSet<>();

    static {

        rayoPackages.add(RayoPackage.builder()
                .packageName("x20 Vouchers")
                .packageId("vouchers")
                .propId(935)
                .amount(2)
                .build());

        rayoPackages.add(RayoPackage.builder()
                .packageName("x1 Primary He3 Pack")
                .packageId("he3")
                .propId(915)
                .amount(1)
                .build());

        rayoPackages.add(RayoPackage.builder()
                .packageName("x1 Primary Metal Pack")
                .packageId("metal")
                .propId(912)
                .amount(1)
                .build());

        rayoPackages.add(RayoPackage.builder()
                .packageName("x30 Raw Gemstones")
                .packageId("gemstones")
                .propId(1119)
                .amount(30)
                .build());

    }

    @Getter
    public JDA jda;
    @Getter
    public Guild guild;
    private String currentStatus = "OpenGalaxy";
    private String pendingStatus = null;

    @Override
    @SneakyThrows
    public void start(String discordToken) {

        this.jda = JDABuilder.createLight(discordToken, Collections.emptyList())
                .addEventListeners(new RayoBot())
                .setActivity(Activity.playing(currentStatus))
                .build().awaitReady();
        
        if (pendingStatus != null) {
            getJda().getPresence().setActivity(Activity.playing(pendingStatus));
            pendingStatus = null;
        }

        this.guild = jda.getGuildById(DiscordService.getInstance().getGuild());

        if (guild == null) {

            BotLogger.error("Guild not found!");
            return;

        }

        guild.upsertCommand(Commands.slash("online", "Administrative command")).queue();
        guild.upsertCommand(Commands.slash("performance", "Administrative command")).queue();
        guild.upsertCommand(Commands.slash("setstatus", "Set the bot's playing status")
                .addOption(OptionType.STRING, "status", "The new status text.", true)).queue();
        guild.upsertCommand(Commands.slash("ignore", "Administrative command")
                .addOption(OptionType.STRING, "id", "Reserved", true)).queue();
        guild.upsertCommand(Commands.slash("user-ignore", "Administrative command")
                .addOption(OptionType.STRING, "id", "Reserved", true)
                .addOption(OptionType.INTEGER, "guid", "Reserved", true)
                .addOption(OptionType.STRING, "info", "Reserved", true)).queue();

        guild.upsertCommand(Commands.slash("remove-acronym", "Remove the alliance acronym!")).queue();
        guild.upsertCommand(Commands.slash("acronym", "Set your alliance acronym (3 Characters)!")
                .addOption(OptionType.STRING, "acronym", "The acronym name (3 Characters).", true)).queue();

        guild.upsertCommand(Commands.slash("bloc", "Basic resume of your current bloc of alliances!")).queue();

        guild.upsertCommand(Commands.slash("create-bloc", "Create a new bloc of alliances!")
                .addOption(OptionType.STRING, "name", "The bloc name.", true)).queue();
        guild.upsertCommand(Commands.slash("join-bloc", "Join your alliance to a bloc of alliances!")
                .addOption(OptionType.STRING, "code", "The bloc code to join (request it to the bloc organizer).", true)).queue();
        guild.upsertCommand(Commands.slash("kick-bloc", "Kick an alliance from your bloc of alliances!")
                .addOption(OptionType.INTEGER, "id", "Alliance ID to kick (check ID's with /bloc).", true)).queue();

        guild.upsertCommand(Commands.slash("create-bloc-code", "Create a new temporal code to invite alliances!")).queue();
        guild.upsertCommand(Commands.slash("leave-bloc", "Remove your alliance from your bloc of alliances!")).queue();
        guild.upsertCommand(Commands.slash("dissolve-bloc", "Dissolve your alliance bloc of alliances!")).queue();

        guild.upsertCommand(Commands.slash("claim", "Claim the rewards of the day!")).queue();
        guild.upsertCommand(Commands.slash("link", "Link your game Account with discord.")
                .addOption(OptionType.STRING, "code", "The generated code.", true)).queue();

    }

    @Override
    public void stop() {

    }

    public void sendAudit(String title, String message, Color color, AuditType auditType) {

        if (guild == null) {
            return;
        }

        TextChannel textChannel = null;

        switch (auditType) {
            case GENERAL:
                textChannel = guild.getTextChannelById(DiscordService.getInstance().getAuditChannel());
                break;
            case CHAT:
                textChannel = guild.getTextChannelById(DiscordService.getInstance().getChatAuditChannel());
                break;
            case TRADE:
                textChannel = guild.getTextChannelById(DiscordService.getInstance().getTradeAuditChannel());
                break;
            case MERGE:
                textChannel = guild.getTextChannelById(DiscordService.getInstance().getMergeAuditChannel());
                break;
            case DELETE:
                textChannel = guild.getTextChannelById(DiscordService.getInstance().getDeleteAuditChannel());
                break;
            case LOGIN:
                textChannel = guild.getTextChannelById(DiscordService.getInstance().getLoginAuditChannel());
                break;
            case COMMANDER:
                textChannel = guild.getTextChannelById(DiscordService.getInstance().getCommanderAuditChannel());
                break;
            case INCIDENT:
                textChannel = guild.getTextChannelById(DiscordService.getInstance().getIncidentAuditChannel());
        }

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(title, null);
        eb.setColor(color);

        if (message.length() > 4000) {
            eb.setDescription(message.substring(0, 4000));
            BotLogger.error("Audit message too long: " + message);
        } else {
            eb.setDescription(message);
        }
        eb.setDescription(message);
        try {
            textChannel.sendMessageEmbeds(eb.build()).queue();
        } catch (Exception ex) {
            //ignore the exception
        }
    }

    public void sendAudit(String message) {

        if (guild == null) {
            return;
        }

        TextChannel textChannel = getAuditChannel();
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle("Rayo Audit", null);
        eb.setColor(Color.cyan);

        eb.setDescription(message);
        try {
            textChannel.sendMessageEmbeds(eb.build())
                    .queue();
        } catch (Exception ex) {
            //ignore exception
        }


    }

    public TextChannel getAuditChannel() {

        return guild.getTextChannelById(DiscordService.getInstance().getAuditChannel());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (event.getMessageChannel() == null) {
            return;
        }

        MessageChannel messageChannel = event.getMessageChannel();
        if (!messageChannel.getId().equals(DiscordService.getInstance().getCmdChannel()) && !messageChannel.getId().equals(DiscordService.getInstance().getAuditChannel())) {

            event.reply("You can only use this command in the bots channel! Try again here: <#" + DiscordService.getInstance().getCmdChannel() + ">")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("setstatus")) {

            try {
                String ownerId = DiscordService.getInstance().getOwner();
                
                if (ownerId == null || !event.getUser().getId().equals(ownerId)) {

                    event.reply("You don't have enough permissions to do this action!")
                            .setEphemeral(true)
                            .queue();
                    return;

                }

                String status = Objects.requireNonNull(event.getOption("status")).getAsString();
                if (status.length() > 100) {
                    status = status.substring(0, 100);
                }

                if (getJda() != null) {
                    getJda().getPresence().setActivity(Activity.of(ActivityType.CUSTOM_STATUS, status));
                    currentStatus = status;
                } else {
                    pendingStatus = status;
                }

                event.reply("Bot status changed to: `" + status + "`")
                        .setEphemeral(true)
                        .queue();
            } catch (Exception e) {
                BotLogger.error("Error in setstatus command: " + e.getMessage());
                e.printStackTrace();
                event.reply("Error: " + e.getMessage())
                        .setEphemeral(true)
                        .queue();
            }
            return;

        }

        if (event.getName().equals("ignore")) {

            if (!event.getUser().getId().equals(DiscordService.getInstance().getOwner())) {

                event.reply("You don't have enough permissions to do this action!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            String id = Objects.requireNonNull(event.getOption("id")).getAsString();
            Optional<RiskIncident> optionalRiskIncident = RiskService.getInstance().getRiskIncidentRepository().findById(id);

            if (optionalRiskIncident.isEmpty()) {

                event.reply("There is no risk incident with that ID!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            RiskIncident riskIncident = optionalRiskIncident.get();
            riskIncident.setIgnore(!riskIncident.isIgnore());

            RiskService.getInstance().getRiskIncidentRepository().save(riskIncident);

            event.reply("New risk status: " + (riskIncident.isIgnore() ? "`ignore`" : "`not ignore`"))
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("user-ignore")) {

            if (!event.getUser().getId().equals(DiscordService.getInstance().getOwner())) {

                event.reply("You don't have enough permissions to do this action!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            String id = event.getOption("id").getAsString();
            int guid = event.getOption("guid").getAsInt();
            String info = event.getOption("info").getAsString();

            Optional<RiskIncident> optionalRiskIncident = RiskService.getInstance().getRiskIncidentRepository().findById(id);

            if (optionalRiskIncident.isEmpty()) {

                event.reply("There is no risk incident with that ID!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            RiskIncident riskIncident = optionalRiskIncident.get();

            if (!(riskIncident instanceof SameIPIncident sameIPIncident)) {

                event.reply("This risk incident is not SameIPIncident!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Optional<UserSameIPIncidentInfo> optionalSameIPIncidentInfo = sameIPIncident.getUsers().stream().filter(user -> user.getGuid() == guid).findFirst();

            if (optionalSameIPIncidentInfo.isPresent()) {

                UserSameIPIncidentInfo user = optionalSameIPIncidentInfo.get();

                if (user.getIgnore() == null) {
                    user.setIgnore(true);
                } else {
                    user.setIgnore(!user.getIgnore());
                }

                user.setDescription(info);

                RiskService.getInstance().getRiskIncidentRepository().save(sameIPIncident);

                event.reply("New user risk status: " + (user.getIgnore() ? "`ignore`" : "`not ignore`") + ", info: `" + user.getDescription() + "`.")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            event.reply("User not found in the risk incident.")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("online")) {

            if (!event.getUser().getId().equals(DiscordService.getInstance().getOwner())) {

                event.reply("You don't have enough permissions to do this action!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle("Online Players", null);
            eb.setColor(Color.green);

            List<String> players = new ArrayList<>();

            for (LoggedGameUser loggedGameUser : LoginService.getInstance().getGameUsers()) {
                User user = loggedGameUser.getUpdatedUser();
                players.add(user.getUsername() + " (" + user.getGuid() + ")");
            }

            if (players.isEmpty()) {
                eb.setDescription("No players online!");
            } else {
                eb.setDescription(StringUtils.join(players, ", "));
            }

            event.replyEmbeds(eb.build())
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("performance")) {

            if (!event.getUser().getId().equals(DiscordService.getInstance().getOwner())) {

                event.reply("You don't have enough permissions to do this action!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle("Server Performance", null);
            eb.setColor(Color.green);

            MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            long usedMemory = heapMemoryUsage.getUsed();
            long maxMemory = heapMemoryUsage.getMax();

            long uptime = System.currentTimeMillis() - PacketService.STARTUP.getTime();

            String info =

                    "**Uptime** = `" + TimeUtil.humanReadable((int) (uptime / 1000)) + "`\n" +
                            "**Online** = `" + LoginService.getInstance().getGameUsers().size() + "`\n" +
                            "**Memory** = `" + humanReadableByteCountSI(usedMemory) + "/" + humanReadableByteCountSI(maxMemory) + "`\n" +
                            // "**Main Average** = `" + (JobService.getInstance().getMainTotalTime() / JobService.getInstance().getMainTotalRuns()) + " ms`\n" +
                            // "**Last Average** = `" + JobService.getLastAverage() + " ms`\n" +
                            // "**Total Runs** = `" + JobService.getInstance().getMainTotalRuns() + "`\n" +
                            // "**Max Peak** = \n" +
                            // "**^~** (" + JobService.getInstance().getHighestTask().getTaskName() + ", " + JobService.getInstance().getHighestTask().getTime() + " ms)\n" +
                            // "**Min Peak** = \n" +
                            // "**^~** (" + JobService.getInstance().getLowestTask().getTaskName() + ", " + JobService.getInstance().getLowestTask().getTime() + " ms)\n" +
                            "**Battles** = `" + BattleService.getInstance().getBattles().size() + "`\n" +
                            // "**Tasks List** = `" + JobService.getInstance().getTasks().size() + "`\n" +
                            "**Threads** = `" + Thread.activeCount() + "`\n" +
                            "**Peak** = `" + maxPeak() + "`\n" +
                            "**Receivers** = `" + GameServer.clients.size() + "`\n";

            eb.setDescription(info);

            event.replyEmbeds(eb.build())
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("remove-acronym")) {

            Pair<User, Corp> pair = getMainUserAndCorp(event);
            if (pair == null) {
                return;
            }

            User user = pair.getLeft();
            Corp corp = pair.getRight();

            CorpMember corpLead = corp.getMembers().getLeader();
            if (corpLead == null || corpLead.getGuid() != user.getGuid()) {

                event.reply("You are not the colonel of your alliance!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            corp.setAcronym(null);
            corp.save();

            event.reply("Acronym removed!")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("acronym")) {

            String acronym = event.getOption("acronym").getAsString();

            if (acronym.length() != 3) {

                event.reply("The acronym must have a length of 3 characters!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            acronym = acronym.toUpperCase();

            if (!StringUtils.isAlphanumeric(acronym)) {

                event.reply("The acronym is invalid!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Corp otherCorp = CorpService.getInstance().getCorpCache().findByAcronym(acronym);
            if (otherCorp != null) {

                event.reply("The acronym is already in use!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Pair<User, Corp> pair = getMainUserAndCorp(event);

            if (pair == null) {
                return;
            }

            User user = pair.getLeft();
            Corp corp = pair.getRight();

            CorpMember corpLead = corp.getMembers().getLeader();

            if (corpLead == null || corpLead.getGuid() != user.getGuid()) {

                event.reply("You are not the colonel of your alliance!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            corp.setAcronym(acronym);
            corp.save();

            event.reply("Acronym `" + acronym + "` set!")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("bloc")) {

            Pair<User, Corp> pair = getMainUserAndCorp(event);
            if (pair == null) {
                return;
            }

            Corp corp = pair.getRight();

            if (corp.getBlocId() == null) {

                event.reply("Your alliance is not in a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Bloc bloc = CorpService.getInstance().getBlocRepository().findById(corp.getBlocId()).orElse(null);
            if (bloc == null) {

                event.reply("Your alliance is not in a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            List<Corp> corps = CorpService.getInstance().getCorpCache().findByBlocId(bloc.getId());
            List<String> cnames = corps.stream().map(cacheCorp -> cacheCorp.getName() + " (ID: " + cacheCorp.getCorpId() + ")").collect(Collectors.toList());

            String names = null;
            String organizer = corps.stream().filter(c -> c.getCorpId() == bloc.getOrganizer()).findFirst().orElse(null).getName();

            if (corps.size() > 1) {
                names = StringUtils.join(cnames, ", ");
            } else {
                names = corps.get(0).getName() + " (ID: " + corps.get(0).getCorpId() + ")";
            }

            EmbedBuilder eb = new EmbedBuilder();

            eb.setTitle("Bloc: " + bloc.getName(), null);
            eb.setColor(Color.cyan);

            eb.setDescription(
                    "**Organizer:** " + organizer + " (ID: " + bloc.getOrganizer() + ")\n" +
                            "**Members:** " + names);

            event.replyEmbeds(eb.build())
                    .queue();
            return;

        }

        if (event.getName().equals("create-bloc")) {

            String name = event.getOption("name").getAsString();

            if (name.length() < 3 || name.length() > 32) {

                event.reply("The bloc name must be in the range of (3-32) chars!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Optional<Bloc> exists = CorpService.getInstance().getBlocRepository().findByName(name);
            if (exists.isPresent()) {

                event.reply("A bloc with this name already exists!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Pair<User, Corp> pair = getMainUserAndCorp(event);
            if (pair == null) {
                return;
            }

            User user = pair.getLeft();
            Corp corp = pair.getRight();

            CorpMember corpLead = corp.getMembers().getLeader();
            if (corpLead == null || corpLead.getGuid() != user.getGuid()) {

                event.reply("You are not the colonel of your alliance!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            if (corp.getBlocId() != null) {

                event.reply("Your alliance is already in a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Bloc bloc = Bloc.builder()
                    .id(ObjectId.get())
                    .name(name)
                    .organizer(corp.getCorpId())
                    .build();
            CorpService.getInstance().getBlocRepository().save(bloc);

            corp.setBlocId(bloc.getId());
            corp.save();

            event.reply("New bloc `" + bloc.getName() + "` created, now you can use /bloc to see the information of the bloc!")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("join-bloc")) {

            Pair<User, Corp> pair = getMainUserAndCorp(event);
            if (pair == null) {
                return;
            }

            User user = pair.getLeft();
            Corp corp = pair.getRight();

            CorpMember corpLead = corp.getMembers().getLeader();
            if (corpLead == null || corpLead.getGuid() != user.getGuid()) {

                event.reply("You are not the colonel of your alliance!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            if (corp.getBlocId() != null) {

                event.reply("Your alliance is already in a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Bloc bloc = CorpService.getInstance().getBlocRepository().findByCode(event.getOption("code").getAsString()).orElse(null);
            if (bloc == null || bloc.getUntil() == null || bloc.getUntil().before(new Date())) {

                event.reply("The code does not exists or has expired!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            List<Corp> corps = CorpService.getInstance().getCorpCache().findByBlocId(bloc.getId());
            if (corps.size() >= 5) {

                event.reply("The bloc is full!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            corp.setBlocId(bloc.getId());
            corp.save();

            event.reply("Your corp `" + corp.getName() + "` joined successfully to the bloc `" + bloc.getName() + "`!")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("leave-bloc")) {

            Pair<User, Corp> pair = getMainUserAndCorp(event);
            if (pair == null) {
                return;
            }

            User user = pair.getLeft();
            Corp corp = pair.getRight();

            CorpMember corpLead = corp.getMembers().getLeader();
            if (corpLead == null || corpLead.getGuid() != user.getGuid()) {

                event.reply("You are not the colonel of your alliance!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            if (corp.getBlocId() == null) {

                event.reply("Your alliance is not in a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Bloc bloc = CorpService.getInstance().getBlocRepository().findById(corp.getBlocId()).orElse(null);
            if (bloc == null) {

                event.reply("Your alliance is not in a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            if (bloc.getOrganizer() == corp.getCorpId()) {

                event.reply("You can't leave the bloc because you are the organizer! (If you want to dissolve try with `/dissolve-bloc`)")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            LoginService.getInstance()
                    .getGameUsers()
                    .stream()
                    .filter(u -> u.getUpdatedUser().getConsortiaId() == corp.getCorpId())
                    .forEach(u -> u.getSmartServer().sendMessage("Your corp " + corp.getName() + " left successfully the bloc " + bloc.getName() + "!"));

            corp.setBlocId(null);
            corp.save();

            event.reply("Your corp " + corp.getName() + " left successfully the bloc " + bloc.getName() + "!")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("kick-bloc")) {

            Pair<User, Corp> pair = getMainUserAndCorp(event);
            if (pair == null) {
                return;
            }

            User user = pair.getLeft();
            Corp corp = pair.getRight();

            CorpMember corpLead = corp.getMembers().getLeader();
            if (corpLead == null || corpLead.getGuid() != user.getGuid()) {

                event.reply("You are not the colonel of your alliance!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            if (corp.getBlocId() == null) {

                event.reply("Your alliance is not part of a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Bloc bloc = CorpService.getInstance().getBlocRepository().findById(corp.getBlocId()).orElse(null);
            if (bloc == null || bloc.getOrganizer() != corp.getCorpId()) {

                event.reply("The bloc does not exists or you aren't the organizer!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Corp otherCorp = CorpService.getInstance().getCorpCache().findByCorpId(event.getOption("id").getAsInt());
            if (otherCorp == null) {

                event.reply("The corp to kick does not exists!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            if (otherCorp.getBlocId() == null || !otherCorp.getBlocId().equals(bloc.getId().toString())) {

                event.reply("The corp to kick (" + otherCorp.getName() + ") is not part of your bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            otherCorp.setBlocId(null);
            otherCorp.save();

            event.reply("The corp " + otherCorp.getName() + " has been kicked from the bloc " + bloc.getName() + "!")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("dissolve-bloc")) {

            Pair<User, Corp> pair = getMainUserAndCorp(event);
            if (pair == null) {
                return;
            }

            User user = pair.getLeft();
            Corp corp = pair.getRight();

            CorpMember corpLead = corp.getMembers().getLeader();
            if (corpLead == null || corpLead.getGuid() != user.getGuid()) {

                event.reply("You are not the colonel of your alliance!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            if (corp.getBlocId() == null) {

                event.reply("Your alliance is not part of a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Bloc bloc = CorpService.getInstance().getBlocRepository().findById(corp.getBlocId()).orElse(null);
            if (bloc == null || bloc.getOrganizer() != corp.getCorpId()) {

                event.reply("The bloc does not exists or you aren't the organizer!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            List<Corp> corps = CorpService.getInstance().getCorpCache().findByBlocId(bloc.getId());

            for (Corp otherCorp : corps) {

                otherCorp.setBlocId(null);
                otherCorp.save();

                LoginService.getInstance()
                        .getGameUsers()
                        .stream()
                        .filter(u -> u.getUpdatedUser().getConsortiaId() == otherCorp.getCorpId())
                        .forEach(u -> u.getSmartServer().sendMessage("Your bloc " + bloc.getName() + " has been disbanded!"));

            }

            CorpService.getInstance().getBlocRepository().delete(bloc);
            event.reply("The bloc " + bloc.getName() + " has been disbanded!")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("create-bloc-code")) {

            Pair<User, Corp> pair = getMainUserAndCorp(event);
            if (pair == null) {
                return;
            }

            User user = pair.getLeft();
            Corp corp = pair.getRight();

            CorpMember corpLead = corp.getMembers().getLeader();
            if (corpLead == null || corpLead.getGuid() != user.getGuid()) {

                event.reply("You are not the colonel of your alliance!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            if (corp.getBlocId() == null) {

                event.reply("Your alliance is not part of a bloc!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Bloc bloc = CorpService.getInstance().getBlocRepository().findById(corp.getBlocId()).orElse(null);
            if (bloc == null || bloc.getOrganizer() != corp.getCorpId()) {

                event.reply("The bloc does not exists or you aren't the organizer!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            String code = RandomStringUtils.randomAlphanumeric(5).toUpperCase();

            bloc.setCode(code);
            bloc.setUntil(DateUtil.now(3600));

            CorpService.getInstance().getBlocRepository().save(bloc);
            event.reply("The new code is: " + code + ", share it with the colonel of the alliance you want that join!")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("link")) {

            Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findByDiscordId(event.getMember().getId());

            if (optionalAccount.isPresent()) {

                event.reply("Your game account is already linked. If you want to change the discord account, please go to the game and use */remove discord* in the __game chat__ to unlink this user.")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            String code = event.getOption("code").getAsString();
            Optional<Account> accountOptional = AccountService.getInstance().getAccountCache().findByDiscordCode(code);

            if (accountOptional.isPresent()) {

                Account account = accountOptional.get();
                DiscordHook discordHook = account.getDiscordHook();

                if (discordHook.getDiscordCodeExpiration() == null || discordHook.getDiscordCodeExpiration().before(new Date())) {

                    event.reply("This code has expired! Please use */discord* in the __game chat__ again to generate a valid link code.")
                            .setEphemeral(true)
                            .queue();
                    return;

                }

                if (!discordHook.isLinkedDiscordBefore()) {

                    discordHook.setLinkedDiscordBefore(true);

                    List<User> users = UserService.getInstance().getUserCache().findByAccountId(account.getId().toString());
                    for (User user : users) {

                        Optional<LoggedGameUser> loggedGameUser = user.getLoggedGameUser();
                        if (!loggedGameUser.isPresent()) {
                            continue;
                        }

                        CustomWarnPacket response = new CustomWarnPacket();

                        response.setSeqId(0);
                        response.setSrcUserId(0);
                        response.setObjUserId(0);
                        response.setGuid(0);
                        response.setObjGuid(0);
                        response.setChannelType((short) 0);
                        response.setSpecialType((short) 0);
                        response.setPropsId(0);
                        response.setName(WideString.of(user.getUsername(), 32));
                        response.setToName(WideString.of(user.getUsername(), 32));
                        response.setBuffer(WideString.of(
                                "<strong><font size='15' face=\"Verdana\" color='#0373FC'>Discord Linked!</font></strong><br/>" +
                                        "Now you can claim a new daily prize using /claim in the #bots channel!", 1024));

                        loggedGameUser.get().getSmartServer().send(response);

                    }

                }


                discordHook.setDiscordId(event.getMember().getId());

                discordHook.setDiscordCode(null);
                discordHook.setDiscordCodeExpiration(null);

                AccountService.getInstance().getAccountCache().save(account);

                event.reply("Your discord account has been linked successfully!")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            event.reply("The code you entered is invalid! Please use */discord* in the __game chat__ to generate a valid link code.")
                    .setEphemeral(true)
                    .queue();
            return;

        }

        if (event.getName().equals("claim")) {

            Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findByDiscordId(event.getMember().getId());

            if (!optionalAccount.isPresent()) {

                event.reply("Your account is not linked to discord yet. Please use */discord* in the __game chat__ to start the linking process.")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Account account = optionalAccount.get();
            List<User> users = UserService.getInstance().getUserCache().findByAccountId(account.getId().toString());

            if (users.isEmpty()) {

                event.reply("Your account does not have any planet. Please create one planet first.")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            DiscordHook discordHook = account.getDiscordHook();

            if (discordHook.getLastClaim() == null || !DateUtil.currentDay(discordHook.getLastClaim())) {

                SelectMenu.Builder selectMenuBuilder = SelectMenu.create("claim-menu");

                for (RayoPackage rayoPackage : rayoPackages) {
                    selectMenuBuilder.addOption(rayoPackage.getPackageName(), rayoPackage.getPackageId());
                }

                event.reply("The intergalactic council has some supplies for us, let's take one! <:2574:1000245697087557773>\n" +
                                "Commander **make sure** you have space available in your mail inbox before you claim your prize.")
                        .addActionRow(selectMenuBuilder.build())
                        .setEphemeral(true)
                        .queue();
                return;

            }

            event.reply("You can only claim the rewards of the day once per day. Please try again tomorrow.")
                    .setEphemeral(true)
                    .queue();

        }

    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {

        if (event.getSelectMenu().getId().equals("claim-menu")) {

            Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findByDiscordId(event.getMember().getId());

            if (!optionalAccount.isPresent()) {

                event.reply("Your account is not linked to discord yet. Please use */discord* in the __game chat__ to start the linking process.")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            Account account = optionalAccount.get();
            List<User> users = UserService.getInstance().getUserCache().findByAccountId(account.getId().toString());

            if (users.isEmpty()) {

                event.reply("Your account does not have any planet. Please create one planet first.")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            List<SelectOption> selectedOptions = event.getSelectedOptions();

            if (selectedOptions.isEmpty()) {

                event.reply("Please select a valid option.")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            SelectOption selected = selectedOptions.get(0);

            Optional<RayoPackage> optionalRayoPackage = rayoPackages.stream().filter(rp -> rp.getPackageId().equals(selected.getValue())).findFirst();

            if (optionalRayoPackage.isEmpty()) {

                event.reply("Please select a valid option.")
                        .setEphemeral(true)
                        .queue();
                return;

            }

            RayoPackage rayoPackage = optionalRayoPackage.get();
            User main = users.get(0); // todo get main planet

            DiscordHook discordHook = account.getDiscordHook();

            if (discordHook.getLastClaim() == null || !DateUtil.currentDay(discordHook.getLastClaim())) {

                account.getDiscordHook().setLastClaim(new Date());
                AccountService.getInstance().getAccountCache().save(account);

                UserEmailStorage userEmailStorage = main.getUserEmailStorage();

                Email email = Email.builder()
                        .autoId(userEmailStorage.nextAutoId())
                        .type(2)
                        .name("System")
                        .subject("Rayo Daily Reward")
                        .emailContent(
                                "Dear Commander, \n" +
                                        "Here I got you some goods, I hope you find them very useful!")
                        .readFlag(0)
                        .date(DateUtil.now())
                        .goods(new ArrayList<>())
                        .guid(-1)
                        .build();

                email.addGood(EmailGood.builder()
                        .goodId(rayoPackage.getPropId())
                        .lockNum(rayoPackage.getAmount())
                        .build());

                List<Packet> toSend = new ArrayList<>();

                userEmailStorage.addEmail(email);

                main.update();
                main.save();

                Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(main);

                if (gameUserOptional.isPresent()) {

                    LoggedGameUser loggedGameUser = gameUserOptional.get();
                    ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                            .errorCode(0)
                            .build();

                    loggedGameUser.getSmartServer().send(response);
                    loggedGameUser.getSmartServer().send(toSend);

                }

                event.getMessageChannel().sendMessage(new MessageBuilder()
                                .append(":gift: <@" + event.getMember().getId() + "> claimed the rewards of the day and picked **" + rayoPackage.getPackageName() + "**!")
                                .build())
                        .queue();

                event.reply("Your goods has been sent to your mail! <:Newinfo:998106779026210926>")
                        .setEphemeral(true)
                        .queue();
                return;

            }

        }


    }

    private Pair<User, Corp> getMainUserAndCorp(SlashCommandInteractionEvent event) {

        User user = getMainUser(event);
        if (user == null) {
            return null;
        }

        Corp corp = user.getCorp();
        if (corp == null) {

            event.reply("Your planet '" + user.getUsername() + "' does not have a corp. Please create one first.")
                    .setEphemeral(true)
                    .queue();
            return null;

        }

        return Pair.of(user, corp);

    }

    private User getMainUser(SlashCommandInteractionEvent event) {

        Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findByDiscordId(event.getMember().getId());

        if (!optionalAccount.isPresent()) {

            event.reply("Your game account is not linked yet.")
                    .setEphemeral(true)
                    .queue();
            return null;

        }

        List<User> users = UserService.getInstance().getUserCache().findByAccountId(optionalAccount.get().getId().toString());
        if (users.isEmpty()) {

            event.reply("You don't have planets yet.")
                    .setEphemeral(true)
                    .queue();
            return null;

        }

        return users.get(0);

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
