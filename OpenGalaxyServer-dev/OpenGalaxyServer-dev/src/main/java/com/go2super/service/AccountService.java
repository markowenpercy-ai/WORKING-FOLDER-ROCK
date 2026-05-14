package com.go2super.service;

import com.go2super.database.cache.AccountCache;
import com.go2super.database.cache.PlanetCache;
import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.dto.ChangePasswordDTO;
import com.go2super.dto.CreateUserDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.dto.response.PlayUserDTO;
import com.go2super.dto.response.UserDTO;
import com.go2super.obj.model.LoggedSessionAccount;
import com.go2super.obj.model.LoggedSessionUser;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.service.jobs.user.RankJob;
import com.go2super.service.league.LeagueRankService;
import com.go2super.socket.util.Crypto;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.RandomUtil;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

@Service
public class AccountService {

    public static int MAX_PLANET = 8388607;

    String usernamePattern = "^[\\w\\d\\S]{1,32}$";
    Pattern pattern = Pattern.compile(usernamePattern);

    private static AccountService accountService;

    @Value("${application.game.max-users}")
    @Getter
    private Integer maxUsers;

    @Getter
    private final UserCache userCache;
    @Getter
    private final AccountCache accountCache;
    @Getter
    private final PlanetCache planetCache;

    @Autowired
    public AccountService(UserCache userCache, AccountCache accountCache, PlanetCache planetCache) {

        accountService = this;

        this.userCache = userCache;
        this.accountCache = accountCache;
        this.planetCache = planetCache;

    }

    public BasicResponse listOfUser(HttpServletRequest request) {

        Optional<LoggedSessionAccount> sessionAccount = LoginService.getInstance().getSessionAccount(request.getHeader("Authorization"));

        if (sessionAccount.isEmpty()) {
            return BasicResponse
                .builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("UNAUTHORIZED")
                .build();
        }

        Account account = sessionAccount.get().getAccount();
        List<User> users = userCache.findByAccountId(account.getId().toString());

        List<UserDTO> response = users
            .stream()
            .map(user -> {

                UserDTO userDTO = UserDTO
                    .builder()
                    .username(user.getUsername())
                    .star(String.format("%d-%d", user.getStarFace(), user.getStarType()))
                    .ground(user.getGround())
                    .userId(user.getUserId())
                    .resources(user.getResources())
                    .build();

                return userDTO;

            })
            .collect(Collectors.toList());

        return BasicResponse
            .builder()
            .code(HttpStatus.OK.value())
            .message("OK")
            .data(response)
            .build();

    }

    public BasicResponse play(long userId, HttpServletRequest request) {

        Optional<LoggedSessionAccount> sessionAccount = LoginService.getInstance().getSessionAccount(request.getHeader("Authorization"));

        if (sessionAccount.isEmpty()) {
            return BasicResponse
                .builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("UNAUTHORIZED")
                .build();
        }

        Account account = sessionAccount.get().getAccount();
        List<User> users = userCache.findByAccountId(account.getId().toString());

        User user = users.stream().limit(maxUsers).filter(x -> x.getUserId() == userId).findAny().orElse(null);

        if (user == null) {
            return BasicResponse
                .builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message("NOT_FOUND")
                .build();
        }

        // Check if this user already has an active session — reuse it instead of disconnecting
        for (LoggedSessionUser session : LoginService.getInstance().getSessionUsers()) {
            if (session.getUser().getUserId() != userId) {
                continue;
            }

            // User already has an active session — return the existing key instead of reconnecting
            PlayUserDTO userDTO = PlayUserDTO
                .builder()
                .sessionKey(session.getSessionKey())
                .userId(user.getUserId())
                .build();

            return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("OK")
                .data(userDTO)
                .build();
        }

        String sessionKey = RandomStringUtils.randomAlphanumeric(25);

        LoggedSessionUser session = LoggedSessionUser
            .builder()
            .user(user)
            .sessionKey(sessionKey)
            .build();

        LoginService.getInstance().addSessionUser(session);

        SessionService.getInstance().updateSessionKey(account.getId().toString(), sessionKey);

        PlayUserDTO userDTO = PlayUserDTO
            .builder()
            .sessionKey(sessionKey)
            .userId(user.getUserId())
            .build();

        return BasicResponse
            .builder()
            .code(HttpStatus.OK.value())
            .message("OK")
            .data(userDTO)
            .build();
    }

    public BasicResponse createUser(CreateUserDTO dto, HttpServletRequest request) {

        Optional<LoggedSessionAccount> sessionAccount = LoginService.getInstance().getSessionAccount(request.getHeader("Authorization"));

        if (sessionAccount.isEmpty()) {
            return BasicResponse
                .builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("UNAUTHORIZED")
                .build();
        }

        Matcher matcher = pattern.matcher(dto.getUsername());

        if (!matcher.find() || !StringUtils.isAlphanumeric(dto.getUsername())) {
            return BasicResponse
                .builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("NOT_VALID_USERNAME")
                .build();
        }

        Optional<User> user = userCache.findByUsername(dto.getUsername());

        if (user.isPresent()) {
            return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("USERNAME_TAKEN")
                .build();
        }

        if (dto.getGround() < 0 || dto.getGround() > 2) {
            return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("INVALID_GROUND")
                .build();
        }

        Account account = sessionAccount.get().getAccount();
        List<User> users = userCache.findByAccountId(account.getId().toString());

        if (users.size() >= maxUsers) {
            return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("MAX_USER")
                .build();
        }

        long userId = AutoIncrementService.getInstance().getNextUserId();
        int ground = dto.getGround();

        User.UserBuilder newUserBuilder = User.builder()

            .id(ObjectId.get())
            .accountId(account.getId().toString())

            .guid(AutoIncrementService.getInstance().getNextGuid())
            .userId(userId)

            .username(dto.getUsername())
            .ground(ground)

            .gameServerId(' ')
            .card1(0)
            .card2(0)
            .card3(0)
            .cardCredit(0)
            .cardUnion(0)
            .chargeFlag(0)
            .shipSpeedCredit(0)
            .lotteryStatus(0)
            .consortiaId(-1)
            .consortiaJob(-1)
            .consortiaUnionLevel(-1)
            .consortiaShop(-1)
            .consortiaThrow(-1)
            .consortiaUnion(-1)
            .day(0)
            .year((short) 0)
            .month(0)
            .gMapId(0)
            .noviceGuide(0)
            .tollGate(0)
            .lastRecruit(DateUtil.now(-1000000))

            .tasks(UserTasks.builder()
                .currentMain(UserTask.builder().taskId(0).type(0).level(-1).build())
                .currentSide(new ArrayList<>())
                .completed(new ArrayList<>())
                .build())

            .territories(UserTerritories.builder()
                .territories(new ArrayList<>())
                .build())

            .chips(UserChips.builder()
                .chips(new ArrayList<>())
                .build())

            .rewards(UserRewards.builder()
                .level(-1)
                .build())

            .metrics(UserMetrics.builder().build());

        User newUser = newUserBuilder.build();

        UserPlanet userPlanet = createNewUserPlanet(newUser, userId);

        while (userCache.findByGuid(newUser.getGuid()) != null) {
            newUser.setGuid(RandomUtil.getRandomInt(MAX_PLANET));
        }

        newUser.setShipUpgrades(createNewUserShipUpgrades());
        newUser.setShips(createNewUserShips());
        newUser.setBuildings(createNewUserBuildings());
        newUser.setStorage(createNewUserStorage());
        newUser.setUserEmailStorage(createNewUserEmailStorage(users.size() == 0));
        newUser.setInventory(createNewUserInventory());
        newUser.setTechs(createNewUserTechs());
        newUser.setStats(createNewUserStats());
        newUser.setResources(createNewUserResources(users.size() == 0));

        // Set mainPlanetId if this is the first planet for the account
        if (account.getMainPlanetId() == null) {
            account.setMainPlanetId(userId);
        }

        accountCache.save(account);
        userCache.save(newUser);
        planetCache.save(userPlanet);

        RankJob rankJob = JobService.getOfflineJob(RankJob.class);
        if (rankJob != null) {
            rankJob.synchronizedAdd(newUser);
        }

        LeagueRankService.getInstance().addNewUser(newUser);

        return BasicResponse
            .builder()
            .code(HttpStatus.OK.value())
            .message("CREATE_COMPLETED")
            .build();

    }

    public BasicResponse changePassword(ChangePasswordDTO dto, HttpServletRequest request) {
        Optional<LoggedSessionAccount> sessionAccount = LoginService.getInstance().getSessionAccount(request.getHeader("Authorization"));

        if (sessionAccount.isEmpty()) {
            return BasicResponse
                .builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("UNAUTHORIZED")
                .build();
        }

        Account account = sessionAccount.get().getAccount();

        if (!Crypto.decrypt(account.getPassword()).equals(dto.getOldPassword())) {
            return BasicResponse
                .builder()
                .code(HttpStatus.UNAUTHORIZED.value())
                .message("UNAUTHORIZED")
                .build();
        }

        if (dto.getNewPassword().length() < 8) {
            return BasicResponse
                .builder()
                .code(HttpStatus.FORBIDDEN.value())
                .message("PASSWORD_TOO_SHORT")
                .build();
        }
        if (dto.getNewPassword().equals(dto.getOldPassword())) {
            return BasicResponse
                .builder()
                .code(HttpStatus.FORBIDDEN.value())
                .message("PASSWORD_CANNOT_MATCH_PASSWORD")
                .build();
        }
        if (dto.getNewPassword().equals(account.getUsername())) {
            return BasicResponse
                .builder()
                .code(HttpStatus.FORBIDDEN.value())
                .message("PASSWORD_CANNOT_MATCH_USERNAME")
                .build();
        }
        if (dto.getNewPassword().equals(account.getEmail())) {
            return BasicResponse
                .builder()
                .code(HttpStatus.FORBIDDEN.value())
                .message("PASSWORD_CANNOT_MATCH_EMAIL")
                .build();
        }

        account.setPassword(Crypto.encrypt(dto.getNewPassword()));
        accountService.getAccountCache().save(account);
        LoginService.getInstance().disconnectWeb(sessionAccount.get().getToken());


        for (LoggedSessionUser session : LoginService.getInstance().getSessionUsers()) {
            if (!Objects.equals(session.getUser().getAccountId(), sessionAccount.get().getAccount().getId().toString())) {
                continue;
            }

            LoginService.getInstance().disconnectGame(session);
            LoginService.getInstance().disconnectUser(session);
        }

        return BasicResponse
            .builder()
            .code(HttpStatus.OK.value())
            .message("OK")
            .build();
    }

    public UserStats createNewUserStats() {

        return UserStats.builder()
            .exp(0)
            .sp(20)
            .build();
    }

    public UserShipUpgrades createNewUserShipUpgrades() {

        return UserShipUpgrades.builder()
            .currentBodies(new ArrayList<>(List.of(0)))
            .currentParts(new ArrayList<>(Arrays.asList(18, 54, 90, 126, 0)))
            .shipUpgrade(null)
            .partUpgrade(null)
            .build();
    }

    public UserShips createNewUserShips() {

        return UserShips.builder()
            .ships(new ArrayList<>())
            .factory(new ArrayList<>())
            .build();
    }

    public UserTechs createNewUserTechs() {
        List<UserTech> tech = new ArrayList<>();
        tech.add(UserTech.builder().id(100).level(1).build());

        return UserTechs.builder()
            .techs(tech)
            .build();
    }

    public UserResources createNewUserResources(boolean firstUser) {
        return UserResources.builder()
            .badge(0)
            .honor(0)
            .corsairs(0)
            .coupons(0)
            .mallPoints(firstUser ? 25000 : 0)
            .vouchers(100)
            .gold(10000)
            .metal(10000)
            .he3(10000)
            .freeSpins(1)
            .build();
    }

    public UserStorage createNewUserStorage() {

        return UserStorage.builder()
            .he3(10000)
            .metal(10000)
            .gold(10000)
            .goldProduction(0)
            .metalProduction(0)
            .he3Production(0)
            .lastProductionCalculus(new Date()).build();
    }

    public UserEmailStorage createNewUserEmailStorage(boolean isMain) {

        UserEmailStorage userEmailStorage = UserEmailStorage.builder()
            .userEmails(new ArrayList<>())
            .build();

        createWelcomeEmail(userEmailStorage, isMain);
        return userEmailStorage;

    }

    public void createWelcomeEmail(UserEmailStorage userEmailStorage, boolean isMain) {

        Email email = Email.builder()
            .autoId(userEmailStorage.nextAutoId())
            .type(2)
            .name("System")
            .subject("Welcome to OpenGalaxy!")
            .emailContent(
                "Welcome to OpenGalaxy, Commander! The Galactic Council welcomes you to the ultimate universe of interstellar warfare. Build your empire, command your fleets, and conquer rival Corps in this epic strategy MMO. Complete daily Quests to earn rewards and rise through the ranks. We're thrilled to have you join us - prepare for battle!")
            .readFlag(0)
            .date(DateUtil.now())
            .goods(new ArrayList<>())
            .guid(-1)
            .build();

        email.addGood(1572, 1);
        email.addGood(905, 6);
        email.addGood(906, 6);
        email.addGood(907, 6);
        email.addGood(923, 5);
        email.addGood(921, 1000);
        email.addGood(924, 10);
        email.addGood(925, 4);
        email.addGood(937, 1);
        email.addGood(1119, 20);

        userEmailStorage.addEmail(email);

    }

    public UserBuildings createNewUserBuildings() {

        UserBuildings buildings = UserBuildings.builder()
            .build()
            .addBuilding(1, 0, 693, 886)
            .addBuilding(2, 0, 914, 962)
            .addBuilding(3, 0, 1136, 982)
            .addBuilding(0, 0, 1082, 802)
            .addBuilding(13, 0, 12, 12)
            .addBuilding(4, 0, 831, 774);

        return buildings;

    }

    public UserInventory createNewUserInventory() {

        UserInventory inventory = UserInventory.builder()
            .maximumStacks(30)
            .stackPrice(1000)
            .propList(new ArrayList<>()).build();

        return inventory;

    }

    public UserPlanet createNewUserPlanet(User user, long userId) {

        GalaxyTile randomTile = GalaxyService.getInstance().randomAvailablePosition();
        return new UserPlanet(user.getId().toString(), userId, randomTile, 0);
    }

    public static Date now() {

        return new Date();
    }

    public static AccountService getInstance() {

        return accountService;
    }

    public static LoginService getLoginService() {

        return LoginService.getInstance();
    }

}
