package com.go2super.listener;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserIglStats;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.RacingEnemyInfo;
import com.go2super.obj.game.RacingShipTeamInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.custom.CustomWarScorePacket;
import com.go2super.packet.igl.*;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.service.*;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.exception.BadGuidException;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IGLListener implements PacketListener {

    @PacketProcessor
    public void onJoinRacing(RequestJoinRacingPacket packet) throws BadGuidException {
        System.out.println("RequestJoinRacingPacket");
    }

    @PacketProcessor
    public void onRacingBattle(RequestRacingBattlePacket packet) throws BadGuidException {
        if (!IGLService.getInstance().getEnabled().get()) {
            return;
        }

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }
        User user2 = UserService.getInstance().getUserCache().findByUserId(packet.getObjUserId());
        if (user2 == null) {
            return;
        }


        UserIglStats iglStats = user.getStats().getIglStats();
        if (iglStats == null) {
            BotLogger.info("User " + user.getUsername() + " does not have IGL stats");
            return;
        }

        var resp = new ResponseRacingBattlePacket();

        if (iglStats.getEntries() < 0 || iglStats.getEntries() > 15 || packet.getGuid() == packet.getObjUserId()) {
            resp.setErrorCode(1);
            packet.reply(resp);
            return;
        }

        if (iglStats.getEntries() > 10) {
            user.getResources().setMallPoints(user.getResources().getMallPoints() - 10);
        }

        resp.setErrorCode(0);
        iglStats.setEntries(iglStats.getEntries() + 1);
        user.save();

        resp.setUserId(packet.getObjUserId());
        MatchRunnable match = BattleService.getInstance().makeIglMatch(user, user2, IGLService.getInstance().getFleetIds(user.getUserId()),
                IGLService.getInstance().getFleetIds(user2.getUserId()));
        if (match == null) {
            return;
        }

        ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
        Match current = match.getMatch();
        Optional<LoggedGameUser> optionalLoggedGameUser = user.getLoggedGameUser();
        if (optionalLoggedGameUser.isEmpty()) {
            return;
        }

        LoggedGameUser loggedGameUser = optionalLoggedGameUser.get();
        loggedGameUser.setMatchViewing(current.getId());
        response.setEctypeId((short) 1002);
        response.setGateId(UnsignedChar.of(0));
        response.setState((byte) 1);

        ResponseDuplicateStatusPacket duplicateStatusPacket = new ResponseDuplicateStatusPacket();
        duplicateStatusPacket.setSeqId(0);
        duplicateStatusPacket.setStatus(UnsignedChar.of(1));
        duplicateStatusPacket.setGuid(packet.getGuid());
        duplicateStatusPacket.setDuplicate(UnsignedChar.of(50));

        packet.reply(resp, response, duplicateStatusPacket);
    }


    @PacketProcessor
    public void onRequestRacingRankPacket(RequestRacingRankPacket packet) throws BadGuidException {
        if (!IGLService.getInstance().getEnabled().get()) {
            return;
        }
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Pair<Integer, List<RacingRank>> rankList;

        if (packet.getPageId() >= 0) {
            rankList = IGLService.getInstance().getIglRankPage(packet.getPageId());
        } else if (packet.getGuid() >= 0) {
            rankList = IGLService.getInstance().getIglRankPageByGuid(packet.getGuid());
        } else {
            rankList = IGLService.getInstance().getIglRankPage(0);
        }

        List<RacingRank> responseList = new ArrayList<>();
        int count = IGLService.getInstance().totalCount();
        for (RacingRank r : rankList.getRight()) {
            responseList.add(RacingRank
                    .builder()
                    .name(r.getName())
                    .rankId(r.getRankId() + 1)
                    .userId(r.getUserId())
                    .gameServerId(0)
                    .build());
        }

        var response = new ResponseRacingRankPacket();
        response.setUserId(user.getUserId());
        response.setPageId(rankList.getLeft());
        response.setUserCount(count);
        response.setDataLen(responseList.size());
        response.setRankList(responseList);
        packet.reply(response);
    }

    @PacketProcessor
    public void onRequestSetRacingShipTeamPacket(RequestSetRacingShipTeamPacket packet) throws BadGuidException {
        if (!IGLService.getInstance().getEnabled().get()) {
            return;
        }
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        ResponseSetRacingShipTeamPacket response = new ResponseSetRacingShipTeamPacket();
        response.setShipTeamLen(packet.getShipTeamLen());
        response.setShips(packet.getShips());
        IGLService.getInstance().addFleetIds(packet.getGuid(), packet.getShips());
        packet.reply(response);
    }

    @PacketProcessor
    public void onClaim(RequestRacingAwardPacket packet) throws BadGuidException {
        if (!IGLService.getInstance().getEnabled().get()) {
            return;
        }
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        if (user.getStats().isCollectedPoints()) {
            return;
        }

        if (user.getBuildings().getBuilding("build:galaxyTransporter") == null) {
            return;
        }

        user.getStats().setCollectedPoints(true);

        if (user.getStats().getUserIglStats() == null) {
            IGLService.getInstance().registerUser(user);
        }

        RacingRank byGuid = IGLService.getInstance().findByGuid(user);
        UserInventory inventory = user.getInventory();

        int points;
        int rankId = byGuid.getRankId() + 1;
        if (rankId <= 10) {
            points = 150;
            inventory.addProp(4458, 1, 0, true);
        } else if (rankId <= 30) {
            points = 120;
        } else if (rankId <= 110) {
            points = 100;
        } else if (rankId <= 210) {
            points = 80;
        } else if (rankId <= 350) {
            points = 70;
        } else if (rankId <= 550) {
            points = 60;
        } else if (rankId <= 1000) {
            points = 30;
        } else {
            points = 10;
        }
        user.getResources().setChampionPoints(user.getResources().getChampionPoints() + points);
        user.update();
        user.save();

        ResponseRacingAwardPacket response = new ResponseRacingAwardPacket();
        response.setAmount(UnsignedInteger.of(points));

        CustomWarScorePacket warScore = new CustomWarScorePacket();
        warScore.setPoints(Long.valueOf(user.getResources().getChampionPoints()).intValue());

        packet.reply(response, warScore);

    }

    @PacketProcessor
    public void onInformation(RequestRacingInformationPacket packet) throws BadGuidException {
        if (!IGLService.getInstance().getEnabled().get()) {
            return;
        }
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        ResponseRacingInformationPacket response = new ResponseRacingInformationPacket();
        if (user.getStats().getIglStats() == null) {
            IGLService.getInstance().registerUser(user);
        }

        RacingRank byGuid = IGLService.getInstance().findByGuid(user);

        response.setRewardValue(0);
        response.setRacingNum(UnsignedChar.of(user.getStats().getIglStats().getEntries()));
        response.setRacingRewardFlag((byte) (user.getStats().isCollectedPoints() ? 1 : 0));
        response.setUserId(user.getUserId());
        response.setEnemyInfo(new ArrayList<>());

        List<RacingRank> iglPageByGuid = IGLService.getInstance().getIglPageByGuid(packet.getGuid());
        for (RacingRank rank : iglPageByGuid) {
            if (rank.getUserId() == user.getUserId()) {
                response.setRankId(UnsignedInteger.of(rank.getRankId() + 1));
                continue;
            }
            response.getEnemyInfo().add(new RacingEnemyInfo(rank.getUserId(), rank.getRankId() + 1, 0, rank.getName()));
        }
        response.setReportInfo(IGLService.getInstance().getRacingReportInfo(user.getUserId()));
        response.setReportLen(UnsignedChar.of(response.getReportInfo().size()));
        response.setEnemyLen(UnsignedChar.of(response.getEnemyInfo().size()));
        List<Integer> shipsInIgl = IGLService.getInstance().getFleetIds(user.getUserId());
        List<Fleet> fleets = PacketService.getInstance().getFleetCache().findAllByGuid(packet.getGuid());

        // send available fleets // kind = 1
        List<ResponseRacingShipTeamInfoPacket> shipResponses = new ArrayList<>();

        List<Fleet> collect = fleets.stream().filter(x -> !shipsInIgl.contains(x.getShipTeamId())).collect(Collectors.toList());
        Lists.partition(collect, 19).forEach(fleetList -> {
            ResponseRacingShipTeamInfoPacket shipResponse = new ResponseRacingShipTeamInfoPacket();
            shipResponse.setKind((byte) 1); // 0 = selected fleets, 1 = available fleets
            shipResponse.setShipTeamInfos(new ArrayList<>());
            for (Fleet fleet : fleetList) {
                var rcs = new RacingShipTeamInfo();
                rcs.setTeamName(fleet.getName());
                rcs.setShipTeamId(fleet.getShipTeamId());
                rcs.setCommanderId(fleet.getCommanderId());
                rcs.setBodyId(fleet.getBodyId());
                rcs.setShipNum(fleet.ships());
                shipResponse.getShipTeamInfos().add(rcs);
            }
            shipResponse.setDataLen((byte) shipResponse.getShipTeamInfos().size());
            shipResponses.add(shipResponse);
        });

        // send available fleets // kind = 0
        ResponseRacingShipTeamInfoPacket shipResponse2 = new ResponseRacingShipTeamInfoPacket();
        shipResponse2.setKind((byte) 0); // 0 = selected fleets, 1 = available fleets
        shipResponse2.setShipTeamInfos(new ArrayList<>());
        for (Fleet fleet : fleets) {
            if (!shipsInIgl.contains(fleet.getShipTeamId())) {
                continue;
            }
            var rcs = new RacingShipTeamInfo();
            rcs.setTeamName(fleet.getName());
            rcs.setShipTeamId(fleet.getShipTeamId());
            rcs.setCommanderId(fleet.getCommanderId());
            rcs.setBodyId(fleet.getBodyId());
            rcs.setShipNum(fleet.ships());
            shipResponse2.getShipTeamInfos().add(rcs);
        }
        shipResponse2.setDataLen((byte) shipResponse2.getShipTeamInfos().size());

        packet.reply(response);
        shipResponses.forEach(packet::reply);
        packet.reply(shipResponse2);

    }

}
