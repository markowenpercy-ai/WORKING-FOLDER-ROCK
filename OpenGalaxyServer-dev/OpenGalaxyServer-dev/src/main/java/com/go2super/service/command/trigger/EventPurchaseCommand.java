package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.StoreEvent;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.storeevent.BuyEventEntry;
import com.go2super.obj.entry.SmartServer;
import com.go2super.resources.json.storeevent.CommanderEventJson;
import com.go2super.resources.json.storeevent.PackData;
import com.go2super.service.CLIEventService;
import com.go2super.service.command.Command;

public class EventPurchaseCommand extends Command {


    public EventPurchaseCommand() {
        super("event", "permission.event");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (!CLIEventService.getInstance().isEventEnabled()) {
            sendMessage("Currently there is no Event.", user);
            return;
        }

        if (parts.length == 2 && parts[1].equalsIgnoreCase("info") || parts.length == 1) {
            CommanderEventJson data = CLIEventService.getInstance().getPackList();
            sendMessage("Commander Event Info:", user);
            for (PackData pack : data.getPack()) {
                String s = String.format("packName: %s description: %s x %s (%smp) limit: %d", pack.getShortName(), pack.getCount(), pack.getName(), pack.getCost(), pack.getLimit());
                sendMessage(s, user);
            }
            String buyInfo = "/event buy <packName> <amount>";
            String buyInfoExample = String.format("/event buy %s %s", data.getPack().get(0).getShortName(), 1);
            sendMessage(buyInfo, user);
            sendMessage("Example: " + buyInfoExample, user);
            return;
        }

        if (parts.length != 4) {
            sendMessage("Command 'event' has invalid arguments! '/event buy <packName> <amount>'", user);
            return;
        }
        int quantity = Integer.parseInt(parts[3]);
        String shortName = parts[2];
        if (quantity <= 0 || quantity > 9999) {
            sendMessage("Invalid quantity for 'event' command! (expected 0-9999)", user);
            return;
        }

        CLIEventService instance = CLIEventService.getInstance();
        PackData pack = instance.getPackList().findByShortName(shortName);
        if (pack == null) {
            sendMessage("Invalid pack name for 'event' command! (got " + shortName + ")", user);
            return;
        }

        int cost = pack.getCost();
        String accountId = user.getAccountId();
        StoreEvent accountEvent = instance.getStoreEvent(accountId);
        long newPoints = accountEvent.getStorePoints() - (long) cost * quantity;
        if (newPoints < 0) {
            sendMessage("Not enough points for 'event' command! (got " + accountEvent.getStorePoints() + ")", user);
            return;
        }

        BuyEventEntry buyEvent = (BuyEventEntry) instance.getEventEntry(
                accountEvent,
                instance.getPackList().getEventId(),
                new BuyEventEntry(instance.getPackList().getEventId())
        );
        if (pack.getLimit() != -1) {
            if (pack.getLimit() < buyEvent.getLimit(pack.getId()) + quantity) {
                sendMessage(String.format("Limit of %d reached", pack.getLimit()), user);
                return;
            }
        }


        boolean response = instance.purchasePack(user, pack.getShortName(), quantity);
        if (response) {
            sendMessage("Successfully purchased " + quantity + "x " + pack.getShortName() + " for " + cost * quantity + " points! " + "You now have " + newPoints + " points!", user);
        } else {
            sendMessage("Failed to purchase " + quantity + " " + pack.getId() + " for " + cost * quantity + " points!", user);
        }

    }
}
