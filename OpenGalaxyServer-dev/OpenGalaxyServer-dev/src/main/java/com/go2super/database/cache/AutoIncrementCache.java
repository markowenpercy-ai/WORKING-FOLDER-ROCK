package com.go2super.database.cache;

import com.go2super.database.entity.AutoIncrement;
import com.go2super.database.repository.AutoIncrementRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class AutoIncrementCache {

    private static final CopyOnWriteArrayList<AutoIncrement> cache = new CopyOnWriteArrayList<>();

    private AutoIncrementRepository repository;

    @Autowired
    public void CommanderCache(AutoIncrementRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());
    }

    public List<AutoIncrement> findAll() {

        return new GlueList<>(cache);
    }

    public void saveChanged() {
        var values = cache.stream().filter(autoIncrement -> autoIncrement.isToSave()).toList();
        values = repository.saveAll(values);
        for (AutoIncrement value : values) {
            value.setToSave(false);
            if (!cache.contains(value)) {
                cache.add(value);
            }
        }
    }

    public void save(AutoIncrement value) {

        value = repository.save(value);
        value.setToSave(false);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(AutoIncrement value) {

        remove(value);
    }

    public void remove(AutoIncrement value) {

        repository.delete(value);
        cache.remove(value);
    }

    public Optional<AutoIncrement> findById(String id) {

        return cache.stream().filter(autoIncrement -> autoIncrement.getId().toString().equals(id)).findFirst();
    }

    public AutoIncrement findByName(String name) {

        return cache.stream().filter(autoIncrement -> name.equals(autoIncrement.getName())).findFirst().orElse(null);
    }


    public long count() {

        return cache.size();
    }

}
