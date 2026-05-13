package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BionicChip;
import com.go2super.database.entity.sub.UserChips;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.service.DiscordService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.awt.*;
import java.util.*;
import java.text.SimpleDateFormat;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_ID;

// need to fuck this off or amy will have everyone endgame by day 2

public class GiveCommand extends Command {

    public GiveCommand() {

        super("give", "permission.give", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'give' need more arguments!", user);
            return;

        }

        int toGuid = -1;
        int propId = -1;
        int amount = -1;
        boolean lockFlag = false;

        if (parts.length == 3) {

            toGuid = user.getGuid();
            propId = Integer.parseInt(parts[1]);
            amount = Integer.parseInt(parts[2]);

        } else {

            toGuid = Integer.parseInt(parts[1]);
            propId = Integer.parseInt(parts[2]);
            amount = Integer.parseInt(parts[3]);

            if (parts.length > 4) {
                lockFlag = parts[4].equalsIgnoreCase("lock");
            }
        }

        if (toGuid < 0 || propId < 0 || amount < 0 || amount > 9999) {

            sendMessage("Invalid arguments for 'give' command! (give <guid> <propId> <amount>)", user);
            return;

        }

        PropData propData = ResourceManager.getProps().getData(propId);

        if (propData == null && propId > MAX_COMMANDER_ID) {

            sendMessage("Invalid propId argument for 'give' command! (give <guid> <propId> <amount>)", user);
            return;

        }

        User receiver = toGuid == user.getGuid() ? user : UserService.getInstance().getUserCache().findByGuid(toGuid);

        if (receiver == null) {

            sendMessage("Receiver with id " + toGuid + " does not exists!", user);
            return;

        }

        if (propData != null && "chip".equals(propData.getType())) {

            UserChips userChips = receiver.getChips();

            if ((userChips.getChips().size() + amount) > userChips.getSlots()) {
                sendMessage("Not enough chip slots! Max: " + userChips.getSlots(), user);
                return;
            }

            boolean bound = propData.getChipData().hasToBound();
            for (int i = 0; i < amount; i++) {
                userChips.getChips().add(BionicChip.builder()
                    .chipId(propId)
                    .chipExperience(0)
                    .bound(bound)
                    .build());
            }

            receiver.save();
            sendMessage("Huma Chip added! Please refresh.", receiver);
            sendMessage("Chip sent!", user);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String s = String.format("Give: %s sent chip propId=%s %dx to %s(%d) on %s", user.getUsername(), propId, amount, receiver.getUsername(), receiver.getUserId(), dateFormat.format(new Date()));
            if (DiscordService.getInstance().getRayoBot() != null) {
                DiscordService.getInstance().getRayoBot().sendAudit("System", s, Color.MAGENTA, AuditType.INCIDENT);
            }
            return;

        }

        UserInventory userInventory = receiver.getInventory();
        userInventory.addProp(propId, amount, 0, lockFlag);

        receiver.save();

        int[] propIds = new int[10];
        int[] propNums = new int[10];

        propIds[0] = propId;
        propNums[0] = amount;

        sendMessage("Items sent!", user);
        sendMessage("New items received!", receiver);

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(-1);
        packet.setNumber(0);
        packet.setLockFlag((byte) 0);

        packet.setAwardPropsId(new IntegerArray(propIds));
        packet.setAwardPropsNum(new IntegerArray(propNums));
        packet.setAwardPropsLen(1);

        packet.setAwardFlag((byte) 1);
        packet.setAwardLockFlag((byte) (lockFlag ? 1 : 0));

        packet.setAwardCoins(0);
        packet.setAwardActiveCredit(0);
        packet.setAwardMetal(0);
        packet.setAwardGas(0);
        packet.setAwardMoney(0);

        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(receiver);

        if (gameUserOptional.isPresent()) {
            gameUserOptional.get().getSmartServer().send(packet);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String propName = propData == null ? "Unknown" : propData.getName();
        String s = String.format("Give: %s sent %s with propId=%s %dx to %s(%d) on %s", user.getUsername(), propName, propId,amount, receiver.getUsername(), receiver.getUserId(), dateFormat.format(new Date()));
        if (DiscordService.getInstance().getRayoBot() != null) {
            DiscordService.getInstance().getRayoBot().sendAudit("System", s, Color.MAGENTA, AuditType.INCIDENT);
        }

    }

}
