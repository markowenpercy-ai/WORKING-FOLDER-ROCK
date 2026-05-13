package com.go2super.database.repository.custom;

import com.go2super.database.entity.Trade;
import com.go2super.database.entity.type.TradeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

public class TradeRepositoryImpl implements TradeRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<Trade> findByPage(int page, int max) {

        Query query = new Query();
        query.with(Sort.by(Arrays.asList(
            new Sort.Order(Sort.Direction.DESC, "priceType"),
            new Sort.Order(Sort.Direction.ASC, "price"),
            new Sort.Order(Sort.Direction.DESC, "tradeType"))));
        query.skip(page <= 0 ? 0 : (long) page * max);
        query.limit(max);

        List<Trade> results = mongoTemplate.find(query, Trade.class);
        return results;

    }

    @Override
    public List<Trade> findByPageAndType(TradeType tradeType, int page, int max) {

        Criteria criteria = Criteria.where("tradeType").is(tradeType);
        Query query = new Query();
        query.addCriteria(criteria);
        query.with(Sort.by(Arrays.asList(
            new Sort.Order(Sort.Direction.DESC, "priceType"),
            new Sort.Order(Sort.Direction.ASC, "price"))));
        query.skip(page <= 0 ? 0 : (long) page * max);
        query.limit(max);

        List<Trade> results = mongoTemplate.find(query, Trade.class);
        return results;

    }

    @Override
    public List<Trade> findByPageAndTypeAndSellId(TradeType tradeType, int sellId, int page, int max) {

        Criteria criteria = Criteria.where("tradeType").is(tradeType).andOperator(Criteria.where("sellId").is(sellId));
        Query query = new Query();
        query.addCriteria(criteria);
        query.with(Sort.by(Arrays.asList(
            new Sort.Order(Sort.Direction.DESC, "priceType"),
            new Sort.Order(Sort.Direction.ASC, "price"))));
        query.skip(page <= 0 ? 0 : (long) page * max);
        query.limit(max);

        List<Trade> results = mongoTemplate.find(query, Trade.class);
        return results;

    }

    @Override
    public long countByType(TradeType tradeType) {

        Criteria criteria = Criteria.where("tradeType").is(tradeType);
        Query query = new Query();
        query.addCriteria(criteria);

        long result = mongoTemplate.count(query, Trade.class);
        return result;

    }

}
