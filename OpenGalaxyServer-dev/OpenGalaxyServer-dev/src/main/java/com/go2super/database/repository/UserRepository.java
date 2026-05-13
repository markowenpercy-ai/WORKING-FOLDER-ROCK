package com.go2super.database.repository;

import com.go2super.database.entity.User;
import com.go2super.database.repository.custom.UserRepositoryCustom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.io.Serializable;
import java.util.*;

public interface UserRepository extends MongoRepository<User, ObjectId>, UserRepositoryCustom, Serializable {

    User findTopByOrderByIdDesc();

    List<User> findAll();

    List<User> findByAccountId(ObjectId accountId);

    @Query(value = "{ 'guid' : {'$in' : ?0 } }")
    List<User> findByGuid(Collection<Integer> guids);

    User findByGuid(int guid);

    List<User> findByConsortiaId(int ConsortiaId);

    User findByUserId(long userId);

    Optional<User> findByUsername(String username);

}