package com.go2super.database.entity.sub;

import com.go2super.database.entity.GameBoost;
import com.go2super.obj.type.BonusType;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.LevelData;
import com.go2super.socket.util.DateUtil;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserStats {

    private int level;
    private int exp;

    private int restrictedUsedEntries;

    private int raidAttemptsEntries;
    private int raidInterceptEntries;

    private int instance;
    private int trial;

    private int sp;
    private int kills;

    private Date nextInvitation;
    private boolean collectedPoints;

    private List<UserBoost> buffs = new ArrayList<>();
    private UserLeagueStats leagueStats;
    private UserChampStats champStats;
    private UserIglStats iglStats;

    public void addExp(int exp) {

        int maxExp = ResourceManager.getLevels().getMaxLevelExp(getLevel());
        if (maxExp == 0) {
            return;
        }

        if (this.exp + exp > maxExp) {

            int leftover = (this.exp + exp) - maxExp;
            this.level++;
            this.exp = 0;
            addExp(leftover);
            return;

        }

        this.exp += exp;

    }

    public int getMaxSp() {

        return 20 + level;
    }

    public boolean hasTruce() {

        List<UserBoost> buffs = getUserBonus(BonusType.PLANET_PROTECTION);
        for (UserBoost boost : buffs) {
            if (boost.getUntil() != null && boost.getUntil().after(new Date())) {
                return true;
            }
        }
        return false;
    }

    public double getShipBuildingBuff() {
        double buff = 0.00d;
        List<BonusType> buffs = getAllBonuses();
        for (BonusType bonusType : buffs) {
            switch (bonusType) {
                case MVP_SHIP_BUILDING_RATE -> buff += BonusType.MVP_SHIP_BUILDING_RATE.delta();
                case GF_SHIP_BUILDING_SPEED -> buff += BonusType.GF_SHIP_BUILDING_SPEED.delta();
                case HALLOWEEN_SHIP_BUILDING_RATE -> buff += BonusType.HALLOWEEN_SHIP_BUILDING_RATE.delta();
                case CHRISTMAS_SHIP_BUILDING_SPEED -> buff += BonusType.CHRISTMAS_SHIP_BUILDING_SPEED.delta();
                default -> {
                }
            }
        }
        return buff;
    }

    public double getRepairBuff() {
        double buff = 0.00d;
        List<BonusType> buffs = getAllBonuses();
        for (BonusType bonusType : buffs) {
            switch (bonusType) {
                case MVP_SHIP_REPAIRING_RATE -> buff += BonusType.MVP_SHIP_REPAIRING_RATE.delta();
                case GF_SHIP_REPAIRING_SPEED -> buff += BonusType.GF_SHIP_REPAIRING_SPEED.delta();
                default -> {
                }
            }
        }
        return buff;
    }

    public List<UserBoost> getUserBonus(BonusType bonusType) {

        return getBuffs().stream()
                .filter(userBoost -> userBoost.boost().getBonuses().contains(bonusType))
                .collect(Collectors.toList());
    }

    public boolean hasBonus(BonusType bonusType) {

        return getAllBonuses().contains(bonusType);
    }

    public List<BonusType> getAllBonuses() {

        List<BonusType> bonusList = new ArrayList<>();
        for (UserBoost userBoost : getBuffs()) {
            for (BonusType bonusType : userBoost.boost().getBonuses()) {
                bonusList.add(bonusType);
            }
        }
        return bonusList;
    }

    public void removeBoost(BonusType bonusType) {

        List<UserBoost> boosts = getUserBonus(bonusType);
        if (boosts.isEmpty()) {
            return;
        }
        buffs.removeAll(boosts);
    }

    public void removeBoost(UserBoost userBoost) {

        buffs.remove(userBoost);
    }

    public void addBonusTime(GameBoost gameBoost, int seconds) {

        if (buffs == null) {
            buffs = new ArrayList<>();
        }

        UserBoost oldBoost = getUserBoost(gameBoost);

        if (oldBoost == null) {
            buffs.add(new UserBoost(gameBoost.getId(), DateUtil.now(seconds)));
            return;
        }
        oldBoost.addSeconds(seconds);
    }

    public UserBoost getUserBoost(GameBoost gameBoost) {

        if (buffs == null) {
            buffs = new ArrayList<>();
        }
        Set<UserBoost> s = new HashSet<>(buffs);
        buffs = new ArrayList<>();
        buffs.addAll(s);
        for (UserBoost boost : buffs) {
            if (boost.getGameBoostId().equals(gameBoost.getId())) {
                return boost;
            }
        }

        return null;

    }

    public UserChampStats getUserChampStats() {
        if (champStats == null) {
            champStats = new UserChampStats(0, 0, 0);
        }
        return champStats;
    }

    public UserIglStats getUserIglStats() {
        if (iglStats == null) {
            iglStats = new UserIglStats(false, 0, new ArrayList<>(), 999_999);
        }
        return iglStats;
    }


    public void setBuffs(List<UserBoost> buffs) {
        this.buffs = buffs;
    }

    public List<UserBoost> getBuffs() {
        if (buffs == null) {
            this.buffs = new ArrayList<>();
        }
        return buffs;
    }

    public LevelData getLevelData() {

        return ResourceManager.getLevels().getData(level);
    }

}
