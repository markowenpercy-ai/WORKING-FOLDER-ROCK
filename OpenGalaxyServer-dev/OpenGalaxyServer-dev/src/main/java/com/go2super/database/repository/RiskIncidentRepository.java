package com.go2super.database.repository;

import com.go2super.database.entity.RiskIncident;
import com.go2super.database.repository.custom.RiskIncidentRepositoryCustom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface RiskIncidentRepository extends MongoRepository<RiskIncident, ObjectId>, RiskIncidentRepositoryCustom, Serializable {

    RiskIncident findTopByOrderByIdDesc();

    Optional<RiskIncident> findById(String id);

    List<RiskIncident> findAll();

    long count();

}