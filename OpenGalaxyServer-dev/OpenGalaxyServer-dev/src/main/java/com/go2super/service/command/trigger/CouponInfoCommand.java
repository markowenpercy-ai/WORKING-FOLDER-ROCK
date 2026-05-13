package com.go2super.service.command.trigger;

import java.util.Optional;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.AccountService;
import com.go2super.service.StoreEventService;
import com.go2super.service.command.Command;

public class CouponInfoCommand extends Command {
  public CouponInfoCommand() {
    super("coupon", "permission.coupon");
  }

  @Override
  public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
    if (parts.length != 1) {
      sendMessage("Command 'event' has invalid arguments! '/coupon'", user);
      return;
    }
    Optional<Account> optionalToAccount = AccountService.getInstance().getAccountCache().findById(user.getAccountId());
    if (optionalToAccount.isEmpty()) {
      return;
    }
    long storePoints = StoreEventService.getInstance().getStorePoints(optionalToAccount.get());
    sendMessage("You have " + storePoints + " points!", user);
  }
}
