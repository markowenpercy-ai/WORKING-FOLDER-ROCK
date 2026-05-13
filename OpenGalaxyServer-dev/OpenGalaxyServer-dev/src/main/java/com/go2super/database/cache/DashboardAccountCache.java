package com.go2super.database.cache;

import com.go2super.database.entity.DashboardAccount;
import com.go2super.database.repository.DashboardAccountRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DashboardAccountCache {

    private static final CopyOnWriteArrayList<DashboardAccount> cache = new CopyOnWriteArrayList<>();

    private DashboardAccountRepository repository;

    @Autowired
    public void AccountCache(DashboardAccountRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());

    }

    public List<DashboardAccount> findAll() {

        return new GlueList<>(cache);
    }

    public void save(DashboardAccount value) {

        value = repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(DashboardAccount value) {

        remove(value);
    }

    public void remove(DashboardAccount value) {

        repository.delete(value);
        cache.remove(value);
    }

    public Optional<DashboardAccount> findById(String id) {

        return cache.stream().filter(account -> id.equals(account.getId().toString())).findFirst();
    }

    public Optional<DashboardAccount> findByEmail(String email) {

        return cache.stream().filter(account -> email.equals(account.getEmail())).findFirst();
    }

    public long count() {

        return cache.size();
    }

}
