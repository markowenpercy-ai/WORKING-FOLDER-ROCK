package com.go2super.database.repository.custom;

import com.go2super.database.entity.Fleet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

public class FleetRepositoryImpl implements FleetRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<Fleet> getInTransmissionFleets(int guid) {

        Criteria criteria = new Criteria();
        criteria.andOperator(Criteria.where("fleetTransmission").ne(null), Criteria.where("guid").is(guid));

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Fleet.class);

    }

    @Override
    public List<Fleet> getRadarFleets(int guid, int toGalaxyId) {

        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("guid").is(guid),
            Criteria.where("fleetTransmission.galaxyId").is(toGalaxyId));

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Fleet.class);

    }

    @Override
    public List<Fleet> getInWarFleets() {

        Criteria criteria = Criteria.where("fleetInitiator").ne(null);

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Fleet.class);

    }

    @Override
    public List<Fleet> getInTransmissionFleets() {

        Criteria criteria = Criteria.where("fleetTransmission").ne(null);

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Fleet.class);

    }

}
