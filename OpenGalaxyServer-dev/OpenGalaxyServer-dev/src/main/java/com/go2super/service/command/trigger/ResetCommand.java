package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.packet.payment.ResponsePaymentSucceedPacket;
import com.go2super.service.DiscordService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class ResetCommand extends Command {

    public ResetCommand() {

        super("reset", "permission.purchasemp");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        user.resetNewDay();
        user.save();
        smartServer.sendMessage("Day Reset Success!");
    }
}
