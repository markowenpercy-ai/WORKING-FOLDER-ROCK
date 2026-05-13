package com.go2super.database.cache;

import com.go2super.database.entity.Account;
import com.go2super.database.repository.AccountRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class AccountCache {

    private static final CopyOnWriteArrayList<Account> cache = new CopyOnWriteArrayList<>();

    private AccountRepository repository;

    @Autowired
    public void AccountCache(AccountRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());
    }

    public List<Account> findAll() {

        return new GlueList<>(cache);
    }

    public void save(Account value) {

        value = repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(Account value) {

        remove(value);
    }

    public void remove(Account value) {

        repository.delete(value);
        cache.remove(value);
    }

    public Optional<Account> findById(String id) {

        return cache.stream().filter(account -> id.equals(account.getId().toString())).findFirst();
    }

    public Optional<Account> findByEmail(String email) {

        return cache.stream().filter(account -> email.equals(account.getEmail())).findFirst();
    }

    public Optional<Account> findByUsername(String username) {

        return cache.stream().filter(account -> username.equals(account.getUsername())).findFirst();
    }

    public Optional<Account> findByDiscordId(String discordId) {

        return cache.stream()
                .filter(account -> account.getDiscordHook() != null && account.getDiscordHook().getDiscordId() != null && discordId.equals(account.getDiscordHook().getDiscordId()))
                .findFirst();
    }

    public Optional<Account> findByDiscordCode(String discordCode) {

        return cache.stream()
                .filter(account -> account.getDiscordHook() != null && account.getDiscordHook().getDiscordCode() != null && discordCode.equals(account.getDiscordHook().getDiscordCode()))
                .findFirst();
    }

    public long count() {

        return cache.size();
    }

}
