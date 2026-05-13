package com.go2super.database.cache;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.repository.FleetRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class FleetCache {

    private static final CopyOnWriteArrayList<Fleet> cache = new CopyOnWriteArrayList<>();

    private FleetRepository repository;

    @Autowired
    public void FleetCache(FleetRepository fleetRepository) {
        this.repository = fleetRepository;
        List<Fleet> pirates = repository.findAll().stream().filter(x -> x.getBattleFleet() != null && x.getBattleFleet().isPirate() && x.getCurrentMatch().isEnded()).toList();
        for (Fleet pirate : pirates) {
            delete(pirate);
        }
        List<Fleet> sortedFleets = repository.findAll().stream()
            .sorted(Comparator.comparingInt(Fleet::getShipTeamId))
            .collect(Collectors.toList());
        cache.addAll(sortedFleets);
    }

    public List<Fleet> findAll() {

        return new GlueList<>(cache);
    }

    public void save(Fleet value) {

        value = repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(Fleet fleet) {

        remove(fleet);
    }

    public void remove(Fleet fleet) {

        repository.delete(fleet);
        cache.remove(fleet);
    }

    public List<Fleet> getInWarFleets() {

        return cache.stream().filter(fleet -> fleet.getFleetInitiator() != null).collect(Collectors.toList());
    }

    public List<Fleet> getInTransmissionFleets() {

        return cache.stream().filter(fleet -> fleet.getFleetTransmission() != null).collect(Collectors.toList());
    }

    public List<Fleet> getRadarFleets(int guid, int toGalaxyId) {

        return cache.stream().filter(fleet -> fleet.getGuid() == guid || (fleet.getFleetTransmission() != null && fleet.getFleetTransmission().getGalaxyId() == toGalaxyId)).collect(Collectors.toList());
    }

    public List<Fleet> getInTransmissionFleets(int guid) {

        return cache.stream().filter(fleet -> fleet.getGuid() == guid && fleet.getFleetTransmission() != null).collect(Collectors.toList());
    }

    public Fleet findByCommanderId(int commanderId) {

        return cache.stream().filter(fleet -> fleet.getCommanderId() == commanderId).findFirst().orElse(null);
    }

    public Fleet findByShipTeamId(int shipTeamId) {

        return cache.stream().filter(fleet -> fleet.getShipTeamId() == shipTeamId).findFirst().orElse(null);
    }

    public List<Fleet> findByShipTeamId(Collection<Integer> shipTeamIds) {

        return cache.stream().filter(fleet -> shipTeamIds.contains(fleet.getShipTeamId())).collect(Collectors.toList());
    }

    public List<Fleet> findAllByGalaxyId(int galaxyId) {

        return cache.stream().filter(fleet -> fleet.getGalaxyId() == galaxyId).collect(Collectors.toList());
    }

    public List<Fleet> findAllByGalaxyIdAndMatch(int galaxyId, boolean match) {

        return cache.stream().filter(fleet -> fleet.getGalaxyId() == galaxyId && fleet.isMatch() == match).collect(Collectors.toList());
    }

    public List<Fleet> findAllByGuid(int guid) {

        return cache.stream().filter(fleet -> fleet.getGuid() == guid).collect(Collectors.toList());
    }

    public long count() {

        return cache.size();
    }

}
