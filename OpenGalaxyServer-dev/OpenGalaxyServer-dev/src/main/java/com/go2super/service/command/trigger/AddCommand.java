package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.payment.ResponsePaymentSucceedPacket;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.service.ResourcesService;
import com.go2super.service.command.Command;

import java.util.*;

public class AddCommand extends Command {

    public AddCommand() {

        super("add", "permission.add", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        Long gold = Math.abs(user.getResources().getGold() - 2_000_000_000L);
        Long he3 = Math.abs(user.getResources().getHe3() - 2_000_000_000L);
        Long metal = Math.abs(user.getResources().getMetal() - 2_000_000_000L);
        Long vouchers = Math.abs(user.getResources().getVouchers() - 900_000_000);
        Long mp = Math.abs(user.getResources().getMallPoints() - 900_000_000);

        if (gold == 0 && he3 == 0 && metal == 0 && vouchers == 0 && mp == 0) {
            sendMessage("Resources sent!", user);
            return;
        }

        user.getResources().setGold(2_000_000_000L);
        user.getResources().setHe3(2_000_000_000L);
        user.getResources().setMetal(2_000_000_000L);
        user.getResources().setVouchers(900_000_000);
        user.getResources().setMallPoints(900_000_000);

        user.save();

        Optional<LoggedGameUser> optional = user.getLoggedGameUser();

        if (optional.isPresent()) {

            if (!(gold == 0 && he3 == 0 && metal == 0 && vouchers == 0)) {

                ResponseUsePropsPacket response = ResourcesService.getInstance().genericUseProps(-1, 0, metal.intValue(), he3.intValue(), gold.intValue(), vouchers.intValue(), 0, 0, 0, 0, 1, 0);
                optional.get().getSmartServer().send(response);

                sendMessage("You have received resources!", user);

            }

            if (mp != 0) {

                ResponsePaymentSucceedPacket responsePaymentSucceedPacket = new ResponsePaymentSucceedPacket();
                responsePaymentSucceedPacket.setCredit(mp.intValue());
                optional.get().getSmartServer().send(responsePaymentSucceedPacket);

                sendMessage("You have received " + mp + " MP!", user);

            }

            sendMessage("(IMPORTANT) Please restart your game!", user);

        }

    }

}