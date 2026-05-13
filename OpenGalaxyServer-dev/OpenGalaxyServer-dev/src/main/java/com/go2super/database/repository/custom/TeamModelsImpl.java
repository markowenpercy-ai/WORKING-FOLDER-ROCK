package com.go2super.database.repository.custom;

import com.go2super.database.entity.TeamModelSlot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class TeamModelsImpl implements TeamModelsCustomRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    public TeamModelSlot findByGuidAndIndexId(int guid, int indexId) {

        Criteria criteriaGuid = Criteria.where("guid").is(guid);
        Criteria criteriaIndexId = Criteria.where("indexId").is(indexId);

        Query query = new Query();
        query.addCriteria(criteriaGuid);
        query.addCriteria(criteriaIndexId);

        return mongoTemplate.findOne(query, TeamModelSlot.class);

    }

}
