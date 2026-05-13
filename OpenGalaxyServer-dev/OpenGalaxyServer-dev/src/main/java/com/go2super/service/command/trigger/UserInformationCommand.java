package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.SameIPIncident;
import com.go2super.database.entity.sub.UserSameIPIncidentInfo;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.AccountService;
import com.go2super.service.RiskService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;
import lombok.SneakyThrows;

import java.util.*;
import java.util.stream.*;

public class UserInformationCommand extends Command {

    public UserInformationCommand() {

        super("userinfo", "permission.userinfo");
    }

    @SneakyThrows
    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {

            sendMessage("Command 'userinfo' need more arguments! (example: /userinfo 1)", user);
            return;

        }

        int toGuid = Integer.parseInt(parts[1]);

        if (toGuid < 0) {

            sendMessage("Invalid command arguments!", user);
            return;

        }

        User toUser = UserService.getInstance().getUserCache().findByGuid(toGuid);

        if (toUser == null) {

            sendMessage("User not exists!", user);
            return;

        }

        Optional<Account> optionalToAccount = AccountService.getInstance().getAccountCache().findById(toUser.getAccountId());
        if (optionalToAccount.isEmpty()) {

            sendMessage("User not have an account!", user);
            return;

        }

        Account toAccount = optionalToAccount.get();
        /*if(toAccount.getUserRank().hasPermission(getPermission())) {

            sendMessage("Can't perform this command because user has info permissions!", user);
            return;

        }*/

        boolean online = toUser.getLoggedGameUser().isPresent();

        sendMessage("---[ " + toUser.getUsername() + " ]---", user);

        sendMessage("GUID: " + toUser.getGuid(), user);
        sendMessage("Account: " + toAccount.getUsername(), user);
        sendMessage("Email: " + toAccount.getEmail(), user);
        sendMessage("Banned: " + (toAccount.getBanUntil() != null ? "Yes" : "No"), user);
        sendMessage("Online: " + (online ? "Yes" : "No"), user);
        sendMessage("Detections:", user);


        List<SameIPIncident> detections = RiskService.getInstance().getRiskIncidentRepository().getSameIPDetections(toAccount);
        detections = detections.stream().filter(detection -> !detection.isIgnore()).collect(Collectors.toList());

        Set<String> values = new HashSet<>();

        if (detections.isEmpty()) {

            sendMessage("~ EMPTY ~", user);
            sendMessage("--- ------- ---", user);
            return;

        }

        for (SameIPIncident sameIPIncident : detections) {
            for (UserSameIPIncidentInfo incidentInfo : sameIPIncident.getUsers()) {
                values.add(incidentInfo.getUsername() + " (ID: " + incidentInfo.getGuid() + ")");
            }
        }

        for (String value : values) {
            sendMessage(value, user);
        }

        sendMessage("--- ------- ---", user);

    }

}
