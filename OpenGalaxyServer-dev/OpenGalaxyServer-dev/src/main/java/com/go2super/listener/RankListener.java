package com.go2super.listener;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.obj.game.RankFightInfo;
import com.go2super.obj.game.RankUserInfo;
import com.go2super.obj.game.UserLeaderboard;
import com.go2super.obj.utility.SmartString;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.rank.*;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.RankService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class RankListener implements PacketListener {

    @PacketProcessor
    public void onRankCent(RequestRankCentPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Pair<Integer, List<UserLeaderboard>> page = null;

        if (packet.getPageId() >= 0) {
            page = RankService.getInstance().getAttackPowerRankByPageId(packet.getPageId());
        } else if (packet.getGuid() >= 0) {
            page = RankService.getInstance().getAttackPowerRankByGuid(packet.getGuid());
        } else if (packet.getObjGuid() >= 0) {
            page = RankService.getInstance().getAttackPowerRankByGuid(packet.getGuid());
        }

        if (page == null) {
            return;
        }

        List<RankUserInfo> ranking = getRanking(page.getValue(), page.getKey(), 6);
        RankUserInfo reference = new RankUserInfo();

        boolean sentMoreInfos = false;
        int size = ranking.size();

        for (RankUserInfo rankUserInfo : ranking) {
            if (PacketService.getInstance().sendMoreInfoPacket(1, rankUserInfo.getUserId(), packet)) {
                sentMoreInfos = true;
            }
        }

        while (ranking.size() < 6) {
            ranking.add(reference.trash());
        }

        ResponseRankCentPacket response = new ResponseRankCentPacket();

        response.setPageId(page.getKey());
        response.setMaxPageId(RankService.getInstance().getAttackPowerPages());

        response.setDataLen(size);
        response.setUsers(ranking);

        if (sentMoreInfos) {
            packet.reply(response);
        } else {
            packet.reply(response);
        }

    }

    @PacketProcessor
    public void onRankKillTotal(RequestRankKillTotalPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Pair<Integer, List<UserLeaderboard>> page = null;

        if (packet.getPageId() >= 0) {
            page = RankService.getInstance().getShootdownsByPageId(packet.getPageId());
        } else if (packet.getGuid() >= 0) {
            page = RankService.getInstance().getShootdownsByGuid(packet.getGuid());
        } else if (packet.getObjGuid() >= 0) {
            page = RankService.getInstance().getShootdownsByGuid(packet.getGuid());
        }

        if (page == null) {
            return;
        }

        List<RankUserInfo> ranking = getRanking(page.getValue(), page.getKey(), 6);
        RankUserInfo reference = new RankUserInfo();

        int size = ranking.size();

        while (ranking.size() < 6) {
            ranking.add(reference.trash());
        }

        ResponseRankKillTotalPacket response = new ResponseRankKillTotalPacket();

        response.setPageId(page.getKey());
        response.setMaxPageId(RankService.getInstance().getAttackPowerPages());

        response.setDataLen(size);
        response.setUsers(ranking);

        packet.reply(response);

    }

    @PacketProcessor
    public void onRankFight(RequestRankFightPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        int fightsMaxPage = RankService.getInstance().getFightsPages();
        if (fightsMaxPage <= 0) {
            return;
        }

        Pair<Integer, List<RankFightInfo>> fights = RankService.getInstance().getFightsByPageId(packet.getPageId());

        ResponseRankFightPacket response = new ResponseRankFightPacket();

        response.setFights(fights.getValue());
        response.setPageId(fights.getKey());
        response.setMaxPageId(fightsMaxPage);
        response.setDataLen(fights.getValue().size());

        packet.reply(response);

    }

    private List<RankUserInfo> getRanking(List<UserLeaderboard> leaderboards, int page, int max) {

        List<RankUserInfo> ranking = new ArrayList<>();

        int pos = page <= 0 ? 0 : page * max;

        for (UserLeaderboard userLeaderboard : leaderboards) {

            User ranked = UserService.getInstance().getUserCache().findByGuid(userLeaderboard.getGuid());

            if (ranked == null) {
                continue;
            }

            int attackPower = Long.valueOf(userLeaderboard.getAttackPower() > Integer.MAX_VALUE ? Integer.MAX_VALUE : userLeaderboard.getAttackPower()).intValue();
            int shootDowns = Long.valueOf(userLeaderboard.getShootdowns() > Integer.MAX_VALUE ? Integer.MAX_VALUE : userLeaderboard.getShootdowns()).intValue();

            RankUserInfo rankUserInfo = new RankUserInfo();

            rankUserInfo.setRankId(pos++);

            if (ranked.getConsortiaId() == -1) {

                rankUserInfo.setConsortiaId(-1);

            } else {

                Corp corp = ranked.getCorp();

                if (corp != null) {

                    rankUserInfo.setConsortiaId(corp.getCorpId());
                    rankUserInfo.setConsortiaName(SmartString.of(corp.getName(), 32));

                } else {

                    rankUserInfo.setConsortiaId(-1);

                }

            }

            rankUserInfo.setUserId(ranked.getUserId());
            rankUserInfo.setGuid(ranked.getGuid());
            rankUserInfo.setLevel(ranked.getStats().getLevel());
            rankUserInfo.getName().setValue(ranked.getUsername());
            rankUserInfo.setAssault(attackPower);
            rankUserInfo.setKillTotal(shootDowns);

            ranking.add(rankUserInfo);

        }

        return ranking;

    }

}
