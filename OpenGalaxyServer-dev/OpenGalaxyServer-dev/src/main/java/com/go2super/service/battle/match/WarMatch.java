package com.go2super.service.battle.match;

import com.go2super.database.entity.*;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.game.FightRobResource;
import com.go2super.obj.game.FightTotalKill;
import com.go2super.obj.game.JumpShipTeamInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.JumpType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.packet.PacketRouter;
import com.go2super.packet.corp.ResponseConsortiaPirateBroPacket;
import com.go2super.packet.fight.ResponseFightGalaxyOverPacket;
import com.go2super.packet.fight.ResponseFightResultPacket;
import com.go2super.packet.fight.ResponseFightResultPacket2;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.packet.ship.ResponseDeleteShipTeamBroadcastPacket;
import com.go2super.packet.ship.ResponseJumpShipTeamPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.InstanceData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.meta.RewardMeta;
import com.go2super.service.*;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.type.AttackSideType;
import com.go2super.service.battle.type.BattleResultType;
import com.go2super.service.battle.type.StopCause;
import com.go2super.service.jobs.other.DefendJob;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.go2super.obj.type.MailType.BATTLE_REWARD;

@Getter
@Setter
@ToString(callSuper = true)
public class WarMatch extends Match {

    private List<Integer> sourceIds = new ArrayList<>();
    private List<Integer> targetIds = new ArrayList<>();

    private Planet targetPlanet;

    private int sourceSend = 0;
    private int targetSend = 0;

    private List<BattleTag> tags = new ArrayList<>();
    private List<BattleRound> rounds = new ArrayList<>();

    private List<BattleFleet> removedFleets = new ArrayList<>();
    private List<BattleFort> removedForts = new ArrayList<>();

    public WarMatch(Planet planet) {

        this.targetPlanet = planet;

    }

    public boolean start() {

        if (targetPlanet != null && targetPlanet.getType() != PlanetType.USER_PLANET) {

            this.getPause().set(false);
            this.setMaxRound(100);
            return true;

        }

        int maxRound = 20 + (getFleets().size());
        maxRound = maxRound > 100 ? 100 : maxRound;

        this.getPause().set(false);
        this.setMaxRound(maxRound);
        return true;

    }

    @Override
    public void stop(StopCause cause) {

        if (cause == StopCause.MANUAL) {

            returnAllFleets();
            return;

        }

        BattleMetadata metadata = getMetadata();

        BattleResultType result = getResultType();
        boolean pirates = metadata.getBooleans().containsKey("pirates");

        defender:
        if (result == BattleResultType.LOSE && DefendJob.getInstance().getCurrentFlagGalaxyId() == getGalaxyId()) {

            Future future = DefendJob.getInstance().getFuture();
            if (future == null) {

                DefendJob.getInstance().setRegister(false);
                DefendJob.getInstance().setSelection(false);
                DefendJob.getInstance().setFuture(null);

                DefendJob.getInstance().setCurrentFlagCorp(-1);
                DefendJob.getInstance().setCurrentFlagGalaxyId(-1);
                DefendJob.getInstance().setCurrentFlag(-1);

                DefendJob.getInstance().getParticipants().clear();
                break defender;

            }

            future.cancel(true);

            Set<Integer> awarded = new HashSet<>();

            for (BattleFleet battleFleet : getFleets()) {

                if (battleFleet.getGuid() == -1 || awarded.contains(battleFleet.getGuid())) {
                    continue;
                }
                if (battleFleet.isDefender()) {
                    continue;
                }

                User user = UserService.getInstance().getUserCache().findByGuid(battleFleet.getGuid());
                if (user == null) {
                    continue;
                }

                if (user.getConsortiaId() == DefendJob.getInstance().getCurrentFlagCorp()) {
                    continue;
                }

                UserEmailStorage userEmailStorage = user.getUserEmailStorage();
                Email email = Email.builder()
                        .autoId(userEmailStorage.nextAutoId())
                        .type(2)
                        .name("System")
                        .subject("Base Defender Rewards")
                        .emailContent(
                                "Here are the goods for our successful attack!")
                        .readFlag(0)
                        .date(DateUtil.now())
                        .goods(new ArrayList<>())
                        .guid(-1)
                        .build();

                email.addGood(EmailGood.builder()
                        .goodId(935)
                        .lockNum(500)
                        .build());

                userEmailStorage.addEmail(email);

                user.update();
                user.save();

                awarded.add(user.getGuid());

                Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(user);

                if (gameUserOptional.isPresent()) {

                    LoggedGameUser loggedGameUser = gameUserOptional.get();
                    ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                            .errorCode(0)
                            .build();

                    loggedGameUser.getSmartServer().send(response);

                }

            }

            DefendJob.getInstance().setRegister(false);
            DefendJob.getInstance().setSelection(false);
            DefendJob.getInstance().setFuture(null);

            DefendJob.getInstance().setCurrentFlagCorp(-1);
            DefendJob.getInstance().setCurrentFlagGalaxyId(-1);
            DefendJob.getInstance().setCurrentFlag(-1);

            DefendJob.getInstance().getParticipants().clear();

        }

        if (result == BattleResultType.WIN) {

            if (pirates) {
                ResponseConsortiaPirateBroPacket defeatPacket = new ResponseConsortiaPirateBroPacket();

                int pirateLevel = metadata.getIntegers().get("level");
                int corpId = metadata.getIntegers().get("consortia_id");
                String corpName = metadata.getStrings().get("consortia_name");

                defeatPacket.setFlag(0);
                defeatPacket.setGalaxyId(getGalaxyId());
                defeatPacket.setPirateLevelId(pirateLevel);
                defeatPacket.setConsortiaId(corpId);
                defeatPacket.getConsortiaName().value(corpName);

                PacketRouter.getInstance().broadcast(defeatPacket);

                InstanceData instanceData = ResourceManager.getPirates().getPirate(pirateLevel + 1);
                Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(corpId);

                if (corp.getPiratesLevel() < instanceData.getId()) {
                    corp.setPiratesLevel(instanceData.getId());
                    corp.save();
                }

                if (corp != null && instanceData != null) {

                    List<Pair<PropData, Integer>> rewards = new ArrayList<>();

                    for (RewardMeta rewardMeta : instanceData.getRewards()) {
                        if (rewardMeta.getWeight() >= 100) {

                            PropData defResult = ResourceManager.getProps().getData(rewardMeta.getProp());
                            if (defResult == null) {
                                continue;
                            }

                            rewards.add(Pair.of(defResult, rewardMeta.getNum()));

                        } else {

                            int random = MathUtil.randomInclusive(1, 100);
                            if (random <= rewardMeta.getWeight()) {

                                PropData defResult = ResourceManager.getProps().getData(rewardMeta.getProp());
                                if (defResult == null) {
                                    continue;
                                }

                                rewards.add(Pair.of(defResult, rewardMeta.getNum()));

                            }

                        }
                    }

                    for (CorpMember member : corp.getMembers().getMembers()) {

                        Optional<User> optionalUser = member.getUser();
                        if (optionalUser.isPresent()) {

                            User user = optionalUser.get();
                            if (user.isPirateReceived()) {
                                continue;
                            }
                            UserEmailStorage userEmailStorage = user.getUserEmailStorage();

                            Email email = Email.builder()
                                    .autoId(userEmailStorage.nextAutoId())
                                    .name("System")
                                    .readFlag(0)
                                    .date(DateUtil.now())
                                    .goods(new ArrayList<>())
                                    .guid(-1)
                                    .build();

                            email.setSubject("Pirates Rewards");
                            email.setEmailContent("Your corp completed the " + instanceData.getName() + " and you earned the following rewards:");

                            email.setType(2);

                            for (Pair<PropData, Integer> reward : rewards) {
                                email.addGood(EmailGood.builder()
                                        .goodId(reward.getKey().getId())
                                        .lockNum(reward.getValue())
                                        .build());
                            }

                            userEmailStorage.addEmail(email);

                            ResponseNewEmailNoticePacket packet = ResponseNewEmailNoticePacket.builder()
                                    .errorCode(0)
                                    .build();
                            user.setPirateReceived(true);
                            user.update();
                            user.save();

                            Optional<LoggedGameUser> onlineUser = user.getLoggedGameUser();
                            if (onlineUser.isPresent()) {
                                onlineUser.get().getSmartServer().send(packet);
                            }

                        }

                    }

                }

            }

        }

        ResponseFightResultPacket fightResultPacket = BattleService.getInstance().getWarFightResult(this, result);
        ResponseFightResultPacket2 emailFightResultPacket = ResponseFightResultPacket2.from(fightResultPacket);

        Planet fromPlanet = getTargetPlanet();
        if (fromPlanet.getType() == PlanetType.USER_PLANET) {

            UserPlanet userPlanet = (UserPlanet) fromPlanet;
            Optional<User> optionalUser = userPlanet.getUser();

            if (optionalUser.isPresent()) {

                BattleReport battleReport = this.getBattleReport();

                User destroyed = optionalUser.get();
                UserResources resources = destroyed.getResources();

                double gold = 0.0d;
                double metal = 0.0d;
                double he3 = 0.0d;

                if (result == BattleResultType.LOSE) {
                    gold = Math.max(resources.getGold() * 0.2, 0);
                    metal = Math.max(resources.getMetal() * 0.2, 0);
                    he3 = Math.max(resources.getHe3() * 0.2, 0);

                    resources.setGold((long) (resources.getGold() - gold));
                    resources.setMetal((long) (resources.getMetal() - metal));
                    resources.setHe3((long) (resources.getHe3() - he3));
                }

                if (destroyed.getLoggedGameUser().isPresent()) {
                    destroyed.getLoggedGameUser().get().getSmartServer().send(ResourcesService.getInstance().getPlayerResourcePacket(destroyed));
                }

                Map<Integer, Integer> shootdownsOverall = new HashMap<>();

                int totalShootdowns = 0;

                for (BattleShipCache shipCache : battleReport.getShipHistoric()) {
                    totalShootdowns += shipCache.getShootdowns();

                    if (shootdownsOverall.containsKey(shipCache.getGuid())) {
                        shootdownsOverall.put(shipCache.getGuid(), (int) (shootdownsOverall.get(shipCache.getGuid()) + shipCache.getShootdowns()));
                    } else {
                        shootdownsOverall.put(shipCache.getGuid(), (int) shipCache.getShootdowns());
                    }

                }

                int index = 0;
                shootdownsOverall = shootdownsOverall.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

                User topOneUser = null;
                Boolean topOneAttacker = null;
                var storageMail = destroyed.getUserEmailStorage();
                Email mail = Email.builder()
                        .autoId(storageMail.nextAutoId())
                        .type(1)
                        .name("System")
                        .subject("")
                        .emailContent("")
                        .readFlag(0)
                        .fightGalaxyId(getGalaxyId())
                        .date(DateUtil.now())
                        .goods(new ArrayList<>())
                        .fightResultPacket(emailFightResultPacket)
                        .guid(-1)
                        .build();
                storageMail.addEmail(mail);
                ResponseNewEmailNoticePacket notification = ResponseNewEmailNoticePacket.builder()
                        .errorCode(0)
                        .build();

                Optional<LoggedGameUser> destroyedOnline = destroyed.getLoggedGameUser();
                destroyedOnline.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(notification));
                for (Map.Entry<Integer, Integer> entry : shootdownsOverall.entrySet()) {

                    User user = UserService.getInstance().getUserCache().findByGuid(entry.getKey());
                    if (user == null) {
                        continue;
                    }

                    user.getStats().setKills(user.getStats().getKills() + entry.getValue());

                    boolean attacker = getAllFleets().stream().anyMatch(fleet -> fleet.getGuid() == user.getGuid() && fleet.isAttacker());
                    if (topOneAttacker == null) {
                        topOneUser = user;
                        topOneAttacker = attacker;
                    }

                    Optional<LoggedGameUser> onlineUser = user.getLoggedGameUser();
                    UserEmailStorage userEmailStorage = user.getUserEmailStorage();

                    if (result == BattleResultType.LOSE && attacker) {
                        index++;
                        Email battleMail = Email.builder()
                                .autoId(userEmailStorage.nextAutoId())
                                .type(1)
                                .name("System")
                                .subject("")
                                .emailContent("")
                                .readFlag(0)
                                .fightGalaxyId(getGalaxyId())
                                .date(DateUtil.now())
                                .goods(new ArrayList<>())
                                .fightResultPacket(emailFightResultPacket)
                                .guid(1)
                                .build();
                        userEmailStorage.addEmail(battleMail);

                        if (index < 10) {
                            // only send rewards to top 10 attackers
                            Email resMail = Email.builder()
                                    .autoId(userEmailStorage.nextAutoId())
                                    .type(4)
                                    .name("System")
                                    .subject("")
                                    .emailContent("")
                                    .readFlag(0)
                                    .fightGalaxyId(getGalaxyId())
                                    .mailType(BATTLE_REWARD)
                                    .date(DateUtil.now())
                                    .goods(new ArrayList<>())
                                    .guid(-1)
                                    .build();
                            double percentage = ((double) entry.getValue() / (double) totalShootdowns);
                            double goldProfit = Math.max(Math.min(percentage * gold, UserResources.MAX_RESOURCES), 0);
                            double metalProfit = Math.max(Math.min(percentage * metal, UserResources.MAX_RESOURCES), 0);
                            double he3Profit = Math.max(Math.min(percentage * he3, UserResources.MAX_RESOURCES), 0);
                            resMail.addGood(EmailGood.builder()
                                    .goodId(0)
                                    .num((int) goldProfit)
                                    .build());
                            resMail.addGood(EmailGood.builder()
                                    .goodId(2)
                                    .num((int) metalProfit)
                                    .build());
                            resMail.addGood(EmailGood.builder()
                                    .goodId(3)
                                    .num((int) he3Profit)
                                    .build());
                            getBattleReport().getFightRobResources().add(
                                    new FightRobResource(user.getUserId(), 0, (int) metalProfit, (int) he3Profit, (int) goldProfit, user.getUsername()));
                            userEmailStorage.addEmail(resMail);
                        }
                    } else if (result == BattleResultType.LOSE) {
                        Email battleMail = Email.builder()
                                .autoId(userEmailStorage.nextAutoId())
                                .autoId(userEmailStorage.nextAutoId())
                                .type(1)
                                .name("System")
                                .subject("")
                                .emailContent("")
                                .readFlag(0)
                                .fightGalaxyId(getGalaxyId())
                                .date(DateUtil.now())
                                .goods(new ArrayList<>())
                                .fightResultPacket(emailFightResultPacket)
                                .guid(-1)
                                .build();
                        userEmailStorage.addEmail(battleMail);
                    } else if (result == BattleResultType.WIN && attacker) {
                        Email battleMail = Email.builder()
                                .autoId(userEmailStorage.nextAutoId())
                                .autoId(userEmailStorage.nextAutoId())
                                .type(1)
                                .name("System")
                                .subject("")
                                .emailContent("")
                                .readFlag(0)
                                .fightGalaxyId(getGalaxyId())
                                .date(DateUtil.now())
                                .goods(new ArrayList<>())
                                .fightResultPacket(emailFightResultPacket)
                                .guid(-1)
                                .build();
                        userEmailStorage.addEmail(battleMail);
                    } else if (result == BattleResultType.WIN) {
                        Email battleMail = Email.builder()
                                .autoId(userEmailStorage.nextAutoId())
                                .autoId(userEmailStorage.nextAutoId())
                                .type(1)
                                .name("System")
                                .subject("")
                                .emailContent("")
                                .readFlag(0)
                                .fightGalaxyId(getGalaxyId())
                                .date(DateUtil.now())
                                .goods(new ArrayList<>())
                                .fightResultPacket(emailFightResultPacket)
                                .guid(1)
                                .build();
                        userEmailStorage.addEmail(battleMail);
                    } else if (result == BattleResultType.DRAW) {
                        Email battleMail = Email.builder()
                                .autoId(userEmailStorage.nextAutoId())
                                .autoId(userEmailStorage.nextAutoId())
                                .type(1)
                                .name("System")
                                .subject("")
                                .emailContent("")
                                .readFlag(0)
                                .fightGalaxyId(getGalaxyId())
                                .date(DateUtil.now())
                                .goods(new ArrayList<>())
                                .fightResultPacket(emailFightResultPacket)
                                .guid(2)
                                .build();
                        userEmailStorage.addEmail(battleMail);
                    }

                    ResponseNewEmailNoticePacket packet = ResponseNewEmailNoticePacket.builder()
                            .errorCode(0)
                            .build();
                    onlineUser.ifPresent(online -> online.getSmartServer().send(packet));
                    user.update();
                    user.save();

                }

                if (topOneAttacker != null && topOneUser != null) {
                    if (topOneAttacker && result == BattleResultType.LOSE) {

                        flag:
                        if (battleReport.getLastShoot() != -1) {

                            User user = UserService.getInstance().getUserCache().findByGuid(battleReport.getLastShoot());
                            if (user == null) {
                                break flag;
                            }

                            Corp corp = user.getCorp();
                            if (corp == null) {
                                break flag;
                            }

                            UserFlag userFlag = UserFlag.builder()
                                    .corpId(corp.getCorpId())
                                    .from(DateUtil.now())
                                    .until(DateUtil.now(259200))
                                    .build();

                            destroyed.setFlag(userFlag);

                        }

                        Map<Integer, User> cacheUsers = new HashMap<>();

                        Corp topCorp = topOneUser.getCorp();
                        Corp destroyedCorp = destroyed.getCorp();

                        if (topCorp != null && destroyedCorp != null) {

                            for (BattleFleet cacheFleet : getFleets()) {

                                if (cacheFleet.getGuid() == -1) {
                                    continue;
                                }
                                if (cacheFleet.isDefender()) {
                                    continue;
                                }

                                if (!cacheUsers.containsKey(cacheFleet.getGuid())) {
                                    cacheUsers.put(cacheFleet.getGuid(), UserService.getInstance().getUserCache().findByGuid(cacheFleet.getGuid()));
                                }

                                User cacheUser = cacheUsers.get(cacheFleet.getGuid());
                                if (cacheUser == null) {
                                    continue;
                                }
                                if (cacheUser.getFlag() == null) {
                                    continue;
                                }
                                if (cacheUser.getFlag().getCorpId() != destroyedCorp.getCorpId()) {
                                    continue;
                                }
                                if (cacheUser.getCorp().getCorpId() != topCorp.getCorpId()) {
                                    continue;
                                }

                                cacheUser.setFlag(null);
                                cacheUser.save();

                            }

                        }

                    } else if (topOneUser.getFlag() != null) {

                        Corp destroyedCorp = destroyed.getCorp();
                        if (destroyedCorp != null && destroyedCorp.getCorpId() == topOneUser.getFlag().getCorpId()) {
                            topOneUser.setFlag(null);
                            topOneUser.save();
                        }

                    }

                }

                destroyed.update();
                destroyed.save();

            }
        }


        if (fromPlanet.getType() == PlanetType.RESOURCES_PLANET) {
            Map<Integer, Integer> shootdownsOverall = new HashMap<>();
            for (BattleShipCache shipCache : this.getBattleReport().getShipHistoric()) {
                if (shootdownsOverall.containsKey(shipCache.getGuid())) {
                    shootdownsOverall.put(shipCache.getGuid(), (int) (shootdownsOverall.get(shipCache.getGuid()) + shipCache.getShootdowns()));
                } else {
                    shootdownsOverall.put(shipCache.getGuid(), (int) shipCache.getShootdowns());
                }

            }
            for (Map.Entry<Integer, Integer> entry : shootdownsOverall.entrySet()) {
                if (entry.getKey() == -1) {
                    continue;
                }
                User user = UserService.getInstance().getUserCache().findByGuid(entry.getKey());
                if (user == null) {
                    continue;
                }
                user.getStats().setKills(user.getStats().getKills() + entry.getValue());
            }

            if (result == BattleResultType.LOSE) {
                ResourcePlanet resourcePlanet = (ResourcePlanet) fromPlanet;
                List<FightTotalKill> totalKills = fightResultPacket.getKill();
                Iterator<FightTotalKill> iterator = totalKills.iterator();

                if (!totalKills.isEmpty()) {
                    boolean delivered = false;
                    while (!delivered && iterator.hasNext()) {
                        FightTotalKill fightTotalKill = iterator.next();
                        User selected = UserService.getInstance().getUserCache().findByUserId(fightTotalKill.getUserId());
                        if (selected != null) {
                            Corp selectedCorp = selected.getCorp();
                            if (selectedCorp != null && selectedCorp.getRbpLimit() > selectedCorp.getResourcePlanets().size()) {
                                resourcePlanet.setPeace(true);
                                resourcePlanet.setCurrentCorp(selectedCorp.getCorpId());
                                resourcePlanet.setStatusTime(DateUtil.now(GalaxyService.getInstance().getRBPPeaceTime()));
                                resourcePlanet.save();
                                delivered = true;
                            }
                        }
                    }
                }

            }

        }

        if (fromPlanet.getType() == PlanetType.HUMAROID_PLANET) {

            HumaroidPlanet humaroidPlanet = (HumaroidPlanet) fromPlanet;
            InstanceData instanceData = humaroidPlanet.getData();

            List<Pair<PropData, Integer>> rewards = new ArrayList<>();

            Map<Integer, Integer> shootdownsOverall = new HashMap<>();
            for (BattleShipCache shipCache : this.getBattleReport().getShipHistoric()) {
                if (shootdownsOverall.containsKey(shipCache.getGuid())) {
                    shootdownsOverall.put(shipCache.getGuid(), (int) (shootdownsOverall.get(shipCache.getGuid()) + shipCache.getShootdowns()));
                } else {
                    shootdownsOverall.put(shipCache.getGuid(), (int) shipCache.getShootdowns());
                }

            }
            for (Map.Entry<Integer, Integer> entry : shootdownsOverall.entrySet()) {
                if (entry.getKey() == -1) {
                    continue;
                }
                User user = UserService.getInstance().getUserCache().findByGuid(entry.getKey());
                if (user == null) {
                    continue;
                }
                user.getStats().setKills(user.getStats().getKills() + entry.getValue());
            }


            if (result == BattleResultType.LOSE) {

                humaroidPlanet.setDestroyed(true);
                humaroidPlanet.setPeace(true);
                humaroidPlanet.setStatusTime(DateUtil.now(GalaxyService.getInstance().getHumaroidRuinTime()));
                humaroidPlanet.save();

                List<FightTotalKill> totalKills = fightResultPacket.getKill();
                Iterator<FightTotalKill> iterator = totalKills.iterator();

                while (iterator.hasNext()) {

                    FightTotalKill fightTotalKill = iterator.next();
                    User user = UserService.getInstance().getUserCache().findByUserId(fightTotalKill.getUserId());

                    if (user != null) {

                        List<RewardMeta> rewardMetas = new ArrayList<>(instanceData.getRewards());
                        Collections.shuffle(rewardMetas);

                        Optional<RewardMeta> optional = rewardMetas.stream().filter(rewardMeta -> rewardMeta.isOne()).findFirst();

                        if (optional.isPresent()) {

                            RewardMeta rewardMeta = optional.get();
                            PropData defResult = ResourceManager.getProps().getData(rewardMeta.getProp());
                            if (defResult != null) {
                                rewards.add(Pair.of(defResult, rewardMeta.getNum()));
                            }

                        }

                        for (RewardMeta rewardMeta : rewardMetas) {
                            if (rewardMeta.isOne()) {
                                continue;
                            } else if (rewardMeta.getWeight() >= 100) {

                                PropData defResult = ResourceManager.getProps().getData(rewardMeta.getProp());
                                if (defResult == null) {
                                    break;
                                }

                                rewards.add(Pair.of(defResult, rewardMeta.getNum()));

                            } else {

                                int random = MathUtil.randomInclusive(1, 100);
                                if (random <= rewardMeta.getWeight()) {

                                    PropData defResult = ResourceManager.getProps().getData(rewardMeta.getProp());
                                    if (defResult == null) {
                                        break;
                                    }

                                    rewards.add(Pair.of(defResult, rewardMeta.getNum()));

                                }

                            }
                        }

                        UserEmailStorage userEmailStorage = user.getUserEmailStorage();

                        Email email = Email.builder()
                                .autoId(userEmailStorage.nextAutoId())
                                .name("System")
                                .readFlag(0)
                                .date(DateUtil.now())
                                .goods(new ArrayList<>())
                                .guid(-1)
                                .build();

                        email.setSubject("Humaroid Rewards");
                        email.setEmailContent("You completed the humaroid lv" + (instanceData.getId() + 1) + " and earned the following rewards:");

                        email.setType(2);

                        for (Pair<PropData, Integer> reward : rewards) {
                            email.addGood(EmailGood.builder()
                                    .goodId(reward.getKey().getId())
                                    .lockNum(reward.getValue())
                                    .build());
                        }

                        userEmailStorage.addEmail(email);

                        Email resMail = Email.builder()
                                .autoId(userEmailStorage.nextAutoId())
                                .type(4)
                                .name("System")
                                .subject("Humaroid Resources Rewards")
                                .emailContent("As a bonus, you also earned the following resources:")
                                .readFlag(0)
                                .date(DateUtil.now())
                                .goods(new ArrayList<>())
                                .guid(-1)
                                .build();
                        int baseResMin = 8_000_000;
                        int baseResMax = 10_000_000;
                        int lvl = humaroidPlanet.getCurrentLevel() + 1;
                        int goldProfit = MathUtil.randomInclusive(baseResMin, baseResMax) * lvl;
                        int metalProfit = MathUtil.randomInclusive(baseResMin, baseResMax) * lvl;
                        int he3Profit = MathUtil.randomInclusive(baseResMin, baseResMax) * lvl;
                        resMail.addGood(EmailGood.builder()
                                .goodId(0)
                                .num(goldProfit)
                                .build());
                        resMail.addGood(EmailGood.builder()
                                .goodId(2)
                                .num(metalProfit)
                                .build());
                        resMail.addGood(EmailGood.builder()
                                .goodId(3)
                                .num(he3Profit)
                                .build());
                        userEmailStorage.addEmail(resMail);


                        ResponseNewEmailNoticePacket packet = ResponseNewEmailNoticePacket.builder()
                                .errorCode(0)
                                .build();

                        Optional<LoggedGameUser> onlineUser = user.getLoggedGameUser();
                        if (onlineUser.isPresent()) {
                            onlineUser.get().getSmartServer().send(packet);
                        }

                        user.update();
                        user.save();
                        break;

                    }
                }

            }

        }

        ResponseFightGalaxyOverPacket overPacket = new ResponseFightGalaxyOverPacket();
        overPacket.setGalaxyId(getGalaxyId());

        PacketRouter.getInstance().broadcast(overPacket);
        sendPacketToViewers(fightResultPacket);

        returnAllFleets();

    }

    @Override
    public void updateFleet(BattleFleet battleFleet) {

    }

    @Override
    public void removeFleet(BattleFleet battleFleet) {

        getFleets().remove(battleFleet);
        removedFleets.add(battleFleet);

    }

    @Override
    public void removeFort(BattleFort battleFort) {
/*
         getForts().remove(battleFort);
         removedForts.add(battleFort);
*/
    }

    @Override
    public void returnAllFleets() {

        List<Fleet> toRemove = new ArrayList<>();
        List<Fleet> toReturn = new ArrayList<>();

        Planet currentPlanet = getTargetPlanet();
        int currentGuid = -1;
        int corpId = -1;

        if (currentPlanet.getType() == PlanetType.USER_PLANET) {
            UserPlanet userPlanet = (UserPlanet) currentPlanet;
            Optional<User> optionalUser = userPlanet.getUser();
            if (optionalUser.isPresent()) {
                currentGuid = optionalUser.get().getGuid();
                corpId = optionalUser.get().getConsortiaId();
            }
        }

        for (BattleFleet userFleet : getAllFleets()) {

            if (userFleet.isPirate()) {
                Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(userFleet.getShipTeamId());
                if (fleet == null) {
                    continue;
                }

                toRemove.add(fleet);
                continue;
            }

            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(userFleet.getShipTeamId());
            if (fleet == null) {
                continue;
            }

            Commander commander = fleet.getCommander();
            if (commander != null) {
                commander.save();
            }

            User user = UserService.getInstance().getUserCache().findByGuid(fleet.getGuid());
            boolean defenderAndSameCorps = userFleet.isDefender() && corpId != -1 && user.getConsortiaId() != -1 && user.getConsortiaId() == corpId;
            if (fleet.getGuid() == currentGuid || defenderAndSameCorps) {

                fleet.setMatch(false);
                fleet.setFleetMatch(null);
                fleet.setFleetInitiator(null);
                fleet.setGalaxyId(getGalaxyId());

                fleet.save();
                continue;

            }

            fleet.setMatch(false);
            fleet.setFleetMatch(null);
            fleet.setFleetInitiator(null);
            fleet.setGalaxyId(getGalaxyId());

            toReturn.add(fleet);

        }

        returnFleets(toReturn);
        for (Fleet fleet : toRemove) {
            PacketService.getInstance().getFleetCache().delete(fleet);
        }
    }

    public static void returnFleets(List<Fleet> toReturn) {
        Set<Integer> userGuids = toReturn.stream().map(Fleet::getGuid).collect(Collectors.toSet());

        for (int guid : userGuids) {

            User user = UserService.getInstance().getUserCache().findByGuid(guid);
            if (user == null) {
                continue;
            }

            UserPlanet planet = user.getPlanet();

            List<Fleet> fleets = toReturn.stream().filter(fleet -> fleet.getGuid() == guid).toList();
            List<JumpShipTeamInfo> teamInfos = new ArrayList<>();

            for (Fleet fleet : fleets) {

                GalaxyTile targetTile = planet.getPosition();
                GalaxyTile from = new GalaxyTile(fleet.getGalaxyId());

                double currentDistance = from.getManhattanDistance(targetTile);
                double currentTransmission = (fleet.getTransmissionRate() * currentDistance + fleet.getTransmissionStart()) / 2;

                Date until = DateUtil.now((int) currentTransmission);
                int spare = DateUtil.remains(until).intValue();

                fleet.setFleetTransmission(FleetTransmission.builder()
                        .galaxyId(targetTile.galaxyId())
                        .jumpType(JumpType.RECALL)
                        .total(spare)
                        .until(until)
                        .build());

                teamInfos.add(JumpShipTeamInfo.builder()
                        .userId(user.getUserId())
                        .userName(user.getUsername())
                        .shipTeamId(fleet.getShipTeamId())
                        .fromGalaxyId(fleet.getGalaxyId())
                        .toGalaxyId(targetTile.galaxyId())
                        .spareTime(spare)
                        .totalTime(spare)
                        .fromGalaxyMapId(0)
                        .toGalaxyMapId(0)
                        .kind((byte) 0)
                        .galaxyType((byte) 1)
                        .build());

                fleet.save();

            }

            Optional<LoggedGameUser> optionalLoggedGameUser = user.getLoggedGameUser();

            if (optionalLoggedGameUser.isPresent()) {

                LoggedGameUser loggedGameUser = optionalLoggedGameUser.get();

                for (JumpShipTeamInfo teamInfo : teamInfos) {

                    ResponseJumpShipTeamPacket response = new ResponseJumpShipTeamPacket();
                    response.setData(teamInfo);

                    List<LoggedGameUser> viewers = LoginService.getInstance().getPlanetViewers(teamInfo.getFromGalaxyId());

                    for (LoggedGameUser viewer : viewers) {

                        ResponseDeleteShipTeamBroadcastPacket broadcast = new ResponseDeleteShipTeamBroadcastPacket();

                        broadcast.setGalaxyMapId(0);
                        broadcast.setGalaxyId(teamInfo.getFromGalaxyId());
                        broadcast.setShipTeamId(teamInfo.getShipTeamId());

                        viewer.getSmartServer().send(broadcast);

                    }

                    loggedGameUser.getSmartServer().send(response);

                }

            }

        }
    }

    @Override
    public AttackSideType fortressAttackType() {

        return AttackSideType.ATTACKER;
    }

    @Override
    public boolean hasUser(int guid) {
        return sourceIds.contains(guid) || targetIds.contains(guid);
    }

    public BattleResultType getResultType() {

        boolean attackers = getFleets().stream().filter(fleet -> !fleet.isDestroyed()).anyMatch(BattleFleet::isAttacker) || getForts().stream().filter(fort -> !fort.isDestroyed()).anyMatch(BattleFort::isAttacker);
        boolean defenders = getFleets().stream().filter(fleet -> !fleet.isDestroyed()).anyMatch(BattleFleet::isDefender) || getForts().stream().filter(fort -> !fort.isDestroyed()).anyMatch(BattleFort::isDefender);
        if (attackers && defenders) {
            return BattleResultType.WIN;
        }
        if (!attackers && defenders) {
            return BattleResultType.WIN;
        }
        if (attackers) {
            return BattleResultType.LOSE;
        }
        return BattleResultType.WIN;
    }

    public List<BattleFleet> getAllFleets(int guid) {

        return getAllFleets().stream().filter(fleet -> fleet.getGuid() == guid).collect(Collectors.toList());
    }

    public List<BattleFleet> getAllFleets() {

        return Stream.concat(getFleets().stream(), getRemovedFleets().stream()).collect(Collectors.toList());
    }

}
