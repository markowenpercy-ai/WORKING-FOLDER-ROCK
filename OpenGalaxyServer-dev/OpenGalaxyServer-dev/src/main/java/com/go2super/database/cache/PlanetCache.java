package com.go2super.database.cache;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.HumaroidPlanet;
import com.go2super.database.entity.sub.ResourcePlanet;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.repository.PlanetRepository;
import com.go2super.obj.utility.GalaxyRegion;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class PlanetCache {

    private static final CopyOnWriteArrayList<Planet> cache = new CopyOnWriteArrayList<>();

    private PlanetRepository repository;

    @Autowired
    public void PlanetCache(PlanetRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());

    }

    public List<Planet> findAll() {

        return new GlueList<>(cache);
    }

    public void saveAll(Collection<Planet> values) {

        repository.saveAll(values);
        cache.addAll(values);
    }

    public void save(Planet value) {

        repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(Planet value) {

        remove(value);
    }

    public void remove(Planet value) {

        repository.delete(value);
        cache.remove(value);
    }

    public Planet findByPosition(GalaxyTile position) {

        return cache.stream().filter(planet -> planet.getPosition().equals(position)).findFirst().orElse(null);
    }

    public UserPlanet findUserPlanet(User user) {

        return findUserPlanet(user.getUserId());
    }

    public UserPlanet findUserPlanet(long userId) {

        return cache.stream()
                .filter(UserPlanet.class::isInstance)
                .filter(planet -> planet.getUserId() == userId)
                .map(UserPlanet.class::cast)
                .findFirst().orElse(null);
    }

    public Planet findByUserId(long userId) {

        return cache.stream().filter(planet -> planet.getUserId() == userId).findFirst().orElse(null);
    }

    public List<Planet> findById(Collection<String> ids) {

        return cache.stream().filter(planet -> ids.contains(planet.getId().toString())).collect(Collectors.toList());
    }

    public List<GalaxyTile> findTakenPositions() {

        return cache.stream().map(planet -> planet.getPosition()).collect(Collectors.toList());
    }

    public List<Planet> findPlanets(GalaxyRegion region) {

        return cache.stream().filter(planet -> region.contains(planet.getPosition())).collect(Collectors.toList());
    }

    public List<HumaroidPlanet> findHumaroidPlanets() {

        return cache.stream()
                .filter(HumaroidPlanet.class::isInstance)
                .map(HumaroidPlanet.class::cast).collect(Collectors.toList());
    }

    public List<ResourcePlanet> findResourcePlanets() {

        return cache.stream()
                .filter(ResourcePlanet.class::isInstance)
                .map(ResourcePlanet.class::cast).collect(Collectors.toList());
    }

    public List<UserPlanet> findUserPlanets(GalaxyRegion region) {

        return cache.stream()
                .filter(UserPlanet.class::isInstance)
                .filter(planet -> region.contains(planet.getPosition()))
                .map(UserPlanet.class::cast).collect(Collectors.toList());
    }

    public List<ResourcePlanet> findResourcePlanets(int corpId) {

        return cache.stream()
                .filter(ResourcePlanet.class::isInstance)
                .map(ResourcePlanet.class::cast)
                .filter(resourcePlanet -> resourcePlanet.getCurrentCorp() == corpId).collect(Collectors.toList());
    }

    public Planet findTopByOrderByIdDesc() {

        return cache.get(cache.size() - 1);
    }

    public long count() {

        return cache.size();
    }

}
