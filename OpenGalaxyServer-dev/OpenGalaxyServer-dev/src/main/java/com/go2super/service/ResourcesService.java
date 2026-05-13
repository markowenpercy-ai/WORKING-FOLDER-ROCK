package com.go2super.service;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.CorpMember;
import com.go2super.database.entity.sub.UserRewards;
import com.go2super.database.repository.GameBoostRepository;
import com.go2super.obj.utility.GameDate;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.boot.ResponsePlayerResourcePacket;
import com.go2super.packet.boot.ResponseRoleInfoPacket;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.packet.reward.ResponseOnlineAwardPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.json.RewardsJson;
import com.go2super.socket.util.DateUtil;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ResourcesService {

    private static ResourcesService resourcesService;

    @Autowired
    @Getter
    private GameBoostRepository boostRepository;

    public ResourcesService() {

        resourcesService = this;
    }

    public ResponsePlayerResourcePacket getPlayerResourcePacket(User user) {

        ResponsePlayerResourcePacket packet = new ResponsePlayerResourcePacket();

        packet.setUserGas(UnsignedInteger.of(user.getResources().getHe3()));
        packet.setUserMetal(UnsignedInteger.of(user.getResources().getMetal()));
        packet.setUserMoney(UnsignedInteger.of(user.getResources().getGold()));

        packet.setCredit(UnsignedInteger.of(user.getResources().getMallPoints()));
        packet.setLevel(user.getStats().getLevel());
        packet.setExp(user.getStats().getExp());
        packet.setCoins(Long.valueOf(user.getResources().getVouchers()).intValue());
        packet.setOutGas(user.getStorage().getHe3Production());
        packet.setOutMetal(user.getStorage().getMetalProduction());
        packet.setOutMoney(user.getStorage().getGoldProduction());
        packet.setMaxSpValue(user.getStats().getMaxSp());
        packet.setSpValue(user.getSp());
        packet.setMoneyBuyNum(0);
        packet.setDefyEctypeNum(user.getRestrictedUsedEntries());
        packet.setMatchCount(0);
        packet.setTollGate(user.getTrial()); // This is the current Trial (1 - 10)
        packet.setReserve(0);

        user.update();
        user.save();

        return packet;

    }

    public ResponseRoleInfoPacket getRoleInfoPacket(User user) {

        ResponseRoleInfoPacket packet = new ResponseRoleInfoPacket();

        Corp corp = CorpService.getInstance().getCorpByUser(user.getGuid());

        packet.setGMapId(user.getGMapId());
        packet.setGId(GalaxyService.getInstance().getUserPlanet(user).getPosition().galaxyId());
        packet.setPropsPack(user.getInventory().getMaximumStacks());
        packet.setPropsCorpPack(0);

        if (corp != null) {

            CorpMember corpMember = corp.getMembers().getMember(user.getGuid());

            packet.setConsortiaId(corp.getCorpId());
            packet.setConsortiaJob((byte) corpMember.getRank());
            packet.setConsortiaUnionLevel((byte) corp.getMergingLevel());
            packet.setConsortiaShopLevel((byte) corp.getMallLevel());

            if (user.getCorpInventory() != null) {

                packet.setPropsCorpPack(user.getCorpInventory().getMaxStacks());

            }

            packet.setConsortiaThrow(corpMember.getContribution());

        } else {

            packet.setConsortiaId(-1);
            packet.setConsortiaJob((byte) -1);
            packet.setConsortiaUnionLevel((byte) -1);
            packet.setConsortiaShopLevel((byte) -1);

            packet.setConsortiaThrow(-1);

        }

        packet.setConsortiaUnion(-1);
        packet.setConsortiaShop(-1);

        packet.setGameServerId((byte) 0);
        packet.setCardCredit(3);

        packet.setCard1(100);
        packet.setCard2(200);
        packet.setCard3(400);

        packet.setCardUnion(0);
        packet.setChargeFlag(1);

        packet.setAddPackMoney(user.getInventory().getStackPrice());
        packet.setShipSpeedCredit(8);
        packet.setLotteryCredit(5);

        int userSpins = user.getSpins();

        packet.setLotteryStatus(user.getMaxSpins() - userSpins);
        packet.setName(SmartString.of(user.getUsername(), 32));
        packet.setEctypeNum(user.getStats().getInstance());

        packet.setBadge(Long.valueOf(user.getResources().getBadge()).intValue());
        packet.setHonor(Long.valueOf(user.getResources().getHonor()).intValue());
        packet.setServerTime(UnsignedInteger.of(new GameDate().getSeconds()));

        packet.setTollGate(user.getTrial()); // This is the current Trial (1 - 10)

        Calendar calendar = Calendar.getInstance();

        packet.setYear((short) calendar.get(Calendar.YEAR));
        packet.setMonth((byte) calendar.get(Calendar.MONTH));
        packet.setDay((byte) calendar.get(Calendar.DAY_OF_MONTH));

        packet.setNoviceGuide(0); // what the fuck is that?
        packet.setWarScore(UnsignedInteger.of(user.getResources().getChampionPoints()));

        user.update();
        user.save();

        return packet;

    }

    public ResponseOnlineAwardPacket getOnlineAwardPacket(User user) {

        UserRewards userRewards = user.getRewards();
        Account account = user.getAccount();
        boolean isMain = account.getMainPlanetId() != null && user.getUserId() == account.getMainPlanetId();
        RewardsJson rewardsJson = ResourceManager.getRewards(isMain);

        if (user.getLastDayUpdate() != null) {
            if (!DateUtil.currentDay(user.getLastDayUpdate())) {

                userRewards.setLevel(0);
                userRewards.setUntil(DateUtil.now(rewardsJson.getReward(0).getTime()));

            }
        }

        if (userRewards.getUntil() == null && userRewards.getLevel() != -1) {

            userRewards.setLevel(0);
            userRewards.setUntil(DateUtil.now(rewardsJson.getReward(0).getTime()));

        }

        if (userRewards.getUntil() == null) {
            return null;
        }

        user.update();
        user.save();

        ResponseOnlineAwardPacket response = new ResponseOnlineAwardPacket();

        response.setSpareTime(DateUtil.remains(userRewards.getUntil()).intValue());
        response.setPropsNum(0);
        response.setPropsId(-1);

        return response;

    }

    public ResponseUsePropsPacket itemAward(int propId) {

        return itemAward(propId, 1);
    }

    public ResponseUsePropsPacket itemAward(int propId, int quantity) {

        return genericUseProps(propId, quantity, 1, 1);
    }

    public ResponseUsePropsPacket smartUseProps(int propId, int number, int lockFlag, int metal, int he3, int gold, int awardFlag, int awardLockFlag, Pair<PropData, Integer> reward) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setLockFlag((byte) lockFlag);

        packet.setAwardFlag((byte) awardFlag);
        packet.setAwardLockFlag((byte) awardLockFlag);

        packet.setAwardMetal(metal);
        packet.setAwardGas(he3);
        packet.setAwardMoney(gold);

        int id = reward.getLeft().getId();
        int amount = reward.getRight();

        packet.getAwardPropsId().getArray()[0] = id;
        packet.getAwardPropsNum().getArray()[0] = amount;

        packet.setAwardPropsLen(1);
        return packet;

    }

    public ResponseUsePropsPacket smartUseProps(int propId, int number, int lockFlag, int metal, int he3, int gold, int awardFlag, int awardLockFlag, List<Pair<Integer, Integer>> props) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setLockFlag((byte) lockFlag);

        packet.setAwardFlag((byte) awardFlag);
        packet.setAwardLockFlag((byte) awardLockFlag);

        packet.setAwardMetal(metal);
        packet.setAwardGas(he3);
        packet.setAwardMoney(gold);

        for (int i = 0; i < props.size(); i++) {

            Pair<Integer, Integer> reward = props.get(i);

            int id = reward.getLeft();
            int amount = reward.getRight();

            packet.getAwardPropsId().getArray()[i] = id;
            packet.getAwardPropsNum().getArray()[i] = amount;

        }

        packet.setAwardPropsLen(props.size());
        return packet;

    }

    public ResponseUsePropsPacket genericUseProps(int propId, int number, int lockFlag, int awardLockFlag) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setLockFlag((byte) lockFlag);
        packet.setAwardLockFlag((byte) awardLockFlag);

        return packet;

    }

    public ResponseUsePropsPacket genericUseProps(int propId, int number, int sp, int lockFlag, int awardLockFlag) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setSpValue(sp);
        packet.setLockFlag((byte) lockFlag);
        packet.setAwardLockFlag((byte) awardLockFlag);

        return packet;

    }


    public ResponseUsePropsPacket genericUseProps(int propId, int number, int metal, int he3, int gold, int corsairs, int lockFlag, int awardFlag, int awardLockFlag) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setLockFlag((byte) lockFlag);

        packet.setAwardFlag((byte) awardFlag);
        packet.setAwardLockFlag((byte) awardLockFlag);

        packet.setAwardMetal(metal);
        packet.setAwardGas(he3);
        packet.setAwardMoney(gold);
        packet.setPirateMoney(corsairs);

        return packet;

    }

    public ResponseUsePropsPacket genericUseProps(int propId, int number, int metal, int he3, int gold, int lockFlag, int awardFlag, int awardLockFlag) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setLockFlag((byte) lockFlag);

        packet.setAwardFlag((byte) awardFlag);
        packet.setAwardLockFlag((byte) awardLockFlag);

        packet.setAwardMetal(metal);
        packet.setAwardGas(he3);
        packet.setAwardMoney(gold);

        return packet;

    }

    public ResponseUsePropsPacket genericUseProps(int propId, int number, int metal, int he3, int gold, int vouchers, int activeCredit, int lockFlag, int awardFlag, int awardLockFlag) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setLockFlag((byte) lockFlag);

        packet.setAwardFlag((byte) awardFlag);
        packet.setAwardLockFlag((byte) awardLockFlag);

        packet.setAwardCoins(vouchers);
        packet.setAwardActiveCredit(activeCredit);
        packet.setAwardMetal(metal);
        packet.setAwardGas(he3);
        packet.setAwardMoney(gold);

        return packet;

    }

    public ResponseUsePropsPacket genericUseProps(int propId, int number, int metal, int he3, int gold, int vouchers, int honor, int badge, int activeCredit, int lockFlag, int awardFlag, int awardLockFlag) {

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(propId);
        packet.setNumber(number);
        packet.setLockFlag((byte) lockFlag);

        packet.setAwardFlag((byte) awardFlag);
        packet.setAwardLockFlag((byte) awardLockFlag);

        packet.setAwardHonor(honor);
        packet.setAwardBadge(badge);
        packet.setAwardCoins(vouchers);
        packet.setAwardActiveCredit(activeCredit);
        packet.setAwardMetal(metal);
        packet.setAwardGas(he3);
        packet.setAwardMoney(gold);

        return packet;

    }

    public static ResourcesService getInstance() {

        return resourcesService;
    }

}
