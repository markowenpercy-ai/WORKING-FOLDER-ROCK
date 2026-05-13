package com.go2super.database.repository.custom;

import com.go2super.database.entity.Account;

import java.util.*;

public interface AccountRepositoryCustom {

    Optional<Account> findByDiscordCode(String discordCode);

    Optional<Account> findByDiscordId(String discordId);

}