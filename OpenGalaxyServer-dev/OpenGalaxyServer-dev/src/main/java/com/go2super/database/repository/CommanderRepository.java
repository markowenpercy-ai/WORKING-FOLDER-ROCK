package com.go2super.database.repository;

import com.go2super.database.entity.Commander;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface CommanderRepository extends MongoRepository<Commander, ObjectId>, Serializable {

    Commander findTopByOrderByIdDesc();

    Commander findBySkillAndUserId(int skill, long userId);

    Commander findByCommanderIdAndUserId(int commanderId, long userId);

    Commander findByCommanderId(int commanderId);

    Commander findByShipTeamId(int shipTeamId);

    List<Commander> findByUserId(long userId);

    long count();

}