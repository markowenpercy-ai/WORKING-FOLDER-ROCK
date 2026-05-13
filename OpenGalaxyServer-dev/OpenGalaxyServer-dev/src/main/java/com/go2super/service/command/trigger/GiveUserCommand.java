package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.game.Prop;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.packet.props.ResponseDeletePropsPacket;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropCommanderData;
import com.go2super.service.DiscordService;
import com.go2super.service.LoginService;
import com.go2super.service.ResourcesService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.awt.*;
import java.util.List;
import java.util.Optional;

// need to check these perms and make sure only ADMIN has them
// TODO - fucking do it

public class GiveUserCommand extends Command {

    public GiveUserCommand() {

        super("giveuser", "permission.giveuser");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (parts.length < 3) {
            sendMessage("Command 'giveuser' need more arguments!", user);
            return;
        }

        if (parts.length != 4) {
            sendMessage("Invalid arguments for 'giveuser' command! (giveuser <planetId> <propId> <amount>)", user);
            return;
        }

        int toGuid = Integer.parseInt(parts[1]);
        int propId = Integer.parseInt(parts[2]);
        int amount = Integer.parseInt(parts[3]);

        if (amount < 1) {
            sendMessage("Invalid amount!", user);
            return;
        }

        if (user.getGuid() == toGuid) {
            sendMessage("You can't send items to yourself!", user);
            return;
        }

        User receiver = UserService.getInstance().getUserCache().findByGuid(toGuid);

        if (receiver == null) {
            sendMessage("Receiver with id " + toGuid + " does not exists!", user);
            return;
        }
        UserInventory userInventory = user.getInventory();
        if(account.getUserRank() != UserRank.GM){

            if (!userInventory.hasProp(propId, amount, 0, false)) {
                sendMessage("You don't have this item!", user);
                return;
            }

            if (user.getResources().getMallPoints() < 5) {
                sendMessage("Failed to send item! You have not enough GPs", user);
                return;
            }
        }

        UserInventory receiverInventory = receiver.getInventory();

        boolean receiverAdded = receiverInventory.addProp(propId, amount, 0, false);
        if (!receiverAdded) {
            sendMessage("Receiver's inventory is full!", user);
            return;
        }

        if(account.getUserRank() != UserRank.GM){
            boolean senderRemovedProp = userInventory.removeProp(propId, amount, 0, false);
            if (!senderRemovedProp) {
                sendMessage("Failed to remove item from your inventory!", user);
                return;
            }
            user.getResources().setMallPoints(user.getResources().getMallPoints() - 5);
        }

        user.save();
        receiver.save();

        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(user);
        gameUserOptional.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(ResourcesService.getInstance().getPlayerResourcePacket(user)));

        int[] propIds = new int[10];
        int[] propNums = new int[10];

        propIds[0] = propId;
        propNums[0] = amount;

        String itemName = getItemName(propId);

        sendMessage("Sent item " + itemName + " x" + amount + " to id=" + receiver.getGuid() + "!", user);
        sendMessage("Received item " + itemName + " x" + amount + " from id=" + user.getGuid() + "!", receiver);

        String buffer = "GiveUser Information\n" +
                "Sender: " + user.getGuid() + " (" + user.getUsername() + ")\n" +
                "Receiver: " + receiver.getGuid() + " (" + receiver.getUsername() + ")\n" +
                "Item: " + itemName + " x" + amount + " (" + propId + ")\n";
        if (DiscordService.getInstance().getRayoBot() != null) {
            DiscordService.getInstance().getRayoBot().sendAudit("GiveUser Information", buffer, Color.yellow, AuditType.TRADE);
        }

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(-1);
        packet.setNumber(0);
        packet.setLockFlag((byte) 0);

        packet.setAwardPropsId(new IntegerArray(propIds));
        packet.setAwardPropsNum(new IntegerArray(propNums));
        packet.setAwardPropsLen(1);

        packet.setAwardFlag((byte) 1);
        packet.setAwardLockFlag((byte) 0);

        packet.setAwardCoins(0);
        packet.setAwardActiveCredit(0);
        packet.setAwardMetal(0);
        packet.setAwardGas(0);
        packet.setAwardMoney(0);

        Optional<LoggedGameUser> senderGameUserOptional = LoginService.getInstance().getGame(user);
        senderGameUserOptional.ifPresent(loggedGameUser -> {
            ResponseDeletePropsPacket response = ResponseDeletePropsPacket.builder()
                    .propsId(propId)
                    .lockFlag((byte) 0)
                    .build();
            for (int i = 0; i < amount; i++) {
                loggedGameUser.getSmartServer().send(response);
            }
        });

        Optional<LoggedGameUser> gameReceiverOptional = LoginService.getInstance().getGame(receiver);
        gameReceiverOptional.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(packet));
    }

    private String getItemName(int propId) {
        boolean isCommander = false;
        PropData propData = null;
        List<PropData> props = ResourceManager.getProps().getCommanders();

        for (PropData data : props) {
            if (data.hasCommanderData()) {
                if ((data.getId() + 8) >= propId && data.getId() <= propId) {
                    propData = data;
                    isCommander = true;
                    break;
                }
            }
        }

        if (propData == null) {
            for (PropData data : ResourceManager.getProps().getProps()) {
                if (data.getId() == propId) {
                    propData = data;
                    break;
                }
            }
        }

        if (propData != null) {
            if (isCommander) {
                PropCommanderData commanderData = propData.getCommanderData();
                return commanderData.getCommander().getName() + " " + ((propId - propData.getId()) + 1) + "*";
            }
            return propData.getName();
        }
        return "unknown";
    }
}
