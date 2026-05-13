package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.game.MatchData;
import com.go2super.obj.game.UserLeagueLeaderboard;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.construction.ResponseBuildInfoPacket;
import com.go2super.packet.fight.ResponseFightBoutBegPacket;
import com.go2super.packet.fight.ResponseFightInitBuildPacket;
import com.go2super.packet.instance.RequestEctypePacket;
import com.go2super.packet.league.RankMatch;
import com.go2super.packet.league.RequestLeagueMatchPacket;
import com.go2super.packet.league.ResponseLeagueMatchPacket;
import com.go2super.packet.match.RequestMatchInfoPacket;
import com.go2super.packet.match.RequestMatchPagePacket;
import com.go2super.packet.match.ResponseMatchInfoPacket;
import com.go2super.packet.match.ResponseMatchPage;
import com.go2super.packet.ship.ResponseGalaxyShipPacket;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.exception.BadGuidException;
import com.go2super.service.league.LeagueMatchService;
import com.go2super.service.league.LeagueRankService;
import com.go2super.service.league.LeagueTime;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatchListener implements PacketListener {

    @SneakyThrows
    @PacketProcessor
    public void onMatchInfo(RequestMatchInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        LinkedList<Packet> packets = new LinkedList<>();
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Optional<LoggedGameUser> optionalLoggedGameUser = user.getLoggedGameUser();
        if (optionalLoggedGameUser.isEmpty()) {
            return;
        }

        LoggedGameUser loggedGameUser = optionalLoggedGameUser.get();
        if (loggedGameUser.getMatchViewing() == null) {
            ResponseMatchInfoPacket responseMatchInfoPacket = getReplyMatchInfo(user);
            packets.addFirst(responseMatchInfoPacket);
            packet.reply(packets);
            var users = BattleService.getInstance().findLeagueUsersByRank(user.getCurrentLeague());
            ResponseMatchPage match = new ResponseMatchPage();
            match.setPageId(0);
            match.setDataLen(users.size());
            match.setMaxPageId(Math.max(users.size() / 10, 1));
            match.setData(users.stream().map(x -> {
                UserLeagueLeaderboard userLeagueLeaderboard = LeagueRankService.getInstance().getByGuid(x);
                User user1 = UserService.getInstance().getUserCache().findByGuid(x);
                var m = new MatchData();
                m.setName(user1.getUsername());
                m.setMatchDogFail((byte) userLeagueLeaderboard.getDraws());
                m.setMatchLost((byte) userLeagueLeaderboard.getLosses());
                m.setMatchWin((byte) userLeagueLeaderboard.getWins());
                m.setMatchResult((byte) userLeagueLeaderboard.getPoints());
                return m;
            }).limit(10).collect(Collectors.toList()));

            packet.reply(match);
            InstanceListener instance = new InstanceListener();
            var ecType = new RequestEctypePacket();
            ecType.setGuid(packet.getGuid());
            ecType.setEctypeId((short) 1000);
            ecType.socket = packet.socket;
            ecType.smartServer = packet.smartServer;
            ecType.setDataLen(UnsignedChar.of(1000));
            instance.onEctype(ecType);
            return;
        }

        MatchRunnable matchRunnable = BattleService.getInstance().getRunnable(loggedGameUser.getMatchViewing());
        if (matchRunnable == null) {
            ResponseMatchInfoPacket responseMatchInfoPacket = getReplyMatchInfo(user);
            packets.addFirst(responseMatchInfoPacket);
            packet.reply(packets);
            return;
        }
        Match current = matchRunnable.getMatch();
        if (current.isWatchMode()) {
            return;
        }

        List<BattleFleet> battleFleetList = current.getFleetsSorted();

        ResponseGalaxyShipPacket response = null;

        for (BattleFleet battleFleet : battleFleetList) {

            if (response == null) {

                response = new ResponseGalaxyShipPacket();
                response.setGalaxyId(user.getPlanet().getPosition().galaxyId());
                response.setGalaxyMapId((short) 0);
                response.setFleets(new ArrayList<>());

            } else if (response.getFleets().size() == 189) {

                packets.add(response);
                response = new ResponseGalaxyShipPacket();
                response.setGalaxyId(user.getPlanet().getPosition().galaxyId());
                response.setGalaxyMapId((short) 0);
                response.setFleets(new ArrayList<>());

            }

            GalaxyFleetInfo fleetInfo = GalaxyFleetInfo.builder()
                    .shipTeamId(battleFleet.getShipTeamId())
                    .shipNum(battleFleet.ships())
                    .bodyId((short) battleFleet.getBodyId())
                    .reserve((short) 1)
                    .direction((byte) battleFleet.getDirection())
                    .posX((byte) battleFleet.getPosX())
                    .posY((byte) battleFleet.getPosY())
                    .owner((byte) (BattleService.getInstance().getFleetColor(user, battleFleet)))
                    .build();

            response.getFleets().add(fleetInfo);
            response.setDataLen((short) response.getFleets().size());

        }

        if (response != null) {
            packets.add(response);
        }

        ResponseFightInitBuildPacket responseFightInitBuildPacket = null;
        ResponseBuildInfoPacket responseBuildInfoPacket = null;

        if (responseBuildInfoPacket != null) {
            packets.addFirst(responseBuildInfoPacket);
        }
        if (responseFightInitBuildPacket != null) {
            packets.addFirst(responseFightInitBuildPacket);
        }

        ResponseFightBoutBegPacket responseFightBoutBegPacket = new ResponseFightBoutBegPacket();
        responseFightBoutBegPacket.setGalaxyMapId(-1);
        responseFightBoutBegPacket.setGalaxyId(-1);
        responseFightBoutBegPacket.setBoutId((short) (Math.max(current.getRound(), 0)));
        packets.addFirst(responseFightBoutBegPacket);

        ResponseMatchInfoPacket responseMatchInfoPacket = getReplyMatchInfo(user);
        packets.addFirst(responseMatchInfoPacket);

        // ? First send MatchInfo
        // ? then BoutBeg
        // ? and then fleets
        user.getLoggedGameUser().get().setMatchViewing(current.getId());
        packet.reply(packets);

    }

    private ResponseMatchInfoPacket getReplyMatchInfo(User user) {
        ResponseMatchInfoPacket responseMatchInfoPacket = new ResponseMatchInfoPacket();

        LeagueTime leagueTime = LeagueMatchService.getInstance().getNextLeague(user);
        var rank = LeagueRankService.getInstance().getByGuid(user.getGuid());
        responseMatchInfoPacket.setSpareTime(leagueTime.getTime());
        responseMatchInfoPacket.setMatchWeekTop(rank.getRank());
        responseMatchInfoPacket.setReserve((short) 1);
        responseMatchInfoPacket.setMatchWin((byte) rank.getWins());
        responseMatchInfoPacket.setMatchLost((byte) rank.getLosses());
        responseMatchInfoPacket.setMatchDogfall((byte) rank.getDraws());
        responseMatchInfoPacket.setMatchLevel((byte) rank.getLeague());
        responseMatchInfoPacket.setMatchCount(user.getLeagueCount());
        if (leagueTime.isOpen()) {
            if (LeagueMatchService.getInstance().isInQueue(user.getGuid())) {
                responseMatchInfoPacket.setMatchType((byte) 6);
            } else {
                responseMatchInfoPacket.setMatchType((byte) 2);
            }
        } else {
            responseMatchInfoPacket.setMatchType((byte) (1));
        }


        return responseMatchInfoPacket;
    }

    @PacketProcessor
    public void onRequestMatchPagePacket(RequestMatchPagePacket packet) {
        if (packet.getPageId() < 0) {
            return;
        }
        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Optional<LoggedGameUser> optionalLoggedGameUser = user.getLoggedGameUser();
        if (optionalLoggedGameUser.isEmpty()) {
            return;
        }

        var users = BattleService.getInstance().findLeagueUsersByRank(user.getCurrentLeague());
        ResponseMatchPage match = new ResponseMatchPage();
        match.setPageId(packet.getPageId());
        match.setDataLen(users.size());
        match.setMaxPageId(Math.max(users.size() / 10, 1));
        match.setData(users.stream().map(x -> {
                    UserLeagueLeaderboard userLeagueLeaderboard = LeagueRankService.getInstance().getByGuid(x);
                    User user1 = UserService.getInstance().getUserCache().findByGuid(x);
                    var m = new MatchData();
                    m.setName(user1.getUsername());
                    m.setMatchDogFail((byte) userLeagueLeaderboard.getDraws());
                    m.setMatchLost((byte) userLeagueLeaderboard.getLosses());
                    m.setMatchWin((byte) userLeagueLeaderboard.getWins());
                    m.setMatchResult((byte) userLeagueLeaderboard.getPoints());
                    return m;
                })
                .skip(packet.getPageId() * 10L)
                .limit(10).toList());

        packet.reply(match);
    }

    @PacketProcessor
    public void onRequestLeagueMatchPacket(RequestLeagueMatchPacket packet) {
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Pair<Integer, List<UserLeagueLeaderboard>> page = null;
        var response = new ResponseLeagueMatchPacket();
        if (packet.getPageId() >= 0) {
            page = LeagueRankService.getInstance().getPaged(packet.getPageId());
        } else if (packet.getGuid() >= 0) {
            page = LeagueRankService.getInstance().getPagedByGuid(packet.getGuid());
        } else if (packet.getObjGuid() >= 0) {
            page = LeagueRankService.getInstance().getPagedByGuid(packet.getGuid());
        }

        if (page == null) {
            return;
        }

        response.setData(page.getValue().stream().map(x -> RankMatch.from(
                UserService.getInstance().getUserCache().findByGuid(x.getGuid()),
                x
        )).collect(Collectors.toList()));

        response.setDataLen(response.getData().size());
        response.setPageId(page.getKey());
        response.setMaxPageId((int) Math.ceil(LeagueRankService.getInstance().totalCount() / 10.0));
        packet.reply(response);
    }


}
