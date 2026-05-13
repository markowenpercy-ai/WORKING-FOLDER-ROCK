package com.go2super.listener;

import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.fight.RequestWarfieldStatusPacket;
import com.go2super.packet.fight.ResponseWarFieldPlayerList;
import com.go2super.packet.fight.ResponseWarfieldStatusPacket;
import com.go2super.packet.fight.WarfieldPlayer;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.packet.rank.ResponseWarFieldPagePacket;
import com.go2super.packet.rank.WarFieldPage;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.battle.match.ChampMatch;
import com.go2super.service.champ.ChampService;
import com.go2super.service.exception.BadGuidException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Optional;

public class ChampionListener implements PacketListener {

    @PacketProcessor
    public void onStatus(RequestWarfieldStatusPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        ChampService champService = ChampService.getInstance();

        if (packet.getRequest().equals(UnsignedChar.of(5))) { // 5 is user page
            var findId = packet.getFindId();
            Pair<Integer, List<WarFieldPage>> pageByGuid = champService.getPageByGuid(findId);
            ResponseWarFieldPagePacket b = ResponseWarFieldPagePacket.builder()
                    .pageId(UnsignedShort.of(pageByGuid.getLeft()))
                    .pageNum(UnsignedShort.of(champService.pageSize()))
                    .data(pageByGuid.getRight())
                    .build();
            packet.reply(b);
            return;
        }


        if (packet.getRequest().equals(UnsignedChar.of(4))) { // 4 is pagination
            var page = packet.getFindId();
            ResponseWarFieldPagePacket b = ResponseWarFieldPagePacket.builder()
                    .pageId(UnsignedShort.of(page))
                    .pageNum(UnsignedShort.of(champService.pageSize()))
                    .data(champService.getByPage(page))
                    .build();
            packet.reply(b);
            return;
        }

        if (packet.getRequest().equals(UnsignedChar.of(6))) {
            int roomId = Integer.parseInt(packet.getRoomId().toString());
            Optional<ChampMatch> match;
            if (roomId == 0) {
                match = BattleService.getInstance().findChampMatchByGuid(user.getGuid());
            } else {
                match = BattleService.getInstance().findChampMatchByRoomId(roomId - 1);
            }
            if (match.isEmpty()) {
                return;
            }
            ChampMatch champMatch = match.get();
            ResponseWarFieldPlayerList response = new ResponseWarFieldPlayerList();
            response.setRoomId(packet.getRoomId());
            response.setAttackerNum(UnsignedChar.of(champMatch.getSourceIds().size()));
            response.setDataLen(UnsignedChar.of(champMatch.getSourceIds().size() + champMatch.getTargetIds().size()));
            response.setReserve(UnsignedChar.of(0));
            UserCache userCache = UserService.getInstance().getUserCache();
            for (Integer sourceId : champMatch.getSourceIds()) {
                WarfieldPlayer player = new WarfieldPlayer();
                player.setUserId(sourceId);
                player.setGuid(userCache.findByGuid(sourceId).getGuid());
                player.setName(userCache.findByGuid(sourceId).getUsername());
                response.getData().add(player);
            }
            for (Integer sourceId : champMatch.getTargetIds()) {
                WarfieldPlayer player = new WarfieldPlayer();
                player.setUserId(sourceId);
                player.setGuid(userCache.findByGuid(sourceId).getGuid());
                player.setName(userCache.findByGuid(sourceId).getUsername());
                response.getData().add(player);
            }
            packet.reply(response);
            return;
        }

        // request 3, go home
        if (packet.getRequest().equals(UnsignedChar.of(1))) {
            // Join Request
            boolean removed = ChampService.getInstance().removePlayer(packet.getGuid());
            ResponseWarfieldStatusPacket response = new ResponseWarfieldStatusPacket();
            response.setWarfield(0);
            response.setUserNumber(UnsignedShort.of(ChampService.getInstance().getWaitingRoomSize()));
            response.setStatus(removed ? (byte) -2 : (byte) -1);
            response.setMatchLevel((byte) user.getCurrentLeague());
            packet.reply(response);
            if (!removed) {
                BotLogger.error("ChampionListener: Failed to remove player from waiting room");
            }
            return;
        }
        if (packet.getRequest().equals(UnsignedChar.of(2))) {
            // View Request
            ResponseWarfieldStatusPacket response = new ResponseWarfieldStatusPacket();
            response.setWarfield(0);
            response.setUserNumber(UnsignedShort.of(ChampService.getInstance().getWaitingRoomSize()));
            response.setStatus((byte) 0);
            response.setMatchLevel((byte) user.getCurrentLeague());


            Optional<LoggedGameUser> loggedGameUser = user.getLoggedGameUser();
            if (loggedGameUser.isPresent()) {
                int roomId = Integer.parseInt(packet.getRoomId().toString());
                Optional<ChampMatch> match;
                if (roomId == 0) {
                    match = BattleService.getInstance().findChampMatchByGuid(user.getGuid());
                } else {
                    match = BattleService.getInstance().findChampMatchByRoomId(roomId - 1);
                }
                match.ifPresent(champMatch -> {
                    loggedGameUser.get().setMatchViewing(champMatch.getId());
                    response.setWarfield(ChampService.getInstance().getCurrentWarFieldBitMask());
                    ResponseEctypeStatePacket state = new ResponseEctypeStatePacket();
                    state.setEctypeId((short) 1002);
                    state.setGateId(UnsignedChar.of(0));
                    state.setState((byte) 1);
                    packet.reply(response, state);
                });
                return;
            }
            return;
        }
        if (packet.getRequest().equals(UnsignedChar.of(3))) {
            ResponseEctypeStatePacket response2 = new ResponseEctypeStatePacket();
            response2.setEctypeId((short) 1002);
            response2.setGateId(UnsignedChar.of(0));
            response2.setState((byte) 0);
            packet.reply(response2);
            return;
        }

        ResponseWarfieldStatusPacket response = new ResponseWarfieldStatusPacket();
        response.setMatchLevel((byte) user.getCurrentLeague());
        response.setWarfield(0);
        response.setUserNumber(UnsignedShort.of(ChampService.getInstance().getWaitingRoomSize()));
        // -2 sign up
        // -1 cancel
        //  0 observe
        boolean isSignedUp = ChampService.getInstance().isPlayerInChampWaitingRoom(packet.getGuid());
        if (!isSignedUp) {
            boolean inChampMatch = ChampService.getInstance().isPlayerInChampMatch(packet.getGuid());
            if (inChampMatch) {
                Optional<LoggedGameUser> loggedGameUser = user.getLoggedGameUser();
                if (loggedGameUser.isPresent()) {
                    Optional<ChampMatch> match = BattleService.getInstance().findChampMatchByGuid(user.getGuid());
                    match.ifPresent(champMatch -> {
                        loggedGameUser.get().setMatchViewing(champMatch.getId());
                        response.setStatus((byte) 0);
                        response.setWarfield(ChampService.getInstance().getCurrentWarFieldBitMask());
                    });
                }
            } else {
                response.setStatus((byte) -2);
            }
        } else {
            response.setStatus((byte) -1);
        }
        packet.reply(response);
    }

}
