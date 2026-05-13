package com.go2super.database.repository;

import com.go2super.database.entity.DashboardAccountSession;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface DashboardAccountSessionRepository extends MongoRepository<DashboardAccountSession, ObjectId>, Serializable {

    List<DashboardAccountSession> findAll();

    List<DashboardAccountSession> findByAccountIdAndExpired(String accountId, boolean expired);

    Optional<DashboardAccountSession> findByToken(String token);

}