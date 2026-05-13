package com.go2super.database.cache;

import com.go2super.database.entity.Sanction;
import com.go2super.database.repository.SanctionRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class SanctionCache {

    private SanctionRepository repository;

    @Autowired
    public void SanctionCache(SanctionRepository repository) {
        this.repository = repository;
    }

    public List<Sanction> findAll() {
        return new GlueList<>(repository.findAll());
    }

    public void save(Sanction value) {
        repository.save(value);
    }

    public void delete(Sanction value) {

        remove(value);
    }

    public void remove(Sanction value) {
        repository.delete(value);
    }

    public long count() {
        return repository.count();
    }

}
