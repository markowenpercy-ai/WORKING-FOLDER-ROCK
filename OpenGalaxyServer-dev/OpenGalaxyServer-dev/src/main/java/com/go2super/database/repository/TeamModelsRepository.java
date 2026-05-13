package com.go2super.database.repository;

import com.go2super.database.entity.TeamModelSlot;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface TeamModelsRepository extends MongoRepository<TeamModelSlot, ObjectId>, Serializable {

    List<TeamModelSlot> findAll();

    TeamModelSlot findByGuidAndIndexId(int guid, int indexId);

    List<TeamModelSlot> findByGuid(int guid);

}