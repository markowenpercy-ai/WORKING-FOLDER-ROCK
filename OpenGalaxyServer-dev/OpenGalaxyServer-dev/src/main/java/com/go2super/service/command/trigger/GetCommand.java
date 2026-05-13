package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.service.LoginService;
import com.go2super.service.command.Command;

import java.util.*;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_ID;

// pretty sure all these commands are fucked, bricked my account
// TODO: who fucking cares, dont use it

public class GetCommand extends Command {

    public GetCommand() {

        super("get", "permission.get", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'get' need more arguments!", user);
            return;

        }

        int toGuid = -1;
        int propId = -1;
        int amount = -1;

        toGuid = user.getGuid();
        propId = Integer.parseInt(parts[1]);
        amount = Integer.parseInt(parts[2]);

        if (toGuid < 0 || propId < 0 || amount < 0 || amount > 9999) {

            sendMessage("Invalid arguments for 'get' command! (get <propId> <amount>)", user);
            return;

        }

        PropData propData = ResourceManager.getProps().getData(propId);

        if (propData == null && propId > MAX_COMMANDER_ID) {

            sendMessage("Invalid propId argument for 'get' command! (get <propId> <amount>)", user);
            return;

        }

        UserInventory userInventory = user.getInventory();
        userInventory.addProp(propId, amount, 0, false);

        user.save();

        int[] propIds = new int[10];
        int[] propNums = new int[10];

        propIds[0] = propId;
        propNums[0] = amount;

        sendMessage("Items sent!", user);
        sendMessage("New items received!", user);

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

        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(user);

        if (gameUserOptional.isPresent()) {
            gameUserOptional.get().getSmartServer().send(packet);
        }

    }

}
