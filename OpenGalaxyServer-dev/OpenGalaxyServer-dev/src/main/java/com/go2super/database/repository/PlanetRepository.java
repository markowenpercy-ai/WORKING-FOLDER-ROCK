package com.go2super.database.repository;

import com.go2super.database.entity.Planet;
import com.go2super.database.repository.custom.PlanetRepositoryCustom;
import com.go2super.obj.utility.GalaxyTile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.io.Serializable;
import java.util.*;

public interface PlanetRepository extends MongoRepository<Planet, ObjectId>, PlanetRepositoryCustom, Serializable {

    Planet findTopByOrderByIdDesc();

    Planet findByPosition(GalaxyTile position);

    Planet findByUserId(long userId);

    @Query(value = "{ 'id' : {'$in' : ?0 } }")
    List<Planet> findById(Collection<String> ids);

    List<Planet> findAll();

    long count();

}