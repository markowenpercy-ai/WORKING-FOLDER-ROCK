package com.go2super.database.repository;

import com.go2super.database.entity.DashboardAccount;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface DashboardAccountRepository extends MongoRepository<DashboardAccount, ObjectId>, Serializable {

    List<DashboardAccount> findAll();

    Optional<DashboardAccount> findById(String id);

    Optional<DashboardAccount> findByEmail(String email);

}