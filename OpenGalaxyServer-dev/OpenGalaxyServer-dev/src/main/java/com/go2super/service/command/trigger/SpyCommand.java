package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Bloc;
import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.listener.ChatListener;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.CorpService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;
import org.bson.types.ObjectId;

import java.util.*;

public class SpyCommand extends Command {

    private static final String pattern = "MM-dd-yyyy HH:mm:ss";

    public SpyCommand() {

        super("spy", "permission.spy");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {

            sendMessage("Command 'spy' need more arguments! (usage: /spy <bloc|corp|stop> <guid>)", user);
            return;

        }

        ObjectId urBloc = null;
        int urCorp = -1;

        Corp userCorp = user.getCorp();
        if (userCorp != null) {
            urCorp = userCorp.getCorpId();
            if (userCorp.getBlocId() != null) {
                urBloc = userCorp.getBlocId();
                if(urBloc == null){
                    return;
                }
            }
        }

        String type = parts[1];
        int toGuid = Integer.parseInt(parts[2]);

        if (!type.equals("bloc") && !type.equals("corp") && !type.equals("stop")) {

            sendMessage("Invalid command arguments! (type must be 'bloc', 'corp', 'stop)", user);
            return;

        }

        if (type.equals("stop")) {

            ChatListener.corpSpy.remove(user.getGuid());
            ChatListener.blockSpy.remove(user.getGuid());

            sendMessage("Spy configurations cleared!", user);
            return;

        }

        User toUser = UserService.getInstance().getUserCache().findByGuid(toGuid);

        if (toUser == null) {

            sendMessage("The selected user does not exists!", user);
            return;

        }

        Corp toCorp = toUser.getCorp();

        if (toCorp.getCorpId() == urCorp) {

            sendMessage("You can't spy your own corp!", user);
            return;

        }

        if (toCorp == null) {

            sendMessage("The selected user is not in a corp!", user);
            return;

        }

        if (type.equals("corp")) {

            ChatListener.corpSpy.put(user.getGuid(), toCorp.getCorpId());
            sendMessage("Now you are spying the corp '" + toCorp.getName() + "'!", user);
            return;

        }

        if (toCorp.getBlocId() == null) {
            return;
        }

        Optional<Bloc> optionalBloc = CorpService.getInstance().getBlocRepository().findById(toCorp.getBlocId());
        if (optionalBloc.isEmpty()) {

            sendMessage("The selected corp does not have a bloc!", user);
            return;

        }

        Bloc bloc = optionalBloc.get();

        if (userCorp != null && userCorp.getBlocId() != null && userCorp.getBlocId().equals(bloc.getId().toString())) {

            sendMessage("You can't spy your own bloc!", user);
            return;

        }

        ChatListener.blockSpy.put(user.getGuid(), bloc.getId());
        sendMessage("Now you are spying the bloc '" + bloc.getName() + "'!", user);

    }

}
