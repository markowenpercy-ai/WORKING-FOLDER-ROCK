package com.go2super.service;

import com.go2super.database.cache.AccountCache;
import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.Account;
import com.go2super.database.entity.AccountSession;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BadGuidIncident;
import com.go2super.service.UserService;
import com.go2super.database.entity.type.AccountStatus;
import com.go2super.database.entity.type.MatchType;
import com.go2super.database.entity.type.UserRank;
import com.go2super.dto.AccountDTO;
import com.go2super.dto.AccountLoginDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.dto.response.WebUserDTO;
import com.go2super.logger.BotLogger;
import com.go2super.logger.data.UserActionLog;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.model.LoggedSessionAccount;
import com.go2super.obj.model.LoggedSessionUser;
import com.go2super.obj.type.AuditType;
import com.go2super.packet.Packet;
import com.go2super.packet.login.PlayerLoginTogPacket;
import com.go2super.server.GameServerReceiver;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.match.ArenaMatch;
import com.go2super.service.battle.type.StopCause;
import com.go2super.service.exception.BadGuidException;
import com.go2super.service.league.LeagueMatchService;
import com.go2super.service.validation.RegisterValidation;
import com.go2super.socket.util.Crypto;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.net.InetAddress;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LoginService {

    private static final Map<String, RegisterValidation> registerValidations = new HashMap<>();

    private static LoginService loginService;

    @Getter
    private final CopyOnWriteArrayList<LoggedGameUser> gameUsers = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<LoggedSessionUser> sessionUsers = new CopyOnWriteArrayList<>();
    // private List<LoggedSessionAccount> sessionAccounts = new ArrayList<>();

    private final SessionService sessionService;

    @Getter
    AccountCache accountCache;
    @Getter
    UserCache userCache;

    @Autowired
    public LoginService(UserCache userCache, AccountCache accountCache, SessionService sessionService) {

        loginService = this;

        this.userCache = userCache;
        this.accountCache = accountCache;

        this.sessionService = sessionService;

    }

    public void addSessionUser(LoggedSessionUser loggedUser) {
        sessionUsers.add(loggedUser);
    }

    public List<LoggedSessionUser> getSessionUsers() {

        return sessionUsers;
    }

    public Optional<LoggedSessionUser> getSession(long userId) {

        Optional<LoggedSessionUser> inMemorySession = sessionUsers.stream().filter(user -> user.getUser().getUserId() == userId).findAny();
        if (inMemorySession.isPresent()) {
            return inMemorySession;
        }

        User user = UserService.getInstance().getUserCache().findByUserId(userId);
        if (user == null) {
            return Optional.empty();
        }

        Optional<AccountSession> dbSession = sessionService.getActiveAccountSessionByAccountIdAndNotExpired(user.getAccountId());
        if (dbSession.isEmpty() || dbSession.get().getSessionKey() == null) {
            return Optional.empty();
        }

        LoggedSessionUser restoredSession = LoggedSessionUser.builder()
                .user(user)
                .sessionKey(dbSession.get().getSessionKey())
                .build();
        sessionUsers.add(restoredSession);

        return Optional.of(restoredSession);
    }

    public Optional<LoggedSessionAccount> getSessionAccount(String token) {

        Optional<AccountSession> accountSessionOptional = sessionService.getActiveAccountSessionByToken(token);

        if (accountSessionOptional.isEmpty()) {
            return Optional.empty();
        }

        AccountSession accountSession = accountSessionOptional.get();

        return Optional.of(LoggedSessionAccount.builder()
                .account(accountSession.getReference())
                .token(accountSession.getToken())
                .build());

    }

    @SneakyThrows
    public void disconnectGame(LoggedGameUser user) {

        sessionUsers.remove(user.getLoggedSessionUser());
        gameUsers.remove(user);
        LeagueMatchService.getInstance().removePlayer(user.getGuid());
    }

    @SneakyThrows
    public void disconnectGame(LoggedSessionUser user) {

        for (LoggedGameUser loggedGameUser : LoginService.getInstance().getGameUsers()) {
            if (loggedGameUser.getUserId() == user.getUser().getPlanet().getUserId()) {
                try {
                    loggedGameUser.getSmartServer().close();
                } catch (Exception ex) {
                    //close failed, but ignore
                }

                gameUsers.remove(loggedGameUser);

                int guid = user.getUser().getGuid();

                List<Match> arenaMatches = BattleService.getInstance().getCurrent(guid, MatchType.ARENA_MATCH);

                if (!arenaMatches.isEmpty()) {
                    Optional<Match> optionalCurrent = arenaMatches.stream().findAny();
                    ArenaMatch arenaMatch = (ArenaMatch) optionalCurrent.get();
                    if (arenaMatch.getPause().get()) {
                        if (arenaMatch.getSourceGuid() == guid) {
                            BattleService.getInstance().stopMatch(arenaMatch, StopCause.MANUAL);
                        } else {
                            arenaMatch.opponentExit(false);
                        }
                    }
                }
                loggedGameUser.setMatchViewing(null);

                LeagueMatchService.getInstance().removePlayer(guid);

                BotLogger.login("Disconnected (Name: " + loggedGameUser.getUpdatedUser().getUsername() + ", Id: " + loggedGameUser.getGuid() + ", IP: " + PacketService.getInstance().getServerIp() + ")");

                // Audit
                Account account = user.getUser().getAccount();

                String buffer = "**Logout:** `" + user.getUser().getUsername() + " (ID: " + user.getUser().getGuid() + ", EMAIL: " + account.getEmail() + ")`\n";

                DiscordService.getInstance().getRayoBot().sendAudit(buffer, "", Color.red, AuditType.LOGIN);
                break;

            }
        }

    }

    @SneakyThrows
    public void disconnectUser(LoggedSessionUser user) {
        sessionUsers.remove(user);
    }

    @SneakyThrows
    public void disconnectWeb(String token) {

        sessionService.removeActiveAccountSession(token);
    }

    public InetAddress getAddress(int guid) {

        for (LoggedGameUser user : gameUsers) {
            if (user.getGuid() == guid) {
                return user.getAddress();
            }
        }

        return null;

    }

    public Optional<LoggedGameUser> getGame(SmartServer smartServer) {

        return gameUsers.stream().filter(user -> user.getSmartServer().equals(smartServer)).findAny();
    }

    public Optional<LoggedGameUser> getGame(long userId) {

        return gameUsers.stream().filter(user -> user.getUserId() == userId).findAny();
    }

    public Optional<LoggedGameUser> getGame(int guid) {

        return gameUsers.stream().filter(user -> user.getGuid() == guid).findAny();
    }

    public Optional<LoggedGameUser> getGame(User user) {

        return gameUsers.stream().filter(guser -> guser.getGuid() == user.getGuid()).findAny();
    }

    public Optional<LoggedGameUser> getGameByFormula(int guid) {

        return gameUsers.stream().filter(user -> user.getGuid() == MathUtil.toRealGuid(guid)).findAny();
    }

    public LoggedGameUser login(LoggedSessionUser sessionUser, PlayerLoginTogPacket packet) {

        LoggedGameUser gameUser = LoggedGameUser.builder()
                .address(packet.getSmartServer().getSocket().getInetAddress())
                .guid(sessionUser.getUser().getGuid())
                .userId(sessionUser.getUser().getPlanet().getUserId())
                .loggedSessionUser(sessionUser)
                .smartServer(packet.getSmartServer())
                .viewing(-1)
                .consortiaId(sessionUser.getUser().getConsortiaId())
                .build();

        gameUsers.add(gameUser);
        return gameUser;

    }

    public BasicResponse login(AccountLoginDTO dto) {

        Optional<Account> accountOptional = accountCache.findByEmail(dto.getUsername())
                .or(() -> accountCache.findByUsername(dto.getUsername()));

        if (accountOptional.isEmpty()) {
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_ACCEPTABLE.value())
                    .message("ACCOUNT_NOT_FOUND")
                    .build();
        }

        Account account = accountOptional.get();

        if (Crypto.decrypt(account.getPassword()).equals(dto.getPassword())) {
            String token = RandomStringUtils.randomAlphanumeric(40);
            sessionService.registerAccountSession(account, token);

            WebUserDTO webUserDTO = WebUserDTO
                    .builder()
                    .vip(account.getVip())
                    .email(account.getEmail())
                    .username(account.getUsername())
                    .rank(account.getUserRank())
                    .token(token)
                    .maxPlanet(AccountService.getInstance().getMaxUsers())
                    .build();

            return BasicResponse
                    .builder()
                    .code(HttpStatus.OK.value())
                    .message("LOGIN_COMPLETED")
                    .data(webUserDTO)
                    .build();

        }

        return BasicResponse
                .builder()
                .code(HttpStatus.NOT_ACCEPTABLE.value())
                .message("INVALID_CREDENTIALS")
                .build();
    }

    public BasicResponse logout(HttpServletRequest request) {
        Optional<AccountSession> authorization = sessionService.getActiveAccountSessionByToken(request.getHeader("Authorization"));
        if (authorization.isEmpty()) {
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("UNAUTHORIZED")
                    .build();
        }
        AccountSession accountSession = authorization.get();
        sessionService.removeAllSessionsByAccountId(accountSession.getReference().getId().toString());
        return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("OK")
                .build();
    }

    @SneakyThrows
    public BasicResponse register(AccountDTO dto) {

        if (dto.getCaptcha() == null || dto.getCaptcha().isEmpty()) {
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_ACCEPTABLE.value())
                    .message("BAD_VALIDATION_PARAMETER")
                    .build();
        }

        if (!PacketService.getInstance().isRegister()) {
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_ACCEPTABLE.value())
                    .message("SERVICE_UNAVAILABLE")
                    .build();
        }

        if (dto.getEmail().contains("+")) {
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_ACCEPTABLE.value())
                    .message("INVALID_EMAIL")
                    .build();
        }

        Optional<Account> account =
                accountCache.findByEmail(dto.getEmail())
                        .or(() -> accountCache.findByUsername(dto.getUsername()));

        if (account.isPresent()) {
            if (dto.getEmail().equals(account.get().getEmail())) {
                return BasicResponse
                        .builder()
                        .code(HttpStatus.NOT_ACCEPTABLE.value())
                        .message("EMAIL_TAKEN")
                        .build();
            } else if (dto.getUsername().equals(account.get().getUsername())) {
                return BasicResponse
                        .builder()
                        .code(HttpStatus.NOT_ACCEPTABLE.value())
                        .message("USERNAME_TAKEN")
                        .build();
            }
        }

        if (dto.getOtp() != null && dto.getOtp().length() == 4) {

            if (!registerValidations.containsKey(dto.getEmail())) {
                return BasicResponse
                        .builder()
                        .code(HttpStatus.NOT_ACCEPTABLE.value())
                        .message("OTP_INVALID")
                        .build();
            }

            RegisterValidation validation = registerValidations.get(dto.getEmail());

            if (new Date().after(validation.getUntil())) {
                registerValidations.remove(dto.getEmail());
                return BasicResponse
                        .builder()
                        .code(HttpStatus.NOT_ACCEPTABLE.value())
                        .message("OTP_EXPIRED")
                        .build();
            }

            if (!validation.getOtp().equals(dto.getOtp())) {
                return BasicResponse
                        .builder()
                        .code(HttpStatus.NOT_ACCEPTABLE.value())
                        .message("OTP_INVALID")
                        .build();
            }

            registerValidations.remove(dto.getEmail());

        }

        Account newAccount = Account.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(Crypto.encrypt(dto.getPassword()))
                .accountStatus(AccountStatus.REGISTER)
                .userRank(UserRank.USER)
                .registerDate(now())
                .vip(0)
                .build();

        accountCache.save(newAccount);

        return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("REGISTER_ACCOUNT_COMPLETED")
                .build();
    }

    public static void validate(Packet packet, int guid) throws BadGuidException {

        if (!(packet.getSmartServer() instanceof GameServerReceiver serverReceiver)) {
            throw new RuntimeException("Invalid SmartServer type");
        }

        boolean invalid = serverReceiver.getGuid() != guid;

        if (invalid) {

            User user = UserService.getInstance().getUserCache().findByGuid(serverReceiver.getGuid());

            if (user != null) {

                Optional<BadGuidIncident> optionalBadGuidIncident = RiskService.getInstance().getRiskIncidentRepository().checkBadGuidAndSave(user, serverReceiver, packet, guid);
                if (optionalBadGuidIncident.isPresent()) {

                    UserActionLog action = UserActionLog.builder()
                            .action("user-risk-bad-guid")
                            .message("[RiskIncident/BadGuid] " + user.getUsername() + " (IP: " + serverReceiver.getIp() + ":" + serverReceiver.getPort() + ", Guid: " + serverReceiver.getGuid() + ", UserId: " + serverReceiver.getUserId() + ", Target: " + guid + ")")
                            .build();

                    // EventLogger.sendUserAction(action, user, (GameServerReceiver) packet.getSmartServer());
                }
            }
            BotLogger.error("BadGUID: " + serverReceiver.getIp() + ":" + serverReceiver.getPort() + " - " + serverReceiver.getGuid() + " - " + serverReceiver.getUserId() + " - " + guid);
            String message = "Hacker: " + serverReceiver.getGuid() + ", HackerAccountName: " + serverReceiver.getAccountName() + ", Target: " + guid;
            throw new BadGuidException(message);
        }
    }

    public CopyOnWriteArrayList<LoggedGameUser> getPlanetViewers(Planet planet) {

        CopyOnWriteArrayList<LoggedGameUser> viewers = new CopyOnWriteArrayList<>();

        int galaxyId = planet.getPosition().galaxyId();

        for (LoggedGameUser gameUser : getGameUsers()) {
            if (gameUser.getViewing() == galaxyId) {
                viewers.add(gameUser);
            }
        }

        return viewers;

    }

    public CopyOnWriteArrayList<LoggedGameUser> getPlanetViewers(long galaxyId) {

        CopyOnWriteArrayList<LoggedGameUser> viewers = new CopyOnWriteArrayList<>();

        for (LoggedGameUser gameUser : getGameUsers()) {
            if (gameUser.getViewing() == galaxyId) {
                viewers.add(gameUser);
            }
        }

        return viewers;

    }

    public CopyOnWriteArrayList<LoggedGameUser> getMatchViewers(String matchId) {

        CopyOnWriteArrayList<LoggedGameUser> viewers = new CopyOnWriteArrayList<>();

        for (LoggedGameUser gameUser : getGameUsers()) {
            if (gameUser.getMatchViewing() != null && gameUser.getMatchViewing().equals(matchId)) {
                viewers.add(gameUser);
            }
        }

        return viewers;

    }

    public static Date now() {

        return new Date();
    }

    public static LoginService getInstance() {

        return loginService;
    }

}
