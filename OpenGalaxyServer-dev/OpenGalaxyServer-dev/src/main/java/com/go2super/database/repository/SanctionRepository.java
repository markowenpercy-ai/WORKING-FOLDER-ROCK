package com.go2super.database.repository;

import com.go2super.database.entity.Sanction;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface SanctionRepository extends MongoRepository<Sanction, ObjectId>, Serializable {

    List<Sanction> findAll();

    long count();

}