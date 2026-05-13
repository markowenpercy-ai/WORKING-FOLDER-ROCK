package com.go2super.database.repository.custom;

import com.go2super.database.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.stream.*;

public class UserRepositoryImpl implements UserRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    public List<User> getByShipUpgrading() {

        Criteria criteria = Criteria.where("game_ships.factory").ne(null);

        Query query = new Query();
        query.addCriteria(criteria);

        List<User> users = mongoTemplate.find(query, User.class);
        users = users.stream().filter(user -> !user.getShips().getFactory().isEmpty()).collect(Collectors.toList());

        return users;

    }

    public List<User> getByTechUpgrading() {

        Criteria criteria = Criteria.where("game_user_techs.upgrade").ne(null);

        Query query = new Query();
        query.addCriteria(criteria);

        List<User> users = mongoTemplate.find(query, User.class);

        return users;

    }

}
