package com.go2super.database.repository.custom;

import com.go2super.database.entity.Corp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

public class CorpRepositoryImpl implements CorpRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Corp findByGuid(int guid) {

        Criteria criteria = Criteria.where("corp_members.members.guid").is(guid);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Corp.class);

    }

    public List<Corp> findByCorpUpgrade() {

        Criteria criteria = Criteria.where("corp_upgrade").ne(null);

        Query query = new Query();
        query.addCriteria(criteria);

        List<Corp> corps = mongoTemplate.find(query, Corp.class);

        return corps;

    }

    public List<Corp> findRecruitsByGuid(int guid) {

        Criteria criteria = Criteria.where("corp_members.recruits.guid").is(guid);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Corp.class);

    }

    public Corp findByName(String name) {

        Criteria criteria = Criteria.where("name").is(name);
        Query query = new Query(criteria);
        return mongoTemplate.findOne(query, Corp.class);

    }

    public List<Corp> findByStartWithName(String name) {

        Criteria criteria = Criteria.where("name").regex("^" + name);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, Corp.class);

    }

}
