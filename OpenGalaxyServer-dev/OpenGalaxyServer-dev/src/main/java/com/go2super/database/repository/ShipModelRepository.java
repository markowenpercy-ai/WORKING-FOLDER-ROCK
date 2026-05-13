package com.go2super.database.repository;

import com.go2super.database.entity.ShipModel;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface ShipModelRepository extends MongoRepository<ShipModel, ObjectId>, Serializable {

    ShipModel findTopByOrderByIdDesc();

    List<ShipModel> findAll();

    List<ShipModel> findAllByGuidAndDeleted(int guid, boolean deleted);

    ShipModel findByShipModelId(int shipModelId);

    ShipModel findByGuid(int guid);

    List<ShipModel> findAllByNameAndGuid(String name, int guid);

    long count();

}