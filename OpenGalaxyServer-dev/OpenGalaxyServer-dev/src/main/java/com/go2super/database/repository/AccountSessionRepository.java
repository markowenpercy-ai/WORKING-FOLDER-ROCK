package com.go2super.database.repository;

import com.go2super.database.entity.AccountSession;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface AccountSessionRepository extends MongoRepository<AccountSession, ObjectId>, Serializable {

    List<AccountSession> findAll();

    List<AccountSession> findByAccountId(String accountId);

    Optional<AccountSession> findByToken(String token);

    Optional<AccountSession> findByAccountIdAndExpiredFalse(String accountId);

}