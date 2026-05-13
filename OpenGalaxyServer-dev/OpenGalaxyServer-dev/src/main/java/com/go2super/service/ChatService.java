package com.go2super.service;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.logger.data.UserActionLog;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.chat.ChatMessagePacket;
import com.go2super.service.command.Command;
import com.go2super.service.command.trigger.*;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;

@Getter
@Service
public class ChatService {

    private static final String commandPrefix = "/";
    private static ChatService instance;

    private final List<Command> commands = new ArrayList<>();

    public ChatService() {

        instance = this;

        commands.add(new ResourcesCommand());
        commands.add(new MpCommand());
        // commands.value(new NextCommand());
        commands.add(new GetCommand());
        commands.add(new AddCommand());
        commands.add(new BroadcastCommand());
        commands.add(new RestartCommand());
        commands.add(new BlueprintsCommand());
        commands.add(new GiveCommand());
        commands.add(new CreateCommand());
        commands.add(new DeleteCommand());
        commands.add(new StatusCommand());
        commands.add(new InformationCommand());
        commands.add(new MuteCommand());
        commands.add(new TempBanCommand());
        commands.add(new BanCommand());
        commands.add(new WarnCommand());
        commands.add(new MaintenanceCommand());
        commands.add(new DiscordCommand());
        commands.add(new RemoveCommand());
        commands.add(new BlockCommand());
        commands.add(new BlackListCommand());
        commands.add(new BlockItemCommand());
        commands.add(new HasBlockCommand());
        commands.add(new NotDisturbCommand());
        commands.add(new SpyCommand());
        commands.add(new TurnPeaceCommand());
        commands.add(new PerformanceCommand());
        commands.add(new UnbanCommand());
        commands.add(new UserInformationCommand());
        commands.add(new SBanCommand());
        commands.add(new STempBanCommand());
        commands.add(new ChangeNicknameCommand());
        commands.add(new GiveMailCommand());
        commands.add(new ChangeRankCommand());
        commands.add(new ChangePasswordCommand());
        commands.add(new PurchaseMpCommand());
        commands.add(new GiftMpCommand());
        commands.add(new PurgeDesignsCommand());
        commands.add(new OpenBagCommand());
        commands.add(new CouponInfoCommand());
        commands.add(new ResetCommand());
        commands.add(new StopMatchCommand());
        commands.add(new FindMatchCommand());
        commands.add(new EventPurchaseCommand());
        commands.add(new EventSettingCommand());
        commands.add(new UnpauseMatchCommand());
        commands.add(new PauseMatchCommand());
        commands.add(new GiveUserCommand());
        commands.add(new FeatureCommand());
        commands.add(new MaxCommandersCommand());
        commands.add(new ListCommandersCommand());
        commands.add(new SetCommanderADESCommand());
        commands.add(new SetCommanderVarianceCommand());
        commands.add(new MaxScienceCommand());
        commands.add(new MaxBuildingsCommand());
        commands.add(new SaveFleetPresetCommand());
        commands.add(new LoadFleetPresetCommand());
        commands.add(new DismissAllFleetsCommand());
        commands.add(new ListFleetPresetsCommand());
        commands.add(new DeleteFleetPresetCommand());
    }

    public boolean checkCommand(User executor, SmartServer smartServer, String message) {

        for (Command command : commands) {

            String label = commandPrefix + command.getCommand();

            if (label.startsWith(message.split(" ")[0])) {

                Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findById(executor.getAccountId());

                if (optionalAccount.isEmpty()) {

                    sendMessage("Unlinked account! Please contact support (admin@supergo2.com)", executor);
                    return true;

                }

                Account account = optionalAccount.get();

                if (!account.getUserRank().hasAnyPermission(command.getPermission())) {

                    sendMessage("Insufficient permissions!", executor);
                    return true;

                }

                try {

                    command.execute(executor, account, smartServer, label, message.split(" "));

                    UserActionLog action = UserActionLog.builder()
                        .action("user-cmd-perform")
                        .message("[CMD] " + executor.getUsername() + " (Command: " + command.getCommand() + ", Permission: " + command.getPermission() + ")")
                        .build();

                    // EventLogger.sendUserAction(action, executor, (GameServerReceiver) smartServer);

                } catch (Exception e) {

                    sendMessage("Error during command execution!", executor);
                    e.printStackTrace();
                    return true;

                }

                return true;

            }

        }

        return false;

    }

    public void medusaMessage(String message) {

        broadcastMessage(message, 500, LoginService.getInstance().getGameUsers().stream().toArray(LoggedGameUser[]::new));
    }

    public void broadcastMessage(String message) {

        broadcastMessage(message, 6, LoginService.getInstance().getGameUsers().stream().toArray(LoggedGameUser[]::new));
    }

    public void broadcastMessage(String message, int channelType, LoggedGameUser... gameUsers) {

        ChatMessagePacket packet = new ChatMessagePacket();

        WideString buffer = WideString.of(message, 128);

        packet.setSeqId(-1);
        packet.setSrcUserId(0);
        packet.setObjUserId(0);
        packet.setGuid(-1);
        packet.setObjGuid(0);
        packet.setChannelType((short) channelType);
        packet.setSpecialType((short) 0);
        packet.setPropsId(-1);
        packet.setCorpAcronym(WideString.of("", 64));
        packet.setName(WideString.of("", 32));
        packet.setToName(WideString.of("", 32));
        packet.setBuffer(buffer);

        for (LoggedGameUser loggedGameUser : gameUsers) {
            loggedGameUser.getSmartServer().send(packet);
        }

    }

    public void sendMessage(String message, User... users) {

        ChatMessagePacket packet = new ChatMessagePacket();

        WideString buffer = WideString.of(message, 128);

        packet.setSeqId(-1);
        packet.setSrcUserId(0);
        packet.setObjUserId(0);
        packet.setGuid(-1);
        packet.setObjGuid(0);
        packet.setChannelType((short) 6);
        packet.setSpecialType((short) 0);
        packet.setPropsId(-1);
        packet.setAcronym(-1);
        packet.setCorpAcronym(WideString.of("", 64));
        packet.setName(WideString.of("", 32));
        packet.setToName(WideString.of("", 32));
        packet.setBuffer(buffer);

        List<LoggedGameUser> loggedGameUsers = new ArrayList<>();

        for (User user : users) {

            Optional<LoggedGameUser> optionalLoggedGameUser = user.getLoggedGameUser();

            if (optionalLoggedGameUser.isEmpty()) {
                continue;
            }

            loggedGameUsers.add(optionalLoggedGameUser.get());

        }

        for (LoggedGameUser loggedGameUser : loggedGameUsers) {
            loggedGameUser.getSmartServer().send(packet);
        }

    }

    public static ChatService getInstance() {

        return instance;
    }

}
