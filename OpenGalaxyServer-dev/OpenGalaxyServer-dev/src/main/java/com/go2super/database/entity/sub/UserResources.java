package com.go2super.database.entity.sub;

import com.go2super.logger.BotLogger;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Builder
@Data
public class UserResources {
    // TODO: Use larger maximum value once flash client is retired.
    public static final long MAX_RESOURCES = Integer.MAX_VALUE;
    // public static final long MAX_RESOURCES = 9_000_000_000_000_000_000L;

    private long gold;
    private long he3;
    private long metal;

    private long vouchers;
    private long mallPoints;
    private long coupons;
    private long corsairs;

    private long honor;
    private long badge;
    private long championPoints;

    private int freeSpins;
    private Date lastSpin;

    public void refresh() {

        if (gold < 0 || gold > MAX_RESOURCES) {
            BotLogger.info("User gold is: " + gold);
            gold = gold < 0 ? 0 : MAX_RESOURCES;
        }

        if (he3 < 0 || he3 > MAX_RESOURCES) {
            he3 = he3 < 0 ? 0 : MAX_RESOURCES;
        }

        if (metal < 0 || metal > MAX_RESOURCES) {
            metal = metal < 0 ? 0 : MAX_RESOURCES;
        }

        if (mallPoints < 0 || mallPoints > MAX_RESOURCES) {
            mallPoints = mallPoints < 0 ? 0 : MAX_RESOURCES;
        }

        if (vouchers < 0 || vouchers > MAX_RESOURCES) {
            vouchers = vouchers < 0 ? 0 : MAX_RESOURCES;
        }

    }

    public void addGold(long gold) {

        if (this.gold + gold > MAX_RESOURCES) {
            this.gold = MAX_RESOURCES;
            return;
        }

        this.gold += gold;

    }

    public void addHe3(long he3) {

        if (this.he3 + he3 > MAX_RESOURCES) {
            this.he3 = MAX_RESOURCES;
            return;
        }

        this.he3 += he3;

    }

    public void addMetal(long metal) {

        if (this.metal + metal > MAX_RESOURCES) {
            this.metal = MAX_RESOURCES;
            return;
        }

        this.metal += metal;

    }

    public void addMallPoints(int mallPoints) {

        if (this.mallPoints + mallPoints > MAX_RESOURCES) {
            this.mallPoints = MAX_RESOURCES;
            return;
        }

        this.mallPoints += mallPoints;

    }

    public void addVouchers(int vouchers) {

        if (this.vouchers + vouchers > MAX_RESOURCES) {
            this.vouchers = MAX_RESOURCES;
            return;
        }

        this.vouchers += vouchers;

    }

    public void addCorsairs(int corsairs) {

        if (this.corsairs + corsairs > MAX_RESOURCES) {
            this.corsairs = MAX_RESOURCES;
            return;
        }

        this.corsairs += corsairs;

    }

    public void addChampionPoints(int championPoints) {

        if (this.championPoints + championPoints > MAX_RESOURCES) {
            this.championPoints = MAX_RESOURCES;
            return;
        }

        this.championPoints += championPoints;

    }

    public void addBadges(int badges) {

        if (this.badge + badges > MAX_RESOURCES) {
            this.badge = MAX_RESOURCES;
            return;
        }

        this.badge += badges;

    }

    public void addHonor(int honor) {

        if (this.honor + honor > MAX_RESOURCES) {
            this.honor = MAX_RESOURCES;
            return;
        }

        this.honor += honor;

    }

    public void removeSpin() {

        if (freeSpins > 0) {
            this.freeSpins -= 1;
        }

    }

}
