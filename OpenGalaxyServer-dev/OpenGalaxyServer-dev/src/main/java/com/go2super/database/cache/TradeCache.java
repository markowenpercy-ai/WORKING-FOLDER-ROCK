package com.go2super.database.cache;

import com.go2super.database.entity.Trade;
import com.go2super.database.entity.type.TradeType;
import com.go2super.database.repository.TradeRepository;
import com.go2super.socket.util.MathUtil;
import com.go2super.utility.GlueList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class TradeCache {

    private static final CopyOnWriteArrayList<Trade> cache = new CopyOnWriteArrayList<>();

    private TradeRepository repository;

    @Autowired
    public void TradeCache(TradeRepository repository) {
        this.repository = repository;
        cache.addAll(repository.findAll());

    }

    public List<Trade> findAll() {

        return new GlueList<>(cache);
    }

    public void save(Trade value) {

        value = repository.save(value);
        if (!cache.contains(value)) {
            cache.add(value);
        }
    }

    public void delete(Trade value) {

        remove(value);
    }

    public void remove(Trade value) {

        repository.delete(value);
        cache.remove(value);
    }

    public List<Trade> findByTradeId(Collection<Integer> tradeIds) {

        return cache.stream().filter(value -> tradeIds.contains(value.getTradeId())).collect(Collectors.toList());
    }

    public Trade findByTradeId(int tradeId) {

        return cache.stream().filter(value -> value.getTradeId() == tradeId).findFirst().orElse(null);
    }

    public List<Trade> findAllBySellerGuid(int sellerGuid) {

        return cache.stream().filter(value -> value.getSellerGuid() == sellerGuid).collect(Collectors.toList());
    }

    public List<Trade> findByPage(int page, int max) {

        return cache.stream()
                .sorted(Comparator.comparing(Trade::getPriceType).reversed()
                        .thenComparing(Trade::getPrice)
                        .thenComparing(Trade::getTradeType))
                .skip(page <= 0 ? 0 : (long) page * max).limit(max).collect(Collectors.toList());
    }

    private List<Trade> findFilteredByPage(Predicate<Trade> predicate, Comparator<Trade> comparator, int page, int max) {
        return cache.stream()
                .filter(predicate)
                .sorted(comparator)
                .skip(page <= 0 ? 0 : (long) page * max)
                .limit(max)
                .collect(Collectors.toList());
    }

    public List<Trade> findByPageAndType(TradeType tradeType, int page, int max) {
        return findFilteredByPage(value -> value.getTradeType().equals(tradeType),
                Comparator.comparing(Trade::getPriceType).reversed().thenComparing(Trade::getPrice), page, max);
    }

    public List<Trade> findByPageAndTypeAndSellId(TradeType tradeType, int sellId, int page, int max) {
        Comparator<Trade> cmp = Comparator.comparing(Trade::getPriceType).reversed().thenComparing(Trade::getPrice);
        if (tradeType.equals(TradeType.CARD)) {
            return findFilteredByPage(value -> value.getTradeType().equals(tradeType) && MathUtil.isInRange(value.getSellId(), sellId, sellId + 8),
                    cmp, page, max);
        }
        return findFilteredByPage(value -> value.getTradeType().equals(tradeType) && value.getSellId() == sellId, cmp, page, max);
    }

    public long countByTypeAndSellId(TradeType tradeType, int sellId) {
        if (tradeType.equals(TradeType.CARD)) {
            return countByPredicate(value -> value.getTradeType().equals(tradeType) && MathUtil.isInRange(value.getSellId(), sellId, sellId + 8));
        }
        return countByPredicate(value -> value.getTradeType().equals(tradeType) && value.getSellId() == sellId);
    }

    public long countByPredicate(Predicate<Trade> predicate) {
        return cache.stream().filter(predicate).count();
    }

    public long countByType(TradeType type) {

        return cache.stream().filter(value -> value.getTradeType().equals(type)).count();
    }

    public long count() {

        return cache.size();
    }

}
