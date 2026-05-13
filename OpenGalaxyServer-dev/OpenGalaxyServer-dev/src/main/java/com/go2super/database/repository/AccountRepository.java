package com.go2super.database.repository;

import com.go2super.database.entity.Account;
import com.go2super.database.repository.custom.AccountRepositoryCustom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface AccountRepository extends MongoRepository<Account, ObjectId>, AccountRepositoryCustom, Serializable {

    List<Account> findAll();

    Optional<Account> findById(ObjectId id);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByUsername(String username);

}