package com.go2super.logger;

import com.go2super.database.entity.User;
import com.go2super.logger.data.Action;
import com.go2super.logger.data.UserActionLog;
import com.go2super.server.GameServerReceiver;
import com.go2super.service.PacketService;
import lombok.SneakyThrows;

public class EventLogger {

    public static void sendUserAction(UserActionLog action, User user, GameServerReceiver serverReceiver) {

        action.setAccountEmail(serverReceiver.getAccountEmail());
        action.setAccountId(serverReceiver.getAccountId());
        action.setAccountName(serverReceiver.getAccountName());
        action.setOptionalDiscordId(serverReceiver.getDiscordId());

        action.setUserId(user.getUserId());
        action.setUsername(user.getUsername());
        action.setGuid(user.getGuid());

        send(action, serverReceiver);

    }

    public static void send(Action action, GameServerReceiver serverReceiver) {

        action.setScope(PacketService.getInstance().getGelfScope());

        action.setHostname(serverReceiver.getHostname());
        action.setIp(serverReceiver.getIp());

        if (action.getCategory() == null) {
            action.setCategory("common");
        }

        send(action);

    }

    @SneakyThrows
    public static void send(Action action) {

        /*ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.valueToTree(action);

        String host = PacketService.getInstance().getGelfEndpoint();

        Unirest.post(host)
                .header("Content-Type", "application/json")
                .body(jsonNode.toString())
                .asStringAsync();*/

    }

}
