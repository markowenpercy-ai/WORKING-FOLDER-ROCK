package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.type.MatchType;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.ArenaPageInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.fight.RequestArenaPagePacket;
import com.go2super.packet.fight.RequestArenaStatusPacket;
import com.go2super.packet.fight.ResponseArenaPagePacket;
import com.go2super.packet.fight.ResponseArenaStatusPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.match.ArenaMatch;
import com.go2super.service.battle.type.StopCause;
import com.go2super.service.exception.BadGuidException;

import java.util.*;

public class ArenaListener implements PacketListener {

    @PacketProcessor
    public void onArenaStatus(RequestArenaStatusPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Optional<LoggedGameUser> optionalLoggedGameUser = user.getLoggedGameUser();
        if (optionalLoggedGameUser.isEmpty()) {
            return;
        }

        LoggedGameUser loggedGameUser = optionalLoggedGameUser.get();
        Optional<ArenaMatch> optionalArenaMatch;

        if (packet.getRoomId() == -1) {

            Optional<Match> optionalMatch = BattleService.getInstance().getVirtual(user.getGuid());

            if (optionalMatch.isEmpty()) {

                if (loggedGameUser.getMatchViewing() == null) {
                    return;
                }

                MatchRunnable matchRunnable = BattleService.getInstance().getRunnable(loggedGameUser.getMatchViewing());
                if (matchRunnable == null) {
                    return;
                }

                Match match = matchRunnable.getMatch();
                if (match.getMatchType() != MatchType.ARENA_MATCH) {
                    return;
                }

                optionalArenaMatch = Optional.of((ArenaMatch) match);

            } else {

                Match match = optionalMatch.get();
                if (match.getMatchType() != MatchType.ARENA_MATCH) {
                    return;
                }

                optionalArenaMatch = Optional.of((ArenaMatch) match);

            }

        } else {
            optionalArenaMatch = BattleService.getInstance().getArenaByRoomId(packet.getRoomId());
        }

        if (!optionalArenaMatch.isPresent()) {
            return;
        }

        ArenaMatch arenaMatch = optionalArenaMatch.get();

        //
        //
        // 2 = Leave room
        int request = packet.getRequest().getValue();

        BotLogger.log("AR REQUEST: " + request);

        if (request == 0) {

            // Has to be a participant of the arena match to execute this request
            if (packet.getGuid() != arenaMatch.getSourceGuid() && packet.getGuid() != arenaMatch.getTargetGuid()) {
                return;
            }

            if (!arenaMatch.getPause().get()) {
                return;
            }
            if (arenaMatch.getSourceGuid() != user.getGuid()) {
                return;
            }

            boolean started = arenaMatch.start();

            if (!started) {

                ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

                status.setGuid(user.getGuid());
                status.setCName(SmartString.of(user.getUsername(), 32));

                status.setRoomId(user.getGuid());
                status.setRequest(UnsignedChar.of(2));

                status.setStatus((byte) 1);
                packet.reply(status);
                return;

            }

            ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

            status.setGuid(user.getGuid());
            status.setCName(SmartString.of(user.getUsername(), 32));

            status.setRoomId(user.getGuid());
            status.setRequest(UnsignedChar.of(5));

            status.setStatus((byte) 1);

            Optional<LoggedGameUser> optionalOwner = arenaMatch.getOptionalOwner();
            Optional<LoggedGameUser> optionalOpponent = arenaMatch.getOptionalOpponent();

            // if(optionalOwner.isPresent()) optionalOwner.get().getSmartServer().send(status);
            // if(optionalOpponent.isPresent()) optionalOpponent.get().getSmartServer().send(status);

        } else if (request == 1) {

            // Has to be a participant of the arena match to execute this request
            if (packet.getGuid() != arenaMatch.getSourceGuid() && packet.getGuid() != arenaMatch.getTargetGuid()) {
                return;
            }

            if (!arenaMatch.getPause().get()) {
                return;
            }
            if (arenaMatch.getSourceGuid() != user.getGuid()) {
                return;
            }

            arenaMatch.opponentExit(true);

        } else if (request == 2) {

            // Has to be a participant of the arena match to execute this request
            if (packet.getGuid() != arenaMatch.getSourceGuid() && packet.getGuid() != arenaMatch.getTargetGuid()) {
                return;
            }
            if (!arenaMatch.getPause().get()) {
                return;
            }

            // Owner
            if (arenaMatch.getSourceGuid() == user.getGuid()) {
                BattleService.getInstance().stopMatch(arenaMatch, StopCause.MANUAL);
            } else {
                arenaMatch.opponentExit(false);
            }

            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

            response.setEctypeId((short) 1001);
            response.setGateId(UnsignedChar.of(1));
            response.setState((byte) 0);

            packet.reply(response);

            ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

            status.setGuid(user.getGuid());
            status.setCName(SmartString.of(user.getUsername(), 32));

            status.setRoomId(user.getGuid());
            status.setRequest(UnsignedChar.of(2));

            status.setStatus((byte) 1);
            packet.reply(status);

        } else if (request == 3) {

            // Has to be a participant of the arena match to execute this request
            if (packet.getGuid() != arenaMatch.getSourceGuid() && packet.getGuid() != arenaMatch.getTargetGuid()) {
                return;
            }

            boolean hasStopBefore = arenaMatch.getSourceGuid() == packet.getGuid() ? arenaMatch.isSourceStop() : arenaMatch.isTargetStop();
            if (hasStopBefore) {
                return;
            }

            Optional<LoggedGameUser> optionalOwner = arenaMatch.getOptionalOwner();
            Optional<LoggedGameUser> optionalOpponent = arenaMatch.getOptionalOpponent();

            if (optionalOpponent.isEmpty() || optionalOpponent.isEmpty()) {
                return;
            }

            ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

            status.setGuid(user.getGuid());
            status.setCName(SmartString.of(user.getUsername(), 32));

            status.setRoomId(user.getGuid());
            status.setRequest(UnsignedChar.of(3));

            status.setStatus((byte) 1);

            if (arenaMatch.getSourceGuid() == packet.getGuid()) {

                arenaMatch.setSourceStop(true);

                if (arenaMatch.isTargetStop()) {

                    BattleService.getInstance().stopMatch(arenaMatch, StopCause.MANUAL);
                    return;

                }

                optionalOpponent.get().getSmartServer().send(status);

            } else {

                arenaMatch.setTargetStop(true);

                if (arenaMatch.isSourceStop()) {

                    BattleService.getInstance().stopMatch(arenaMatch, StopCause.MANUAL);
                    return;

                }

                optionalOwner.get().getSmartServer().send(status);

            }

        } else if (request == 4) {

            // Has to be a participant of the arena match to execute this request
            if (packet.getGuid() != arenaMatch.getSourceGuid() && packet.getGuid() != arenaMatch.getTargetGuid()) {
                return;
            }

            Optional<LoggedGameUser> optionalOwner = arenaMatch.getOptionalOwner();
            Optional<LoggedGameUser> optionalOpponent = arenaMatch.getOptionalOpponent();

            if (optionalOpponent.isEmpty() || optionalOpponent.isEmpty()) {
                return;
            }

            ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

            status.setGuid(user.getGuid());
            status.setCName(SmartString.of(user.getUsername(), 32));

            status.setRoomId(user.getGuid());
            status.setRequest(UnsignedChar.of(4));

            status.setStatus((byte) 1);

            if (arenaMatch.getSourceGuid() == packet.getGuid()) {

                arenaMatch.setSourceStop(true);
                optionalOpponent.get().getSmartServer().send(status);

            } else {

                arenaMatch.setTargetStop(true);
                optionalOwner.get().getSmartServer().send(status);

            }

        } else if (request == 5) {

            if (arenaMatch.getPause().get()) {
                return;
            }
            loggedGameUser.setMatchViewing(arenaMatch.getId());

            ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

            status.setGuid(arenaMatch.getSourceGuid());
            status.setCName(SmartString.of(user.getUsername(), 32));

            status.setRoomId(arenaMatch.getSourceGuid());
            status.setRequest(UnsignedChar.of(5));

            status.setStatus((byte) 1);
            packet.reply(status);

            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

            response.setEctypeId((short) 1000);
            response.setGateId(UnsignedChar.of(0));
            response.setState((byte) 1); // 1 show stop button, 0 hide stop button

            packet.reply(response);

        } else if (request == 6) {

            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

            response.setEctypeId((short) arenaMatch.getEctype());
            response.setGateId(UnsignedChar.of(0));
            response.setState((byte) 2);

            packet.reply(response);

            ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

            status.setGuid(user.getGuid());
            status.setCName(SmartString.of(user.getUsername(), 32));

            status.setRoomId(user.getGuid());
            status.setRequest(UnsignedChar.of(5));

            status.setStatus((byte) 1);
            packet.reply(status);

        }

    }

    @PacketProcessor
    public void onArenaPage(RequestArenaPagePacket packet) throws BadGuidException {

        // BotLogger.log((int) packet.getArenaFlag().getValue());
        // BotLogger.log(packet.getPageId().getValue());

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        boolean waiting = packet.getArenaFlag().getValue() == 0;

        List<List<ArenaMatch>> arenas = BattleService.getInstance().getArenasPages(waiting);

        if (arenas.isEmpty()) {

            ResponseArenaPagePacket response = new ResponseArenaPagePacket();

            response.setPageId(packet.getPageId());
            response.setPageNum(UnsignedShort.of(0));

            response.setArenaFlag(packet.getArenaFlag());

            packet.reply(response);
            return;

        }

        if (packet.getPageId().getValue() < 0 || packet.getPageId().getValue() >= arenas.size()) {
            return;
        }

        List<ArenaMatch> page = arenas.get(packet.getPageId().getValue());

        ResponseArenaPagePacket response = new ResponseArenaPagePacket();

        response.setPageId(packet.getPageId());
        response.setPageNum(UnsignedShort.of(arenas.size()));

        response.setArenaFlag(packet.getArenaFlag());

        for (ArenaMatch arenaMatch : page) {

            ArenaPageInfo arenaPageInfo = new ArenaPageInfo();

            arenaPageInfo.setSourceUserId(arenaMatch.getSourceUserId());
            arenaPageInfo.setTargetUserId(arenaMatch.getTargetUserId());

            arenaPageInfo.setSourceName(arenaMatch.getSourceUsername());
            arenaPageInfo.setTargetName(arenaMatch.getTargetUsername());

            arenaPageInfo.setSourceGuid(arenaMatch.getSourceGuid());
            arenaPageInfo.setTargetGuid(arenaMatch.getTargetGuid());

            arenaPageInfo.setSourceShipNum(arenaMatch.getSourceSend());
            arenaPageInfo.setTargetShipNum(arenaMatch.getTargetSend());

            arenaPageInfo.setPassKey(arenaMatch.getPassword() == -1 ? 0 : 1);

            response.getArenas().add(arenaPageInfo);

        }

        response.setDataLen(UnsignedChar.of(response.getArenas().size()));
        packet.reply(response);

    }

}
