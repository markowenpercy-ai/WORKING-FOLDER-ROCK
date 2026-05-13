package com.go2super.database.repository;

import com.go2super.database.entity.UserIP;
import com.go2super.database.repository.custom.UserIPRepositoryCustom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface UserIPRepository extends MongoRepository<UserIP, ObjectId>, UserIPRepositoryCustom, Serializable {

    UserIP findTopByOrderByIdDesc();

    List<UserIP> findAll();

    long count();

}