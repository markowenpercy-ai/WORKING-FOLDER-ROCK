package com.go2super.database.repository;

import com.go2super.database.entity.FleetPreset;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FleetPresetRepository extends MongoRepository<FleetPreset, ObjectId> {
    List<FleetPreset> findByUserId(int userId);
    Optional<FleetPreset> findByUserIdAndName(int userId, String name);
    void deleteByUserIdAndName(int userId, String name);
    long countByUserId(int userId);
}
