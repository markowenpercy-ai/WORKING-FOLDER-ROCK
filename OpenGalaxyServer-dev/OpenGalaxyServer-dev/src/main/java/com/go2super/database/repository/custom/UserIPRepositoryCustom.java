package com.go2super.database.repository.custom;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.UserIP;
import com.go2super.server.GameServerReceiver;

import java.util.*;

public interface UserIPRepositoryCustom {

    void updateUserIP(Account account, User user, GameServerReceiver serverReceiver);

    boolean hasIPConflict(Account account, String ip);

    List<UserIP> getIPConflict(String ip);

}