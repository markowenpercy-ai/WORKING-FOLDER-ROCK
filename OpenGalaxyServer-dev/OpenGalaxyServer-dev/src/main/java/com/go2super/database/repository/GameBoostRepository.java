package com.go2super.database.repository;

import com.go2super.database.entity.GameBoost;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface GameBoostRepository extends MongoRepository<GameBoost, ObjectId>, Serializable {

    List<GameBoost> findAll();

    GameBoost findByMimeType(int mimeType);

    GameBoost findByPropId(int propId);

    long count();

}