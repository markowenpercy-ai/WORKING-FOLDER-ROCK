package com.go2super.database.entity;

import com.go2super.database.entity.sub.*;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.BonusType;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.custom.CustomWarnPacket;
import com.go2super.packet.props.ResponseTimeQueuePacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.FarmLandData;
import com.go2super.resources.json.RewardsJson;
import com.go2super.service.*;
import com.go2super.service.league.LeagueRankService;
import com.go2super.socket.util.DateUtil;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_users")
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class User {

    @Id
    private ObjectId id;
    private String accountId;

    @Column(unique = true)
    private int guid;
    @Column(unique = true)
    private long userId;
    @Column(unique = true)
    private String username;

    // Ground
    //
    // 0 = Desert
    // 1 = Snow
    // 2 = Plains
    //
    private int ground;
    private int gMapId;

    private int consortiaId;
    private int consortiaJob;
    private int consortiaUnionLevel;

    private int gameServerId;
    private int card1;
    private int cardCredit;
    private int card2;
    private int card3;
    private int cardUnion;
    private int chargeFlag;
    private int shipSpeedCredit;
    private int lotteryStatus;

    @Deprecated
    private int consortiaThrow;
    @Deprecated
    private int consortiaUnion;
    @Deprecated
    private int consortiaShop;

    private int tollGate;
    private short year;
    private int month;
    private int day;
    private int noviceGuide;
    private int warScore;

    private byte leagueCount;

    private boolean notDisturb;

    private Date lastRecruit;
    private Date lastDayUpdate;

    private double userMaxPpt;
    private boolean toSave;
    private boolean pirateReceived;
    @Field(name = "game_flag")
    private UserFlag flag;
    @Field(name = "game_stats")
    private UserStats stats;
    @Field(name = "game_ships")
    private UserShips ships;
    @Field(name = "game_user_techs")
    private UserTechs techs = UserTechs.builder()
        .techs(new ArrayList<>())
        .build();

    @Field(name = "game_tasks")
    private UserTasks tasks = UserTasks.builder()
        .currentMain(UserTask.builder().taskId(0).type(0).level(-1).build())
        .currentSide(new ArrayList<>())
        .completed(new ArrayList<>())
        .build();

    @Field(name = "game_territories")
    private UserTerritories territories = UserTerritories.builder()
        .territories(new ArrayList<>())
        .build();

    @Field(name = "game_bionic_chips")
    private UserChips chips = UserChips.builder()
        .chips(new ArrayList<>())
        .build();

    @Field(name = "game_rewards")
    private UserRewards rewards = UserRewards.builder()
        .level(-1)
        .build();

    @Field(name = "game_metrics")
    private UserMetrics metrics = UserMetrics.builder().build();
    @Field(name = "game_upgrades")
    private UserShipUpgrades shipUpgrades;
    @Field(name = "game_resources")
    private UserResources resources;
    @Field(name = "game_buildings")
    private UserBuildings buildings;
    @Field(name = "game_resource_storage")
    private UserStorage storage;
    @Field(name = "game_user_emails")
    private UserEmailStorage userEmailStorage;
    @Field(name = "game_user_inventory")
    private UserInventory inventory;
    @Field(name = "game_corp_inventory")
    private CorpInventory corpInventory;
    @Field(name = "game_user_friends")
    private List<Integer> friends;
    @Field(name = "game_block_users")
    private List<Integer> blockUsers;

    public void update() {

        getBuildings().refresh();
        UserService.getInstance().updateResources(this);
        UserService.getInstance().updateStats(this);
        UserService.getInstance().updateShips(this);
        UserService.getInstance().updateEmails(this);

        TaskService.getInstance().updateTasks(this);

    }

    public void save() {
        toSave = true;
    }

    public Optional<LoggedGameUser> getLoggedGameUser() {

        return LoginService.getInstance().getGame(this);
    }

    public boolean isOnline() {

        return getLoggedGameUser().isPresent();
    }

    public void addFriend(int guid) {

        if (!getFriendsIds().contains(guid)) {
            getFriendsIds().add(guid);
        }
    }

    public void removeFriend(int guid) {

        if (getFriendsIds().contains(guid)) {
            getFriendsIds().remove((Integer) guid);
        }
    }

    public boolean isFriend(int guid) {

        return getFriendsIds().contains(guid);
    }

    public List<Integer> getFriendsIds() {

        if (friends == null) {
            friends = new ArrayList<>();
        }
        return friends;
    }

    public List<User> findFriends() {

        List<User> friends = new ArrayList<>();

        for (int friendId : getFriendsIds()) {

            if (friendId == guid) {
                continue;
            }

            User friend = UserService.getInstance().getUserCache().findByGuid(friendId);

            if (friend == null) {
                continue;
            }

            friends.add(friend);

        }

        return friends;

    }

    public int getViewFlag(int requester) {

        if (requester == guid || isFriend(requester)) {
            return 1;
        }

        User user = UserService.getInstance().getUserCache().findByGuid(requester);
        if (user == null) {
            return 0;
        }

        Corp corp = user.getCorp();
        Corp hereCorp = getCorp();

        if (corp == null || hereCorp == null) {
            return 0;
        }
        return corp.getCorpId() == hereCorp.getCorpId() ? 1 : 0;

    }

    public int getRanking() {

        return RankService.getInstance().getAttackPower(this);
    }

    public int getCityLevel() {

        return getBuildings().getBuildings("build:civicCenter").get(0).getLevelId();
    }

    public int getSpaceStationLevel() {

        return getBuildings().getBuildings("build:spaceStation").get(0).getLevelId();
    }

    public int getCurrentLeague() {
        if (stats.getLeagueStats() == null) {
            stats.setLeagueStats(UserLeagueStats.builder().league(0).draws(0).wins(0).losses(0).build());
            save();
        }
        return stats.getLeagueStats().getLeague();
    }

    public int getCurrentInstance() {

        return 0;
    }

    public int getStarFace() {

        Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findById(accountId);

        if (optionalAccount.isPresent()) {

            Account account = optionalAccount.get();
            if (account.getUserRank().hasPermission("permission.i.g.c")) {
                return 20;
            }

        }

        List<BonusType> types = stats.getAllBonuses();

        if (types.contains(BonusType.PLANET_APPEARANCE)) {
            if (types.contains(BonusType.LUXURIOUS_GOLD_RESOURCE_PRODUCTION)) {
                return 10;
            } else if (types.contains(BonusType.METALLIC_METAL_RESOURCE_PRODUCTION)) {
                return 11;
            } else if (types.contains(BonusType.GASEOUS_HE3_RESOURCE_PRODUCTION)) {
                return 12;
            } else if (types.contains(BonusType.ORDINARY_PLANET)) {
                return 13;
            } else if (types.contains(BonusType.CHRISTMAS_RESOURCE_PRODUCTION)) {
                return 14;
            } else if (types.contains(BonusType.GF_RESOURCE_PRODUCTION)) {
                return 15;
            } else if (types.contains(BonusType.HALLOWEEN_RESOURCE_PRODUCTION)) {
                return 16;
            }
        }

        return 9;

    }

    public int getStarType() {

        Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findById(accountId);

        if (optionalAccount.isPresent()) {

            Account account = optionalAccount.get();
            if (account.getUserRank().hasPermission("permission.i.g.c")) {
                return 6;
            }

        }

        List<FarmLandData> farmLands = getPlanet().getAdjacentFarmLands();

        if (farmLands.size() <= 2) {
            return 4;
        }

        if (farmLands.size() <= 8) {
            return 5;
        }

        return 6;

    }

    public int getAttackPowerRank() {

        return RankService.getInstance().getAttackPowerRank(this);
    }

    public int getShootdownsRank() {

        return RankService.getInstance().getShootdownsRank(this);
    }

    public int getNextRecruit() {

        if (stats.getNextInvitation() == null || DateUtil.remains(stats.getNextInvitation()) <= 0) {
            return 0;
        }

        return Math.toIntExact(DateUtil.remains(stats.getNextInvitation()));

    }

    public Commander getCommanderBySkill(int skillId) {

        return CommanderService.getInstance().getCommanderCache().findBySkillAndUserId(skillId, getPlanet().getUserId());
    }

    public Commander getCommander(int commanderId) {

        return CommanderService.getInstance().getCommanderCache().findByCommanderIdAndUserId(commanderId, getPlanet().getUserId());
    }

    public Corp getCorp() {

        return CorpService.getInstance().getCorpCache().findByGuid(guid);
    }

    public int getSpins() {

        if (getLastDayUpdate() != null) {
            if (DateUtil.currentDay(getLastDayUpdate())) {
                return resources.getFreeSpins();
            }
        }

        resetNewDay();
        return resources.getFreeSpins();

    }

    public int getMaxSpins() {

        boolean mvp = stats.hasBonus(BonusType.MVP_DAILY_DRAWS_BONUS);
        return mvp ? 3 : 1;

    }

    public Pair<Integer, Integer> getRaidEntries() {

        if (getLastDayUpdate() != null) {
            if (DateUtil.currentDay(getLastDayUpdate())) {
                return Pair.of(stats.getRaidAttemptsEntries(), stats.getRaidInterceptEntries());
            }
        }

        resetNewDay();
        return Pair.of(stats.getRaidAttemptsEntries(), stats.getRaidInterceptEntries());

    }

    public int getRestrictedUsedEntries() {

        if (getLastDayUpdate() != null) {
            if (DateUtil.currentDay(getLastDayUpdate())) {
                return stats.getRestrictedUsedEntries();
            }
        }

        resetNewDay();
        return stats.getRestrictedUsedEntries();

    }

    public int getTrial() {

        if (getLastDayUpdate() != null) {
            if (DateUtil.currentDay(getLastDayUpdate())) {
                return stats.getTrial();
            }
        }

        resetNewDay();
        return stats.getTrial();

    }

    public int getSp() {

        if (getLastDayUpdate() != null) {
            if (DateUtil.currentDay(getLastDayUpdate())) {
                return stats.getSp();
            }
        }

        resetNewDay();
        return stats.getSp();

    }

    public void resetNewDay() {

        setLastDayUpdate(DateUtil.now());
        UserRewards userRewards = getRewards();
        RewardsJson rewardsJson = ResourceManager.getRewards(getAccount().getUserRank());

        if (userRewards == null) {
            setRewards(UserRewards.builder()
                .level(-1)
                .build());
            userRewards = getRewards();
        }
        pirateReceived = false;
        userRewards.setLevel(0);
        userRewards.setUntil(DateUtil.now(rewardsJson.getReward(0).getTime()));

        resources.setFreeSpins(getMaxSpins());

        stats.setSp(stats.getMaxSp());
        stats.setTrial(0);
        stats.setRestrictedUsedEntries(0);

        if (stats.getLeagueStats() == null) {
            stats.setLeagueStats(UserLeagueStats.builder().league(0).draws(0).wins(0).losses(0).build());
        }

        if (stats.getIglStats() == null) {
            stats.setIglStats(UserIglStats.builder().claimed(false).entries(0).fleetIds(new ArrayList<>()).rank(IGLService.getInstance().totalCount()).build());
        }
        stats.getIglStats().setEntries(0);

        setLeagueCount((byte) 3);
        stats.setRaidAttemptsEntries(0);
        stats.setRaidInterceptEntries(0);
        stats.setCollectedPoints(false);

    }

    public ResponseTimeQueuePacket getQueuesAsPacket() {

        return UserService.getInstance().getUserQueues(this);
    }

    public int totalShips() {

        return countFleetShips() + ships.countStoredShips();
    }

    public int countFleetShips() {

        int ships = 0;
        for (Fleet fleet : getFleets()) {
            ships += fleet.ships();
        }
        return ships;
    }

    public void sendMessage(String content) {

        Optional<LoggedGameUser> optionalOnline = LoginService.getInstance().getGame(guid);
        if (optionalOnline.isEmpty()) {
            return;
        }

        optionalOnline.get().getSmartServer().sendMessage(content);

    }

    public void sendWarning(String content) {

        Optional<LoggedGameUser> optionalOnline = LoginService.getInstance().getGame(guid);
        if (optionalOnline.isEmpty()) {
            return;
        }

        LoggedGameUser online = optionalOnline.get();

        CustomWarnPacket response = new CustomWarnPacket();

        response.setSeqId(0);
        response.setSrcUserId(0);
        response.setObjUserId(0);
        response.setGuid(0);
        response.setObjGuid(0);
        response.setChannelType((short) 0);
        response.setSpecialType((short) 0);
        response.setPropsId(0);
        response.setName(WideString.of(getUsername(), 32));
        response.setToName(WideString.of(getUsername(), 32));
        response.setBuffer(WideString.of(content, 1024));

        online.getSmartServer().send(response);

    }

    public List<Fleet> getFleets() {

        return PacketService.getInstance().getFleetCache().findAllByGuid(this.getGuid());
    }

    public List<Fleet> getFleetsInTransmission() {

        return PacketService.getInstance().getFleetCache().getInTransmissionFleets(this.getGuid());
    }

    public List<Commander> getCommanders() {

        return CommanderService.getInstance().getCommanders(this);
    }

    public int getGalaxyId() {

        return getPlanet().getPosition().galaxyId();
    }

    public UserMetrics getMetrics() {

        if (metrics == null) {
            metrics = UserMetrics.builder().build();
        }
        return metrics;
    }

    public UserPlanet getPlanet() {

        return GalaxyService.getInstance().getUserPlanet(this);
    }

    public Account getAccount() {

        return AccountService.getInstance().getAccountCache().findById(accountId).orElse(null);
    }

    //
    // Default Initializers
    // - Add default initializers
    // added post-production
    //

}
