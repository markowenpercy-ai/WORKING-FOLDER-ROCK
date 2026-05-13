package com.go2super.database.cache;

import com.go2super.database.entity.StoreEvent;
import com.go2super.database.repository.StoreEventRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class StoreEventCache {
    private static final CopyOnWriteArrayList<StoreEvent> cache = new CopyOnWriteArrayList<>();

    private StoreEventRepository repository;

    @Autowired
    public void StoreEventCache(StoreEventRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());

    }

    public List<StoreEvent> findAll() {
        return new GlueList<>(cache);
    }

    public void save(StoreEvent value) {
        value = repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(StoreEvent value) {
        remove(value);
    }

    public void remove(StoreEvent value) {
        repository.delete(value);
        cache.remove(value);
    }

    public Optional<StoreEvent> findById(String id) {
        return cache.stream().filter(event -> id.equals(event.getAccountId())).findFirst();
    }

    public long count() {
        return cache.size();
    }
}
