package com.go2super.database.repository;

import com.go2super.database.entity.Trade;
import com.go2super.database.repository.custom.TradeRepositoryCustom;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.io.Serializable;
import java.util.*;

public interface TradeRepository extends MongoRepository<Trade, ObjectId>, TradeRepositoryCustom, Serializable {

    Trade findTopByOrderByIdDesc();

    List<Trade> findAll();

    @Query(value = "{ 'tradeId' : {'$in' : ?0 } }")
    List<Trade> findByTradeId(Collection<Integer> tradeIds);

    Trade findByTradeId(int tradeId);

    List<Trade> findAllBySellerGuid(int sellerGuid);

    long count();

}