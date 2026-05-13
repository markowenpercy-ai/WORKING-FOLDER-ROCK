package com.go2super.database.cache;

import com.go2super.database.entity.Commander;
import com.go2super.database.repository.CommanderRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class CommanderCache {

    private static final CopyOnWriteArrayList<Commander> cache = new CopyOnWriteArrayList<>();

    private CommanderRepository repository;

    @Autowired
    public void CommanderCache(CommanderRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());
    }

    public List<Commander> findAll() {

        return new GlueList<>(cache);
    }

    public void save(Commander value) {

        value = repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(Commander value) {

        remove(value);
    }

    public void remove(Commander value) {

        repository.delete(value);
        cache.remove(value);
    }

    public Optional<Commander> findById(String id) {

        return cache.stream().filter(commander -> commander.getId().toString().equals(id)).findFirst();
    }

    public Commander findBySkillAndUserId(int skill, long userId) {

        return cache.stream().filter(commander -> commander.getSkill() == skill && commander.getUserId() == userId).findFirst().orElse(null);
    }

    public Commander findByCommanderIdAndUserId(int commanderId, long userId) {

        return cache.stream().filter(commander -> commander.getCommanderId() == commanderId && commander.getUserId() == userId).findFirst().orElse(null);
    }

    public Commander findByCommanderId(int commanderId) {

        return cache.stream().filter(commander -> commander.getCommanderId() == commanderId).findFirst().orElse(null);
    }

    public Commander findByShipTeamId(int shipTeamId) {

        return cache.stream().filter(commander -> commander.getShipTeamId() == shipTeamId).findFirst().orElse(null);
    }

    public List<Commander> findByUserId(long userId) {

        return cache.stream().filter(commander -> commander.getUserId() == userId).collect(Collectors.toList());
    }

    public long count() {

        return cache.size();
    }

}
