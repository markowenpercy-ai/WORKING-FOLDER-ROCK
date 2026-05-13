package com.go2super.database.repository.custom;

import com.go2super.database.entity.Trade;
import com.go2super.database.entity.type.TradeType;

import java.util.*;

public interface TradeRepositoryCustom {

    List<Trade> findByPage(int page, int max);

    List<Trade> findByPageAndType(TradeType tradeType, int page, int max);

    List<Trade> findByPageAndTypeAndSellId(TradeType tradeType, int sellId, int page, int max);

    long countByType(TradeType tradeType);

}
