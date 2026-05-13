package com.go2super.database.cache;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.repository.ShipModelRepository;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class ShipModelCache {

    private static final CopyOnWriteArrayList<ShipModel> cache = new CopyOnWriteArrayList<>();

    private ShipModelRepository repository;

    @Autowired
    public void ShipModelCache(ShipModelRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());
    }

    public List<ShipModel> findAll() {

        return new GlueList<>(cache);
    }

    public void save(ShipModel value) {

        value = repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(ShipModel value) {

        remove(value);
    }

    public void remove(ShipModel value) {

        repository.delete(value);
        cache.remove(value);
    }

    public List<ShipModel> findAllByGuidAndDeleted(int guid, boolean deleted) {

        return cache.stream().filter(shipModel -> shipModel.getGuid() == guid && shipModel.isDeleted() == deleted).collect(Collectors.toList());
    }

    public ShipModel findByShipModelId(int shipModelId) {

        return cache.stream().filter(shipModel -> shipModel.getShipModelId() == shipModelId).findFirst().orElse(null);
    }

    public ShipModel findByGuid(int guid) {

        return cache.stream().filter(shipModel -> shipModel.getGuid() == guid).findFirst().orElse(null);
    }

    public List<ShipModel> findAllByNameAndGuid(String name, int guid) {

        return cache.stream().filter(shipModel -> shipModel.getName().equals(name) && shipModel.getGuid() == guid).collect(Collectors.toList());
    }

    public long count() {

        return cache.size();
    }

}
