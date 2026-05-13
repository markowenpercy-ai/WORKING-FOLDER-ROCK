package com.go2super.database.repository.custom;

import com.go2super.database.entity.User;

import java.util.*;

public interface UserRepositoryCustom {

    List<User> getByTechUpgrading();

    List<User> getByShipUpgrading();

}
