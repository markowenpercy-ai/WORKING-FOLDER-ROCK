package com.go2super.service.league;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.Email;
import com.go2super.database.entity.sub.EmailGood;
import com.go2super.database.entity.sub.UserEmailStorage;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.resources.ResourceManager;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.match.LeagueMatch;
import com.go2super.service.battle.type.StopCause;
import com.go2super.socket.util.DateUtil;
import lombok.Data;
import lombok.Getter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Getter
@Service
@EnableScheduling
public class LeagueMatchService {

    @Getter
    private static LeagueMatchService instance;

    public LeagueMatchService() {
        instance = this;
    }

    public static boolean isInQueue(Integer guid) {
        Optional<LeagueMatch> match = BattleService.getInstance().findLeagueByGuid(guid);
        return match.isPresent();
    }


    public boolean addPlayer(Integer id, List<Integer> fleetIds) {
        if (isInQueue(id)) {
            return false;
        }
        User player1 = UserService.getInstance().getUserCache().findByUserId(id);
        if ((player1.getLeagueCount() - 1) < 0 || player1.getResources().getGold() < 30_000 || fleetIds.isEmpty()) {
            return false;
        }

        Optional<LeagueMatch> guid = BattleService.getInstance().findLeagueByGuid(id);
        if (guid.isPresent()) {
            return false;
        }

        Optional<LeagueMatch> waiting = BattleService.getInstance().findEmptyLeagueRoom(player1.getCurrentLeague());
        if (waiting.isEmpty()) {
            MatchRunnable m = BattleService.getInstance().makeLeagueMatch(player1, fleetIds);
            return m != null;
        }
        LeagueMatch match = waiting.get();
        match.addOpponent(player1, fleetIds, match.getSourceGuid() != -1);
        return true;
    }

    public void removePlayer(Integer id) {
        if (!isInQueue(id)) {
            return;
        }
        Optional<LeagueMatch> match = BattleService.getInstance().findLeagueByGuid(id);
        if (match.isEmpty()) {
            return;
        }
        match.get().exit(id);
        if (match.get().getSourceGuid() == -1 && match.get().getTargetGuid() == -1) {
            BattleService.getInstance().stopMatch(match.get(), StopCause.MANUAL);
        }
    }

    @Scheduled(cron = "0 */3 * * * *")
    public void startMatches() {
        LocalDateTime now = LocalDateTime.now();
        Map<Integer, List<LeagueMatch>> rank = BattleService.getInstance().getLeaguesWaitingByRank();
        for (Map.Entry<Integer, List<LeagueMatch>> entry : rank.entrySet()) {
            List<LeagueMatch> matches = entry.getValue();
            for (LeagueMatch match : matches) {
                Optional<LoggedGameUser> target = LoginService.getInstance().getGame(match.getTargetGuid());
                Optional<LoggedGameUser> source = LoginService.getInstance().getGame(match.getSourceGuid());
                if (match.getTargetGuid() == -1 || match.getSourceGuid() == -1) {
                    if (!isOpen(entry.getKey(), now)) {
                        BattleService.getInstance().stopMatch(match, StopCause.MANUAL);
                        continue;
                    }
                    if (target.isEmpty()) {
                        match.exit(match.getTargetGuid());
                    }
                    if (source.isEmpty()) {
                        match.exit(match.getSourceGuid());
                    }
                    if (source.isEmpty() && target.isEmpty()) {
                        BattleService.getInstance().stopMatch(match, StopCause.MANUAL);
                    }
                    continue;
                }

                boolean started = match.start();
                if (!started) {
                    if (target.isEmpty()) {
                        match.exit(match.getTargetGuid());
                    }
                    if (source.isEmpty()) {
                        match.exit(match.getSourceGuid());
                    }
                    if (source.isEmpty() && target.isEmpty()) {
                        BattleService.getInstance().stopMatch(match, StopCause.MANUAL);
                    }
                    continue;
                }

                User player1 = UserService.getInstance().getUserCache().findByUserId(match.getTargetGuid());
                User player2 = UserService.getInstance().getUserCache().findByUserId(match.getSourceGuid());
                player1.setLeagueCount((byte) (player1.getLeagueCount() - 1));
                player1.getResources().setGold(player1.getResources().getGold() - 30_000);
                player1.save();

                player2.setLeagueCount((byte) (player2.getLeagueCount() - 1));
                player2.getResources().setGold(player2.getResources().getGold() - 30_000);
                player2.save();
            }
        }
    }

    public void sendMailReward(User player) {
        UserEmailStorage userEmailStorage = player.getUserEmailStorage();
        Email email = Email.builder()
                .autoId(userEmailStorage.nextAutoId())
                .type(2)
                .name("System")
                .readFlag(0)
                .date(DateUtil.now())
                .goods(new ArrayList<>())
                .guid(-1)
                .build();
        email.setSubject("League Result");
        email.setEmailContent("Here is your league reward and you earned " + ((player.getStats().getLeagueStats().getLeague() + 1) * 10) + " champ points!");
        //honor
        email.addGood(EmailGood.builder()
                .goodId(ResourceManager.getProp("prop:other.honorEmblem").getId())
                .lockNum(ResourceManager.getLeaguesJson().lookup(player.getStats().getLeagueStats().getLeague() + 1).getDailyHonor())
                .build());
        //champ points
        player.getResources().addChampionPoints((player.getStats().getLeagueStats().getLeague() + 1) * 10);
        userEmailStorage.addEmail(email);
        player.setToSave(true);
        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(player);
        if (gameUserOptional.isPresent()) {
            LoggedGameUser loggedGameUser = gameUserOptional.get();
            ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();
            loggedGameUser.getSmartServer().send(response);
        }
    }

    public LeagueTime getNextLeague(User user) {
        LocalDateTime currentTime = LocalDateTime.now();
        var leagueData = ResourceManager.getLeaguesJson().lookup(user.getStats().getLeagueStats().getLeague() + 1);
        LocalTime period1Start = LocalTime.parse(leagueData.getPeriod1Start());
        LocalTime period1End = LocalTime.parse(leagueData.getPeriod1End());
        LocalTime period2Start = LocalTime.parse(leagueData.getPeriod2Start());
        LocalTime period2End = LocalTime.parse(leagueData.getPeriod2End());

        LeagueTime l = new LeagueTime();
        l.setOpen(isLeagueOpen(user, currentTime));
        LocalDateTime next = getNextLeagueTime(currentTime, period1Start, period1End, period2Start, period2End);
        int time = (int) Duration.between(currentTime, next).get(ChronoUnit.SECONDS);
        l.setTime(time);
        return l;
    }

    public boolean isLeagueOpen(User user, LocalDateTime currentTime) {
        var leagueData = ResourceManager.getLeaguesJson().lookup(user.getStats().getLeagueStats().getLeague() + 1);
        LocalTime period1Start = LocalTime.parse(leagueData.getPeriod1Start());
        LocalTime period1End = LocalTime.parse(leagueData.getPeriod1End());
        LocalTime period2Start = LocalTime.parse(leagueData.getPeriod2Start());
        LocalTime period2End = LocalTime.parse(leagueData.getPeriod2End());
        return isLeagueOpen(currentTime.toLocalTime(), period1Start, period1End, period2Start, period2End);
    }

    public boolean isOpen(int league, LocalDateTime currentTime) {
        var leagueData = ResourceManager.getLeaguesJson().lookup(league + 1);
        LocalTime period1Start = LocalTime.parse(leagueData.getPeriod1Start());
        LocalTime period1End = LocalTime.parse(leagueData.getPeriod1End());
        LocalTime period2Start = LocalTime.parse(leagueData.getPeriod2Start());
        LocalTime period2End = LocalTime.parse(leagueData.getPeriod2End());
        return isLeagueOpen(currentTime.toLocalTime(), period1Start, period1End, period2Start, period2End);
    }

    private static LocalDateTime getNextLeagueTime(LocalDateTime currentTime, LocalTime period1Start, LocalTime period1End, LocalTime period2Start, LocalTime period2End) {
        List<LocalTime> times = List.of(period1Start, period1End, period2Start, period2End);
        Optional<LocalTime> closestPositive = times.stream().filter(t -> t.isAfter(currentTime.toLocalTime())).min(LocalTime::compareTo);
        if (closestPositive.isPresent()) {
            return LocalDateTime.of(LocalDate.now(), closestPositive.get());
        }
        Optional<LocalTime> closestPositive2 = times.stream().filter(t -> t.isBefore(currentTime.toLocalTime())).min(LocalTime::compareTo);
        LocalTime closest = closestPositive.orElse(closestPositive2.orElse(period1Start));
        return LocalDateTime.of(LocalDate.now().plusDays(1), closest);
    }

    private static boolean isLeagueOpen(LocalTime currentTime, LocalTime period1Start, LocalTime period1End, LocalTime period2Start, LocalTime period2End) {
        return (currentTime.isAfter(period1Start) && currentTime.isBefore(period1End)) ||
                (currentTime.isAfter(period2Start) && currentTime.isBefore(period2End)) ||
                (period2End.isBefore(period2Start) && (currentTime.isBefore(period2End) || currentTime.isAfter(period2Start)));
    }
}
