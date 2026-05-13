package com.go2super.obj.model;

import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.UserService;
import lombok.Builder;
import lombok.Data;

import java.net.InetAddress;

@Data
@Builder
public class LoggedGameUser {

    private long userId;
    private int guid;
    private int consortiaId;

    private String matchViewing;
    private long viewing = -1;

    private SmartServer smartServer;
    private InetAddress address;

    private LoggedSessionUser loggedSessionUser;

    public User getUpdatedUser() {

        return UserService.getInstance().getUserCache().findByGuid(loggedSessionUser.getUser().getGuid());
    }

}
