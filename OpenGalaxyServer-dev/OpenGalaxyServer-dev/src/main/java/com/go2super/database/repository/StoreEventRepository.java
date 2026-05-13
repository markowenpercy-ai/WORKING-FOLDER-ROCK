package com.go2super.database.repository;

import com.go2super.database.entity.StoreEvent;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface StoreEventRepository extends MongoRepository<StoreEvent, ObjectId>, Serializable {
    List<StoreEvent> findAll();
    Optional<StoreEvent> findById(ObjectId id);
}
