package com.go2super.service;

import com.go2super.database.cache.FleetPresetCache;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.FleetPreset;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class FleetPresetService {

    private static FleetPresetService instance;
    private static final int MAX_PRESETS_PER_USER = 10;

    private FleetPresetService() {}

    public static FleetPresetService getInstance() {
        if (instance == null) {
            instance = new FleetPresetService();
        }
        return instance;
    }

    public FleetPreset savePreset(int userId, String name, List<Fleet> fleets) {
        FleetPresetCache cache = FleetPresetCache.getInstance();
        if (cache.countByUserId(userId) >= MAX_PRESETS_PER_USER) {
            throw new IllegalStateException("Maximum presets limit reached (10)");
        }

        Optional<FleetPreset> existing = cache.findByUserIdAndName(userId, name);
        if (existing.isPresent()) {
            throw new IllegalStateException("Preset with this name already exists");
        }

        List<FleetPreset.FleetPresetEntry> entries = new ArrayList<>();
        for (Fleet fleet : fleets) {
            FleetPreset.FleetPresetEntry entry = FleetPreset.FleetPresetEntry.builder()
                .name(fleet.getName())
                .he3(fleet.getHe3())
                .commanderId(fleet.getCommanderId())
                .bodyId(fleet.getBodyId())
                .rangeType(fleet.getRangeType())
                .preferenceType(fleet.getPreferenceType())
                .posX(fleet.getPosX())
                .posY(fleet.getPosY())
                .direction(fleet.getDirection())
                .cells(new ArrayList<>(fleet.getFleetBody().getCells()))
                .build();
            entries.add(entry);
        }

        FleetPreset preset = FleetPreset.builder()
            .userId(userId)
            .name(name)
            .entries(entries)
            .createdAt(new Date())
            .build();

        return cache.save(preset);
    }

    public Optional<FleetPreset> getPreset(int userId, String name) {
        return FleetPresetCache.getInstance().findByUserIdAndName(userId, name);
    }

    public List<FleetPreset> listPresets(int userId) {
        return FleetPresetCache.getInstance().findByUserId(userId);
    }

    public void deletePreset(int userId, String name) {
        FleetPresetCache.getInstance().deleteByUserIdAndName(userId, name);
    }

    public boolean presetExists(int userId, String name) {
        return FleetPresetCache.getInstance().findByUserIdAndName(userId, name).isPresent();
    }
}
