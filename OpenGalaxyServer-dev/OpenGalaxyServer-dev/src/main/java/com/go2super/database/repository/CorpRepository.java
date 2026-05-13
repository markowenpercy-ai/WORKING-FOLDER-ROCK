package com.go2super.database.repository;

import com.go2super.database.entity.Corp;
import com.go2super.database.repository.custom.CorpRepositoryCustom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.io.Serializable;
import java.util.*;

public interface CorpRepository extends MongoRepository<Corp, ObjectId>, CorpRepositoryCustom, Serializable {

    Corp findTopByOrderByIdDesc();

    List<Corp> findAll();

    List<Corp> findByBlocId(String blocId);

    @Query(value = "{ 'corpId' : {'$in' : ?0 } }")
    List<Corp> findByCorpId(List<Integer> corpIds);

    Corp findByCorpId(int corpId);

    long count();

    Corp findByAcronym(String acronym);

}