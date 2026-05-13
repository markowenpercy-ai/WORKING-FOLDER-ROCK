package com.go2super.database.repository;

import com.go2super.database.entity.Fleet;
import com.go2super.database.repository.custom.FleetRepositoryCustom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface FleetRepository extends MongoRepository<Fleet, ObjectId>, FleetRepositoryCustom, Serializable {

    Fleet findTopByOrderByIdDesc();

    Fleet findByCommanderId(int commanderId);

    Fleet findByShipTeamId(int shipTeamId);

    List<Fleet> findAll();

    List<Fleet> findAllByGalaxyId(int galaxyId);

    List<Fleet> findAllByGalaxyIdAndMatch(int galaxyId, boolean match);

    List<Fleet> findAllByGuid(int guid);

    long count();

}