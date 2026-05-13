package com.go2super.service;

import com.go2super.database.cache.*;
import com.go2super.database.entity.AutoIncrement;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public class AutoIncrementService {

    private static AutoIncrementService instance;

    private final AutoIncrementCache autoIncrementCache;
    private final ShipModelCache shipModelCache;
    private final CommanderCache commanderCache;
    private final PlanetCache planetCache;
    private final FleetCache fleetCache;
    private final TradeCache tradeCache;
    private final CorpCache corpCache;
    private final UserCache userCache;

    @Autowired
    public AutoIncrementService(AutoIncrementCache autoIncrementCache, CommanderCache commanderCache, PlanetCache planetCache, FleetCache fleetCache, UserCache userCache, CorpCache corpCache, ShipModelCache shipModelCache, TradeCache tradeCache) {

        instance = this;

        this.autoIncrementCache = autoIncrementCache;
        this.shipModelCache = shipModelCache;
        this.commanderCache = commanderCache;
        this.planetCache = planetCache;
        this.fleetCache = fleetCache;
        this.corpCache = corpCache;
        this.userCache = userCache;
        this.tradeCache = tradeCache;

    }

    public synchronized int getNextCommanderId() {

        final String pointer = "game_commanders";
        int current = getCurrent(pointer);

        setCurrent(pointer, ++current);
        return current;

    }

    public synchronized int getNextCorpId() {

        final String pointer = "game_corps";
        int current = getCurrent(pointer);

        setCurrent(pointer, ++current);
        return current;

    }

    public synchronized int getNextTestId() {

        final String pointer = "game_test";
        int current = getCurrent(pointer);
        //while(fleetCache.findByShipTeamId(current) != null) ++current;

        setCurrent(pointer, ++current);
        return current;

    }

    public synchronized int getNextFleetId() {

        final String pointer = "game_fleets";
        int current = getCurrent(pointer);
        // while(fleetCache.findByShipTeamId(current) != null) ++current;

        setCurrent(pointer, ++current);
        return current;

    }

    @Deprecated
    public synchronized int getNextMatchId() {

        final String pointer = "game_matches";
        int current = getCurrent(pointer);

        setCurrent(pointer, ++current);
        return current;

    }

    public synchronized int getNextShipModelId() {

        final String pointer = "game_models";
        int current = getCurrent(pointer);

        setCurrent(pointer, ++current);
        return current;

    }

    public synchronized long getNextUserId() {

        final String pointer = "game_planets";
        int current = getCurrent(pointer);

        setCurrent(pointer, ++current);
        return Long.valueOf(current);

    }

    public synchronized int getNextTradeId() {

        final String pointer = "game_trades";
        int current = getCurrent(pointer);

        setCurrent(pointer, ++current);
        return current;

    }

    public synchronized int getNextGuid() {

        final String pointer = "game_users";
        int current = getCurrent(pointer);

        setCurrent(pointer, ++current);
        return current;

    }

    public static synchronized int getCurrent(String name) {

        AutoIncrement autoIncrement = instance.getAutoIncrementCache().findByName(name);

        if (autoIncrement == null) {
            int defaultCurrent = "game_models".equals(name) ? 175 : 1;
            autoIncrement = AutoIncrement.builder()
                .name(name)
                .current(defaultCurrent)
                .build();
            instance.getAutoIncrementCache().save(autoIncrement);
            return defaultCurrent;
        }

        synchronized (autoIncrement) {
            return autoIncrement.getCurrent();
        }

    }

    public static synchronized int setCurrent(String name, int current) {

        AutoIncrement autoIncrement = instance.getAutoIncrementCache().findByName(name);

        if (autoIncrement == null) {
            autoIncrement = AutoIncrement.builder()
                .name(name)
                .current(current)
                .build();
            instance.getAutoIncrementCache().save(autoIncrement);
            return current;
        }

        synchronized (autoIncrement) {
            autoIncrement.setCurrent(current);
            instance.getAutoIncrementCache().save(autoIncrement);
            return current;
        }

    }

    public static AutoIncrementService getInstance() {

        return instance;
    }

}
