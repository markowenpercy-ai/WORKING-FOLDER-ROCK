package com.go2super.database.repository.custom;

import com.go2super.database.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

public class AccountRepositoryImpl implements AccountRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Optional<Account> findByDiscordId(String discordId) {

        Criteria criteria = Criteria.where("discord_hook.discord_id").is(discordId);
        Query query = new Query(criteria);
        return Optional.ofNullable(mongoTemplate.findOne(query, Account.class));

    }

    @Override
    public Optional<Account> findByDiscordCode(String discordCode) {

        Criteria criteria = Criteria.where("discord_hook.discord_code").is(discordCode);
        Query query = new Query(criteria);
        return Optional.ofNullable(mongoTemplate.findOne(query, Account.class));

    }

}
