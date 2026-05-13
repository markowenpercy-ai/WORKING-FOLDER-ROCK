package com.go2super.database.cache;

import com.go2super.database.entity.FleetPreset;
import com.go2super.database.repository.FleetPresetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FleetPresetCache {

    private static FleetPresetCache instance;
    private final FleetPresetRepository repository;

    @Autowired
    public FleetPresetCache(FleetPresetRepository repository) {
        this.repository = repository;
        instance = this;
    }

    public static FleetPresetCache getInstance() {
        return instance;
    }

    public List<FleetPreset> findByUserId(int userId) {
        return repository.findByUserId(userId);
    }

    public Optional<FleetPreset> findByUserIdAndName(int userId, String name) {
        return repository.findByUserIdAndName(userId, name);
    }

    public FleetPreset save(FleetPreset preset) {
        return repository.save(preset);
    }

    public void delete(FleetPreset preset) {
        repository.delete(preset);
    }

    public void deleteByUserIdAndName(int userId, String name) {
        repository.deleteByUserIdAndName(userId, name);
    }

    public long countByUserId(int userId) {
        return repository.countByUserId(userId);
    }
}
