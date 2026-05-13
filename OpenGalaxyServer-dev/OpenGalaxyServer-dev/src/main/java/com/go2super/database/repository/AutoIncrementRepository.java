package com.go2super.database.repository;

import com.go2super.database.entity.AutoIncrement;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface AutoIncrementRepository extends MongoRepository<AutoIncrement, ObjectId>, Serializable {

    AutoIncrement findTopByOrderByIdDesc();

    List<AutoIncrement> findAll();

    Optional<AutoIncrement> findByName(String name);

}