package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.payment.ResponsePaymentSucceedPacket;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.util.*;

public class MpCommand extends Command {

    public MpCommand() {

        super("mp", "permission.mp", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {

            sendMessage("Command 'mp' has invalid arguments! (mp <guid>)", user);
            return;

        }

        int guid = Integer.parseInt(parts[1]);
        User receiver = UserService.getInstance().getUserCache().findByGuid(guid);

        if (receiver == null) {

            sendMessage("Receiver with id " + guid + " does not exists!", user);
            return;

        }

        Long mp = Math.abs(receiver.getResources().getMallPoints() - 900_000_000);

        if (mp == 0) {
            sendMessage("Can't send mp.");
            return;
        }

        receiver.getResources().setMallPoints(900_000_000);
        receiver.save();

        Optional<LoggedGameUser> optional = receiver.getLoggedGameUser();

        if (optional.isPresent()) {

            ResponsePaymentSucceedPacket responsePaymentSucceedPacket = new ResponsePaymentSucceedPacket();
            responsePaymentSucceedPacket.setCredit(mp.intValue());
            optional.get().getSmartServer().send(responsePaymentSucceedPacket);

            sendMessage("You have received " + mp + " MP!", receiver);

        }

        sendMessage("MP sent!", user);

    }

}
