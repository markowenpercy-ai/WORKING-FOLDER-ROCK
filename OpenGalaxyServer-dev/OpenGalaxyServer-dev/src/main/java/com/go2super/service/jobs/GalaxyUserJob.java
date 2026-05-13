package com.go2super.service.jobs;

import com.go2super.database.entity.User;
import com.go2super.obj.model.LoggedGameUser;

public interface GalaxyUserJob {

    String getName();

    boolean needUpdate(User user);

    boolean run(LoggedGameUser loggedGameUser, User updated);

}
