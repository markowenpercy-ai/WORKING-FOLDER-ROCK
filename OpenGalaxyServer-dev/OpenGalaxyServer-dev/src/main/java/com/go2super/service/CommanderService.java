package com.go2super.service;

import com.go2super.database.cache.CommanderCache;
import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.database.entity.sub.BionicChip;
import com.go2super.database.entity.sub.CommanderExpertise;
import com.go2super.database.entity.sub.CommanderTrigger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.game.*;
import com.go2super.obj.utility.Gem;
import com.go2super.packet.chiplottery.ResponseCommanderInsertCmosPacket;
import com.go2super.packet.commander.*;
import com.go2super.packet.gems.ResponseCommanderInsertStonePacket;
import com.go2super.packet.gems.ResponseCommanderPropertyStonePacket;
import com.go2super.packet.gems.ResponseCommanderUnionStonePacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CommanderLevelsData;
import com.go2super.resources.data.CommanderPullulateData;
import com.go2super.resources.data.CommanderStatsData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.meta.CommanderProcMeta;
import com.go2super.socket.util.ListUtil;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.*;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_NUM;

@Getter
@Service
public class CommanderService {

    public static final String COMMON_EXPERTISE_PATTERN = "ABBBBBBCCCCCCCDDDDDDD";
    public static final int MAXIMUM_EXPERIENCE = 3748500;
    public static final int MAX_RESET = 100;
    public static final int MAX_VARIANCE = 30;
    public static final int MAX_GROWTH = 50;

    private static CommanderService instance;

    @Getter
    private final CommanderCache commanderCache;

    @Autowired
    public CommanderService(CommanderCache commanderCache) {

        instance = this;
        this.commanderCache = commanderCache;

    }

    public ResponseUnionCommanderCardBroPacket getUnionCommanderCardBroPacket(User user, int skillId, int stars, boolean failed) {

        ResponseUnionCommanderCardBroPacket response = new ResponseUnionCommanderCardBroPacket();

        response.setGuid(user.getGuid());
        response.setUserId(user.getPlanet().getUserId());
        response.setSkillId(skillId);
        response.setCardLevel(stars);
        response.setSuccessFlag(failed ? 0 : 1);
        response.getName().value(user.getUsername());

        return response;

    }

    public ResponseUnionDoubleSkillCardPacket getUnionDoubleCommanderCardPacket(int winCC, int card1, int card2, int goods, int goodsFlag, int chip, int chipFlag) {

        ResponseUnionDoubleSkillCardPacket response = new ResponseUnionDoubleSkillCardPacket();

        response.setPropsId(winCC);

        response.setCard1(card1);
        response.setCard2(card2);

        response.setGoods(goods);
        response.setGoodsLockFlag(goodsFlag);

        response.setChip(chip);
        response.setChipLockFlag(chipFlag);

        return response;

    }

    public ResponseUnionCommanderCardPacket getUnionCommanderCardPacket(int winCC, int card1, int card2, int card3, int goods, int goodsFlag) {

        ResponseUnionCommanderCardPacket response = new ResponseUnionCommanderCardPacket();

        response.setPropsId(winCC);

        response.setCard1(card1);
        response.setCard2(card2);
        response.setCard3(card3);

        response.setGoods(goods);
        response.setGoodsLockFlag(goodsFlag);
        return response;

    }

    public ResponseCommanderChangeCardPacket getSealCommanderPacket(User user, Prop sealCard, Commander commander) {

        ResponseCommanderChangeCardPacket response = new ResponseCommanderChangeCardPacket();

        user.getInventory().addProp(CommanderService.getInstance().getPropId(commander), 1, 0, false);

        response.setCommanderId(commander.getCommanderId());
        response.setPropsId(CommanderService.getInstance().getPropId(commander));
        response.setLockFlag(0);

        response.setUsePropsId(sealCard.getPropId());
        response.setUseLockFlag(sealCard.getPropLockNum());
        commander.delete();

        return response;

    }

    public ResponseCommanderUnionStonePacket getCommanderUnionStonePacket(User user, int result, boolean lock, boolean bro) {

        ResponseCommanderUnionStonePacket response = new ResponseCommanderUnionStonePacket();

        response.setGuid(user.getGuid());
        response.getName().value(user.getUsername());
        response.setBroFlag(bro ? 1 : 0);

        response.setLockFlag(lock ? 1 : 0);
        response.setPropsId(result);

        return response;

    }

    public ResponseCommanderPropertyStonePacket getCommanderPropertyStonePacket(User user, int type, int obj, int src1, int src2, int src3, boolean lock, boolean bro) {

        ResponseCommanderPropertyStonePacket response = new ResponseCommanderPropertyStonePacket();

        response.setGuid(user.getGuid());
        response.getName().value(user.getUsername());
        response.setBroFlag(bro ? 1 : 0);
        response.setType(type);

        response.setLockFlag(lock ? 1 : 0);
        response.setObjStoneId(obj);
        response.setSrcStoneId1(src1);
        response.setSrcStoneId2(src2);
        response.setSrcStoneId3(src3);

        return response;

    }

    public ResponseCommanderInsertCmosPacket getCommanderInsertCmosPacket(int cmosType, int commanderId, int holeId, int cmosId) {

        ResponseCommanderInsertCmosPacket response = new ResponseCommanderInsertCmosPacket();

        response.setKind(cmosType);
        response.setCommanderId(commanderId);
        response.setHoleId(holeId);
        response.setCmosId(cmosId);

        return response;

    }

    public ResponseCommanderInsertStonePacket getCommanderInsertStonePacket(int gemType, int commanderId, int holeId, int propsId, int lockFlag) {

        ResponseCommanderInsertStonePacket response = new ResponseCommanderInsertStonePacket();

        response.setGemType(gemType);
        response.setCommanderId(commanderId);
        response.setHoleId(holeId);
        response.setPropsId(propsId);
        response.setLockFlag(lockFlag);

        return response;

    }

    public ResponseResumeCommanderPacket getResumeCommanderPacket(Prop revivalCard, Commander commander) {

        ResponseResumeCommanderPacket response = new ResponseResumeCommanderPacket();

        commander.setUntilRest(null);
        commander.save();

        response.setCommanderId(commander.getCommanderId());
        response.setPropsId(revivalCard.getPropId());
        response.setLockFlag(revivalCard.getPropLockNum());

        return response;

    }

    public ResponseReliveCommanderPacket getReliveCommanderPacket(Prop revivalCard, Commander commander) {

        ResponseReliveCommanderPacket response = new ResponseReliveCommanderPacket();

        commander.setDead(false);
        commander.setUntilRest(null);

        commander.save();

        response.setCommanderId(commander.getCommanderId());
        response.setPropsId(revivalCard.getPropId());
        response.setLockFlag(revivalCard.getPropLockNum());

        return response;

    }

    public ResponseClearCommanderPercentPacket getResetCommanderPacket(boolean lockCard, Commander commander) {

        ResponseClearCommanderPercentPacket response = new ResponseClearCommanderPercentPacket();

        commander.reset();
        commander.save();

        CommanderLevel level = CommanderService.getInstance().getLevel(commander);
        CommanderAttributes attributes = commander.getBaseAttributes();

        response.setCommanderId(commander.getCommanderId());
        response.setLevel(level.getLevel());
        response.setExp(level.getLevelExperience());

        response.setAim((short) attributes.getAim());
        response.setBlench((short) attributes.getDodge());
        response.setPriority((short) attributes.getSpeed());
        response.setElectron((short) attributes.getElectron());

        response.setLockFlag(lockCard ? 1 : 0);

        response.setAimPer((byte) commander.getGrowthAim());
        response.setBlenchPer((byte) commander.getGrowthDodge());
        response.setPriorityPer((byte) commander.getGrowthSpeed());
        response.setElectronPer((byte) commander.getGrowthElectron());

        return response;

    }

    public ResponseDeleteCommanderPacket getDeleteCommanderPacket(Commander commander) {

        commanderCache.delete(commander);

        ResponseDeleteCommanderPacket packet = new ResponseDeleteCommanderPacket();

        packet.setCommanderId(commander.getCommanderId());

        return packet;

    }

    public List<ResponseCommanderBaseInfoPacket> getBaseInfoPacket(User user) {

        List<ResponseCommanderBaseInfoPacket> response = new ArrayList<>();
        List<Commander> commanders = user.getCommanders();

        ResponseCommanderBaseInfoPacket packet = null;

        for (Commander commander : commanders) {

            if (packet == null) {

                packet = new ResponseCommanderBaseInfoPacket();
                packet.setNextInviteTime(user.getNextRecruit());
                packet.setReserve(0);

            }

            packet.getCommanderBaseInfos().add(getCommanderBaseInfo(commander));
            packet.setDataLen(packet.getCommanderBaseInfos().size());

            if (packet.getCommanderBaseInfos().size() == 18) {

                response.add(packet);
                packet = null;
            }
        }

        if (packet != null && !response.contains(packet)) {
            response.add(packet);
        }

        return response;

    }

    public ResponseRefreshCommanderBaseInfoPacket getRefreshBaseInfoPacket(User user) {

        List<Commander> commanders = user.getCommanders();
        ResponseRefreshCommanderBaseInfoPacket packet = new ResponseRefreshCommanderBaseInfoPacket();

        for (Commander commander : commanders) {
            packet.getCommanderBaseInfos().add(getRefreshCommanderBaseInfo(commander));
            packet.setDataLen(packet.getCommanderBaseInfos().size());

            if (packet.getCommanderBaseInfos().size() == MAX_COMMANDER_NUM) {
                break;
            }
        }

        return packet;

    }

    public void sendInfoPacket(int commanderId, int showType, SmartServer smartServer, User user) {

        Set<Integer> modelIds = new HashSet<>();
        List<Commander> commanders = user.getCommanders();

        for (Commander commander : commanders) {
            if (commander.getCommanderId() == commanderId) {

                List<Commander> statuses = new ArrayList<>(commanders);
                statuses.remove(commander);

                ResponseCommanderInfoPacket response = new ResponseCommanderInfoPacket();
                CommanderLevel level = getLevel(commander);
                CommanderAttributes baseAttributes = getBaseAttributes(commander);

                Fleet fleet = commander.getFleet();

                response.setLevel((byte) level.getLevel());
                response.setCommanderId(commanderId);
                response.setShipTeamId(fleet == null ? -1 : fleet.getShipTeamId());
                response.setCardType((byte) commander.getType());
                response.setSkill((short) commander.getSkill());
                response.setCardLevel((short) commander.getStars());
                response.setShowType((byte) showType);
                response.setState((byte) commander.getState());

                if (fleet != null) {

                    response.setTarget((byte) fleet.getPreferenceType());
                    response.setTargetInterval((byte) fleet.getRangeType());

                    response.setTeamBody(fleet.getFleetBody().getCells());

                    for (ShipTeamNum shipTeamNum : fleet.getFleetBody().getCells()) {
                        if (shipTeamNum.getShipModelId() != -1) {
                            modelIds.add(shipTeamNum.getShipModelId());
                        }
                    }

                    if (modelIds.size() > 0) {
                        smartServer.send(PacketService.getInstance().getShipModels(user, modelIds));
                    }

                }

                response.setAim((short) baseAttributes.getAim());
                response.setElectron((short) baseAttributes.getElectron());
                response.setBlench((short) baseAttributes.getDodge());
                response.setPriority((short) baseAttributes.getSpeed());

                response.setAimPer((byte) commander.getGrowthAim());
                response.setElectronPer((byte) commander.getGrowthElectron());
                response.setBlenchPer((byte) commander.getGrowthDodge());
                response.setPriorityPer((byte) commander.getGrowthSpeed());

                response.setExp(level.getLevelExperience());
                response.setStone(new ShortArray(ListUtil.toArray(commander.getGems())));
                response.setStoneHole(level.getLevelData().getGem());

                response.setCmosExp(new IntegerArray(5, 0));
                response.setCmos(new ShortArray(5, -1));

                for (BionicChip bionicChip : commander.getChips().stream().sorted(Comparator.comparing(BionicChip::getHoleId)).collect(Collectors.toList())) {

                    response.getCmosExp().getArray()[bionicChip.getHoleId()] = bionicChip.getChipExperience();
                    response.getCmos().getArray()[bionicChip.getHoleId()] = (short) bionicChip.getChipId();

                }

                for (Commander status : statuses) {
                    response.getAllStatus().add(getCommanderInfo(status));
                }

                response.setAllStatusLen((byte) response.getAllStatus().size());

                response.getCommanderZJ().value(commander.getExpertise().getJZ());
                smartServer.send(response);
                return;

            }
        }

    }

    public ResponseCommanderStoneInfoPacket getCommanderStoneInfo(Commander commander) {

        ResponseCommanderStoneInfoPacket response = new ResponseCommanderStoneInfoPacket();

        CommanderLevel level = getLevel(commander);
        CommanderAttributes baseAttributes = getBaseAttributes(commander);

        response.getUserName().value(commander.getUser().getUsername());
        response.getCommanderZJ().value(commander.getExpertise().getJZ());

        response.setExp(level.getLevelExperience());
        response.setSkillId(commander.getSkill());
        response.setLevel(level.getLevel());
        response.setCardLevel(commander.getStars());

        response.setAim(baseAttributes.getAim());
        response.setBlench(baseAttributes.getDodge());
        response.setPriority(baseAttributes.getSpeed());
        response.setElectron(baseAttributes.getElectron());

        response.setStoneAim((int) commander.getStoneAim());
        response.setStoneBlench((int) commander.getStoneDodge());
        response.setStoneElectron((int) commander.getStoneElectron());
        response.setStonePriority((int) commander.getStonePriority());

        response.setStoneAssault((int) (commander.getStoneAssault() * 1000));
        response.setStoneEndure((int) (commander.getStoneEndure() * 1000));
        response.setStoneShield((int) (commander.getStoneShield() * 1000));
        response.setStoneBlastHurt((int) (commander.getStoneBlastHurt() * 1000));
        response.setStoneBlast((int) (commander.getStoneBlastHurtRate() * 1000));
        response.setStoneDoubleHit((int) (commander.getStoneDoubleHit() * 1000));
        response.setStoneRepairShield((int) (commander.getStoneRepairShield() * 1000));
        response.setStoneExp((int) (commander.getStoneExp() * 1000));

        response.setAimPer((byte) commander.getGrowthAim());
        response.setBlenchPer((byte) commander.getGrowthDodge());
        response.setPriorityPer((byte) commander.getGrowthSpeed());
        response.setElectronPer((byte) commander.getGrowthElectron());

        response.setCmos(new ShortArray(5, -1));

        for (BionicChip bionicChip : commander.getChips().stream().sorted(Comparator.comparing(BionicChip::getHoleId)).collect(Collectors.toList())) {
            response.getCmos().getArray()[bionicChip.getHoleId()] = (short) bionicChip.getChipId();
        }

        return response;

    }

    public ResponseCreateCommanderPacket getCreateCommander(Commander commander) {

        ResponseCreateCommanderPacket response = new ResponseCreateCommanderPacket();

        response.setNextInviteTime(commander.getUser().getNextRecruit());
        response.setCommanderBaseInfo(getCommanderBaseInfo(commander));

        return response;

    }

    public RefreshCommanderBaseInfo getRefreshCommanderBaseInfo(Commander commander) {

        RefreshCommanderBaseInfo baseInfo = new RefreshCommanderBaseInfo();

        CommanderAttributes baseAttributes = getBaseAttributes(commander);
        CommanderLevel level = getLevel(commander);

        baseInfo.setCommanderId(commander.getCommanderId());
        baseInfo.setExp(level.getLevelExperience());

        baseInfo.setAim(baseAttributes.getAim());
        baseInfo.setBlench(baseAttributes.getDodge());
        baseInfo.setPriority(baseAttributes.getSpeed());
        baseInfo.setElectron(baseAttributes.getElectron());
        baseInfo.setLevel((byte) level.getLevel());

        return baseInfo;

    }

    public CommanderBaseInfo getCommanderBaseInfo(Commander commander) {

        CommanderBaseInfo baseInfo = new CommanderBaseInfo();
        CommanderLevel level = getLevel(commander);

        baseInfo.getName().value(commander.getName());
        baseInfo.setUserId(commander.getUserId());
        baseInfo.setCommanderId(commander.getCommanderId());
        baseInfo.setShipTeamId(-1);
        baseInfo.setState(commander.getState());
        baseInfo.setSkill((short) commander.getSkill());
        baseInfo.setLevel((byte) level.getLevel());
        baseInfo.setType((byte) commander.getType());

        return baseInfo;

    }

    public CommanderInfo getCommanderInfo(Commander commander) {

        CommanderInfo info = new CommanderInfo();

        info.setCommanderId(commander.getCommanderId());
        info.setState(commander.getState());

        return info;

    }

    public CommanderStatsData getStats(Commander commander) {

        return getStats(commander.getSkill());
    }

    public CommanderStatsData getStats(int skillId) {

        if (skillId == -1) {
            return null;
        }
        return ResourceManager.getCommanders().getCommander().get(skillId);
    }

    public CommanderLevel getLevel(Commander commander) {

        if (commander.isPirate()) {
            return new CommanderLevel(CommanderLevelsData.builder()
                .exp(0)
                .gem(12)
                .build(), commander.getInheritedLevel(), 0);
        }
        return getLevel(commander.getExperience());
    }

    public CommanderLevel getLevel(int experience) {

        List<CommanderLevelsData> levels = ResourceManager.getCommanders().getLevels();
        int level = 0;
        int total = 0;

        if (experience >= MAXIMUM_EXPERIENCE) {
            return new CommanderLevel(levels.get(levels.size() - 1), 49, 0);
        }

        for (CommanderLevelsData data : levels) {

            level++;
            total += data.getExp();

            if (experience < total) {
                int real = (data.getExp() + experience) - total;
                return new CommanderLevel(data, level - 1, real);
            }

        }

        return new CommanderLevel(ResourceManager.getCommanders().getLevels().get(0), level, 0);

    }

    public void randomizeVariance(Commander commander) {
        int variance = MathUtil.random(-30, 32);
        variance = Math.min(Math.max(variance, -MAX_VARIANCE), MAX_VARIANCE);
        commander.setVariance(variance);
    }

    public void randomizeGrowth(Commander commander) {

        int aim = MathUtil.random(0, 51);
        int electron = MathUtil.random(0, 51);

        int dodge = 0;
        int speed = 0;

        if (MathUtil.random()) {

            dodge = 50 - aim;
            speed = 50 - electron;

        } else {

            dodge = 50 - electron;
            speed = 50 - aim;

        }

        commander.setGrowthAim(aim);
        commander.setGrowthElectron(electron);
        commander.setGrowthDodge(dodge);
        commander.setGrowthSpeed(speed);

    }

    public CommanderTrigger createTrigger(BattleCommander battleCommander) {

        if (battleCommander.getSkillId() == -1) {
            return new CommanderTrigger();
        }

        CommanderTrigger commanderTrigger = new CommanderTrigger();

        CommanderStatsData statsData = battleCommander.getStatsData();
        CommanderProcMeta procMeta = statsData.getProc();

        double integral = 0.d, rate;
        for (String stat : procMeta.getStat()) {
            switch (stat) {
                case "accuracy":
                    integral += battleCommander.getTotalAccuracy();
                    break;
                case "dodge":
                    integral += battleCommander.getTotalDodge();
                    break;
                case "speed":
                    integral += battleCommander.getTotalSpeed();
                    break;
                case "electron":
                    integral += battleCommander.getTotalElectron();
                    break;
            }
        }

        double procA = procMeta.getCurve()[0];
        double procB = procMeta.getCurve()[1];
        double procC = procMeta.getCurve()[2];
        double procD = procMeta.getCurve()[3];

        rate = procA + (procB * integral) + (procC * Math.pow(integral, 2)) + (procD * Math.pow(integral, 3));

        commanderTrigger.setAccuracy(battleCommander.getTotalAccuracy());
        commanderTrigger.setDodge(battleCommander.getTotalDodge());
        commanderTrigger.setSpeed(battleCommander.getTotalSpeed());
        commanderTrigger.setElectron(battleCommander.getTotalElectron());

        commanderTrigger.setProcA(procA);
        commanderTrigger.setProcB(procB);
        commanderTrigger.setProcC(procC);
        commanderTrigger.setProcD(procD);

        double result = Math.round(rate) / 100.0d;

        commanderTrigger.setRate(result);
        return commanderTrigger;

    }

    public CommanderTrigger createTrigger(BattleCommander battleCommander, BattleCommander copiedCommander) {

        if (battleCommander.getSkillId() == -1) {
            return new CommanderTrigger();
        }

        CommanderTrigger commanderTrigger = new CommanderTrigger();
        CommanderStatsData statsData = copiedCommander.getStatsData();
        if (statsData == null) {
            return new CommanderTrigger();
        }

        CommanderProcMeta procMeta = statsData.getProc();

        double integral = 0.d, rate;
        for (String stat : procMeta.getStat()) {
            switch (stat) {
                case "accuracy":
                    integral += battleCommander.getTotalAccuracy();
                    break;
                case "dodge":
                    integral += battleCommander.getTotalDodge();
                    break;
                case "speed":
                    integral += battleCommander.getTotalSpeed();
                    break;
                case "electron":
                    integral += battleCommander.getTotalElectron();
                    break;
            }
        }

        double procA = procMeta.getCurve()[0];
        double procB = procMeta.getCurve()[1];
        double procC = procMeta.getCurve()[2];
        double procD = procMeta.getCurve()[3];

        rate = procA + (procB * integral) + (procC * Math.pow(integral, 2)) + (procD * Math.pow(integral, 3));

        commanderTrigger.setAccuracy(battleCommander.getTotalAccuracy());
        commanderTrigger.setDodge(battleCommander.getTotalDodge());
        commanderTrigger.setSpeed(battleCommander.getTotalSpeed());
        commanderTrigger.setElectron(battleCommander.getTotalElectron());

        commanderTrigger.setProcA(procA);
        commanderTrigger.setProcB(procB);
        commanderTrigger.setProcC(procC);
        commanderTrigger.setProcD(procD);

        double result = Math.round(rate) / 100.0d;
        commanderTrigger.setRate(result);
        return commanderTrigger;

    }

    public Commander basic(int skill, long userId) {

        return basic(skill, 0, 0, userId);
    }

    public Commander basic(int skill, int stars, int experience, long userId) {

        CommanderStatsData statsData = CommanderService.getInstance().getStats(skill);
        if (statsData == null) {
            return common(userId);
        }

        Commander commander = new Commander();

        commander.setName(statsData.getName());
        commander.setCommanderId(AutoIncrementService.getInstance().getNextCommanderId());
        commander.setUserId(userId);
        commander.setSkill(skill);
        commander.setStars(stars);
        commander.setExperience(experience);

        commander.randomizeGrowth();
        commander.randomizeVariance();

        commander.setGems(Arrays.asList(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));

        return commander;

    }

    public Commander common(long userId) {

        Commander commander = new Commander();

        commander.setName("Commander");
        commander.setCommanderId(AutoIncrementService.getInstance().getNextCommanderId());
        commander.setUserId(userId);
        commander.setSkill(-1);
        commander.setStars(-1);
        commander.setExperience(0);

        commander.setCommonBaseAim(MathUtil.random(0, 11));
        commander.setCommonBaseElectron(MathUtil.random(0, 11));
        commander.setCommonBaseDodge(MathUtil.random(0, 11));
        commander.setCommonBaseSpeed(MathUtil.random(0, 11));
        commander.setCommonExpertise(CommanderExpertise.common());

        commander.randomizeGrowth();
        commander.randomizeVariance();

        commander.setGems(Arrays.asList(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));
        return commander;

    }

    public CommanderAttributes getBaseAttributes(Commander commander) {

        int stars = commander.isCommon() ? 0 : commander.getStars();

        int aim = (int) (getGrowth(commander, commander.getGrowthAim()) + commander.getBaseAim() + stars);
        int electron = (int) (getGrowth(commander, commander.getGrowthElectron()) + commander.getBaseElectron() + stars);
        int dodge = (int) (getGrowth(commander, commander.getGrowthDodge()) + commander.getBaseDodge() + stars);
        int speed = (int) (getGrowth(commander, commander.getGrowthSpeed()) + commander.getBaseSpeed() + stars);

        return new CommanderAttributes(aim, electron, dodge, speed);

    }

    public double getGrowth(Commander commander, double rate) {

        CommanderPullulateData pullulate = getCommanderPullulate(commander);
        double gain = getGain(pullulate);

        if (commander.isTitan()) {
            gain += 5;
        }

        double variance = (commander.getVariance() - 0.025) / 200;
        return Math.floor(rate / 100 * (gain + 0.5 + variance) * (double) getLevel(commander).getLevel());

    }

    public int getGain(CommanderPullulateData pullulate) {

        return pullulate.getMinPullulate();
    }

    public int getPropId(Commander commander) {

        List<PropData> props = ResourceManager.getProps().getCommanders();

        for (PropData data : props) {
            if (data.hasCommanderData()) {
                if (data.getCommanderData().getCommander().getId() == commander.getSkill()) {
                    return data.getId() + commander.getStars();
                }
            }
        }

        return -1;

    }

    public List<PropData> getCommanderPropData(String type) {

        List<PropData> props = ResourceManager.getProps().getCommanders();
        return props.stream()
            .filter(propData -> propData.hasCommanderData() && propData.getCommanderData().getCommander().getType().equals(type))
            .collect(Collectors.toList());

    }

    public PropData getCommanderPropData(int propId) {

        List<PropData> props = ResourceManager.getProps().getCommanders();

        for (PropData data : props) {
            if (data.hasCommanderData()) {
                if ((data.getId() + 8) >= propId && data.getId() <= propId) {
                    return data;
                }
            }
        }

        return null;

    }

    public CommanderPullulateData getCommanderPullulate(Commander commander) {

        return getCommanderPullulate(commander.isCommon() ? 0 : CommanderService.getInstance().getStats(commander).typeCode(), commander.getStars());
    }

    public CommanderPullulateData getCommanderPullulate(int commanderType, int stars) {

        for (CommanderPullulateData data : getPullulateData()) {
            if (data.getCommandType() == commanderType && data.getCommandStar() == stars) {
                return data;
            }
        }

        return getPullulateData().get(0);

    }

    public List<CommanderPullulateData> getPullulateData() {

        return ResourceManager.getCommanders().getPullulates();
    }

    public Commander getCommander(int commanderId) {

        return commanderCache.findByCommanderId(commanderId);
    }

    public List<Commander> getCommanders(User user) {

        List<Commander> commanders = commanderCache.findByUserId(user.getPlanet().getUserId());

        if (commanders == null) {
            return new ArrayList<>();
        }

        return commanders;

    }

    public boolean validateGemEX(Gem objGem, Gem srcGem1, Gem srcGem2, Gem srcGem3) {

        if (srcGem1 != null && srcGem2 != null && srcGem3 != null) {
            //triad

            //lv must match
            if (srcGem1.level != srcGem2.level || srcGem2.level != srcGem3.level || srcGem3.level != objGem.level) {
                return false;
            }

            int add = srcGem1.type + srcGem2.type + srcGem3.type;
            int mul = srcGem1.type * srcGem2.type * srcGem3.type;

            //wtf
            switch (objGem.type) {
                case 21:
                    return add == (5 + 6 + 10) && mul == (5 * 6 * 10);
                case 22:
                    return add == (6 + 7 + 11) && mul == (6 * 7 * 11);
                case 23:
                    return add == (8 + 9 + 10) && mul == (8 * 9 * 10);
                default:
                    return false;
            }

        } else if (srcGem1 != null && srcGem2 != null) {
            //quadra

            //color must match
            if (srcGem1.color != srcGem2.color || srcGem2.color != objGem.color) {
                return false;
            }

            //lv must equal the average lv of the two gems, rounded down
            if ((srcGem1.level + srcGem2.level) / 2 != objGem.level) {
                return false;
            }

            //order gems lowest to highest
            if (srcGem1.type > srcGem2.type) {
                Gem srcGemSwap = srcGem2;
                srcGem2 = srcGem1;
                srcGem1 = srcGemSwap;
            }

            //lower gem must be one of the basic stat gems
            if (srcGem1.type < 1 || srcGem1.type > 4) {
                return false;
            }
            //higher gem must be a triad
            if (srcGem2.type < 21 || srcGem2.type > 23) {
                return false;
            }

            return 23 + srcGem1.type == objGem.type;

        }

        return false;

    }

    public String getCommonExpertisePattern() {

        return COMMON_EXPERTISE_PATTERN;
    }

    public int getMaximumExperience() {

        return MAXIMUM_EXPERIENCE;
    }

    public static CommanderService getInstance() {

        return instance;
    }

}
