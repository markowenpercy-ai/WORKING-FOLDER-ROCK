package com.go2super.listener;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.entity.type.MatchType;
import com.go2super.logger.BotLogger;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.GameInstance;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.fight.ResponseArenaStatusPacket;
import com.go2super.packet.fight.ResponseWarfieldStatusPacket;
import com.go2super.packet.instance.RequestEctypeInfoPacket;
import com.go2super.packet.instance.RequestEctypePacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.service.*;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.match.ArenaMatch;
import com.go2super.service.battle.type.StopCause;
import com.go2super.service.champ.ChampService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.service.league.LeagueMatchService;
import com.go2super.service.raids.RaidStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstanceListener implements PacketListener {

    @PacketProcessor
    public void onEctype(RequestEctypePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getDataLen().getValue() == 0) {
            return;
        }

        UserPlanet planet = user.getPlanet();
        if (planet == null || planet.isInWar()) {
            return;
        }

        Optional<Match> optionalCurrent = BattleService.getInstance().getVirtual(user.getGuid());
        if (optionalCurrent.isPresent() && !optionalCurrent.get().getMatchType().equals(MatchType.CHAMPION_MATCH)) {
            return;
        }
        ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

        if (packet.getActivity() == (byte) 2) {
            // Champs Qualifier join fleets
            ResponseWarfieldStatusPacket response2 = new ResponseWarfieldStatusPacket();
            response2.setWarfield(0);
            response2.setStatus((byte) -1);
            response2.setMatchLevel((byte) user.getCurrentLeague());

            List<Integer> fleetIds = new ArrayList<>();
            for (int i = 0; i < packet.getDataLen().getValue(); i++) {
                fleetIds.add(packet.getShips().getArray()[i]);
            }
            boolean added = ChampService.getInstance().addPlayer(user.getGuid(), fleetIds);
            response2.setStatus(added ? (byte) -1 : (byte) -2);
            response2.setUserNumber(UnsignedShort.of(ChampService.getInstance().getWaitingRoomSize()));
            packet.reply(response2);
            return;
        }


        switch (packet.getEctypeId()) {
            case 1000 -> {
                response.setEctypeId(packet.getEctypeId());
                response.setGateId(UnsignedChar.of(0));
                List<GameBattle> league = BattleService.getInstance().getBattles(MatchType.LEAGUE_MATCH);
                if (!LeagueMatchService.isInQueue(packet.getGuid())) {
                    if (packet.getDataLen().getValue() > 0 && packet.getDataLen().getValue() <= 4) {
                        List<Integer> fleetIds = new ArrayList<>();
                        for (int i = 0; i < packet.getDataLen().getValue(); i++) {
                            fleetIds.add(packet.getShips().getArray()[i]);
                        }
                        if (fleetIds.size() > 4 || fleetIds.size() < 1) {
                            response.setState((byte) 0);
                            packet.reply(response);
                            return;
                        }
                        if (!LeagueMatchService.getInstance().addPlayer(packet.getGuid(), fleetIds)) {
                            response.setState((byte) 0);
                        } else {
                            response.setState((byte) 4);
                        }
                    } else {
                        response.setState((byte) 0);
                    }
                } else if (league.stream().anyMatch(x -> x.getMatch().getFleets().stream().anyMatch(y -> y.getGuid() == packet.getGuid()))) {
                    response.setState((byte) 2);
                } else {
                    response.setState((byte) 4);
                }
                packet.reply(response);
            }
            case 1001 -> {
                if (packet.getRoomId() == -1) {

                    List<Integer> fleetIds = new ArrayList<>();

                    for (int i = 0; i < packet.getDataLen().getValue(); i++) {
                        fleetIds.add(packet.getShips().getArray()[i]);
                    }

                    if (fleetIds.size() > 60 || fleetIds.size() < 1) {
                        return;
                    }

                    MatchRunnable runnable = BattleService.getInstance().makeArenaMatch(user, fleetIds, packet.getPassKey());
                    if (runnable == null) {
                        return;
                    }

                    response.setEctypeId(packet.getEctypeId());
                    response.setGateId(UnsignedChar.of(1));
                    response.setState((byte) 4);

                    packet.reply(response);

                    ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

                    status.setGuid(user.getGuid());
                    status.setCName(SmartString.of(user.getUsername(), 32));

                    status.setRoomId(user.getGuid());
                    status.setRequest(UnsignedChar.of(100));

                    status.setStatus((byte) 1);
                    packet.reply(status);

                } else if (packet.getRoomId() != user.getGuid()) {

                    Optional<ArenaMatch> optionalMatch = BattleService.getInstance().getArenaByOwner(packet.getRoomId(), true);

                    if (optionalMatch.isEmpty()) {
                        return;
                    }

                    ArenaMatch arenaMatch = optionalMatch.get();

                    if (arenaMatch.getTargetGuid() != -1) {
                        return;
                    }

                    if (arenaMatch.getPassword() != -1 && arenaMatch.getPassword() != packet.getPassKey()) {

                        response.setEctypeId(packet.getEctypeId());
                        response.setGateId(UnsignedChar.of(0));
                        response.setState((byte) 0);

                        packet.reply(response);

                        ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

                        status.setGuid(user.getGuid());
                        status.setCName(SmartString.of(user.getUsername(), 32));

                        status.setRoomId(arenaMatch.getSourceGuid());
                        status.setRequest(UnsignedChar.of(101));

                        status.setStatus((byte) 2);
                        packet.reply(status);
                        return;

                    }

                    List<Integer> fleetIds = new ArrayList<>();

                    for (int i = 0; i < packet.getDataLen().getValue(); i++) {
                        fleetIds.add(packet.getShips().getArray()[i]);
                    }

                    if (fleetIds.size() > 60 || fleetIds.size() < 1) {
                        return;
                    }

                    Optional<LoggedGameUser> optionalOwner = LoginService.getInstance().getGame(arenaMatch.getSourceUserId());

                    if (optionalOwner.isEmpty()) {
                        return;
                    }

                    LoggedGameUser owner = optionalOwner.get();
                    arenaMatch.addOpponent(user, fleetIds);

                    response.setEctypeId(packet.getEctypeId());
                    response.setGateId(UnsignedChar.of(0));
                    response.setState((byte) 4);

                    packet.reply(response);

                    ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

                    status.setGuid(user.getGuid());
                    status.setCName(SmartString.of(user.getUsername(), 32));

                    status.setRoomId(arenaMatch.getSourceGuid());
                    status.setRequest(UnsignedChar.of(101));

                    status.setStatus((byte) 1);
                    packet.reply(status);

                    ResponseArenaStatusPacket ownerStatus = new ResponseArenaStatusPacket();

                    ownerStatus.setGuid(user.getGuid());
                    ownerStatus.setCName(SmartString.of(user.getUsername(), 32));

                    ownerStatus.setRoomId(arenaMatch.getSourceGuid());
                    ownerStatus.setRequest(UnsignedChar.of(101));

                    ownerStatus.setStatus((byte) 1);
                    owner.getSmartServer().send(ownerStatus);

                }
            }
            default -> {
                // * Basic instances
                if (packet.getActivity() == 0) {
                    GameInstance gameInstance = ResourceManager.getGameInstance(packet.getEctypeId());
                    if (gameInstance != null && gameInstance.isValid()) {
                        Optional<LoggedGameUser> optionalLoggedGameUser = user.getLoggedGameUser();
                        if (optionalLoggedGameUser.isEmpty()) {
                            return;
                        }

                        LoggedGameUser loggedGameUser = optionalLoggedGameUser.get();
                        List<Integer> fleetIds = new ArrayList<>();

                        for (int i = 0; i < packet.getDataLen().getValue(); i++) {
                            fleetIds.add(packet.getShips().getArray()[i]);
                        }

                        MatchRunnable runnable = BattleService.getInstance().makeInstanceMatch(user, fleetIds, gameInstance);
                        if (runnable == null) {
                            BotLogger.error("Instance runnable creation error");
                            return;
                        }

                        Match current = runnable.getMatch();
                        loggedGameUser.setMatchViewing(current.getId());

                        response.setEctypeId((short) 0);
                        response.setGateId(UnsignedChar.of(0));
                        response.setState((byte) 1);

                        packet.reply(response);
                        return;
                    }
                } else if (packet.getActivity() == 1) {
                    /*boolean changed = false;
                    if (!RaidsService.getInstance().getEnabled().get()) {
                        return;
                    }
                    var prop = user.getInventory().getProp(packet.getPropsId().toInt());
                    if (user.getStats().getRaidAttemptsEntries() >= 5 || prop == null) {
                        return;
                    }
                    if (prop.getPropLockNum() < 1 && prop.getPropNum() < 1) {
                        return;
                    }
                    user.getInventory().removeProp(prop, 1);
                    ResponseUsePropsPacket deletePropsPacket = ResourcesService.getInstance().genericUseProps(prop.getPropId(), 1, 1, 1);
                    //Raids
                    for (var raidRoom : RaidsService.getInstance().getRaids()) {
                        if (raidRoom.getRoomId() == packet.getRoomId()) {
                            // 1 = Right
                            // 2 = Left
                            if (packet.getCapturePlace().getValue() == 2) {
                                if (raidRoom.getSecondGuid() != -1) {
                                    //someone had in the room!
                                    break;
                                }
                                raidRoom.setSecondGuid(user.getGuid());
                                raidRoom.setSecondPropId(packet.getPropsId().getValue());
                                raidRoom.setStatus(RaidStatus.WAITING);
                                for (var x = 0; x < packet.getDataLen().getValue(); x++) {
                                    raidRoom.getSecondDefenceFleets().add(packet.getShips().getArray()[x]);
                                }
                                if (raidRoom.getFirstGuid() != -1) {
                                    raidRoom.setTime(60);
                                    raidRoom.setStatus(RaidStatus.IN_PROGRESS);
                                    var anotherUser = UserService.getInstance().getUserCache().findByGuid(raidRoom.getFirstGuid());
                                    var anotherLoggedIn = anotherUser.getLoggedGameUser();
                                    anotherLoggedIn.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(RaidsService.getInstance().getArkInfoPacket(anotherUser)));
                                }
                                //leave slot
                                if (raidRoom.getFirstGuid() == user.getGuid()) {
                                    raidRoom.setFirstGuid(-1);
                                    raidRoom.setFirstPropId(-1);
                                }
                                changed = true;
                            } else if (packet.getCapturePlace().getValue() == 1) {
                                if (raidRoom.getFirstGuid() != -1) {
                                    //someone had in the room!
                                    break;
                                }
                                raidRoom.setFirstGuid(user.getGuid());
                                raidRoom.setFirstPropId(packet.getPropsId().getValue());
                                raidRoom.setStatus(RaidStatus.WAITING);
                                for (var x = 0; x < packet.getDataLen().getValue(); x++) {
                                    raidRoom.getFirstDefenceFleets().add(packet.getShips().getArray()[x]);
                                }
                                if (raidRoom.getSecondGuid() != -1) {
                                    raidRoom.setTime(60);
                                    raidRoom.setStatus(RaidStatus.IN_PROGRESS);
                                    var anotherUser = UserService.getInstance().getUserCache().findByGuid(raidRoom.getSecondGuid());
                                    var anotherLoggedIn = anotherUser.getLoggedGameUser();
                                    anotherLoggedIn.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(RaidsService.getInstance().getArkInfoPacket(anotherUser)));
                                }
                                //leave slot
                                if (raidRoom.getSecondGuid() == user.getGuid()) {
                                    raidRoom.setSecondGuid(-1);
                                    raidRoom.setSecondPropId(-1);
                                }
                                changed = true;
                            } else if (packet.getCapturePlace().getValue() == 4) {
                                //intercept， will not be complete for now.
                                packet.getSmartServer().sendMessage("Sorry, that instance is not done yet!");
                                response.setEctypeId((short) 0);
                                response.setGateId(UnsignedChar.of(0));
                                response.setState((byte) 0);
                                packet.reply(response);
                                return;
                                /*raidRoom.setStatus(RaidStatus.INTERCEPTED);
                                changed = true;
                                LoggedGameUser loggedGameUser = optionalLoggedGameUser.get();
                                List<Integer> fleetIds = new ArrayList<>();

                                for (int i = 0; i < packet.getDataLen().getValue(); i++) {
                                    fleetIds.add(packet.getShips().getArray()[i]);
                                }
                                MatchRunnable runnable = BattleService.getInstance().makeRaidMatch(raidRoom, packet.getGuid(), fleetIds);
                                Match current = runnable.getMatch();
                                loggedGameUser.setMatchViewing(current.getId());
                                response.setEctypeId((short) 0);
                                response.setGateId(UnsignedChar.of(0));
                                response.setState((byte) 1);
                                packet.reply(response);
                            }
                            packet.reply(deletePropsPacket, RaidsService.getInstance().getArkInfoPacket(user));
                        }
                        //leave slot
                        if (raidRoom.getFirstGuid() == user.getGuid() && raidRoom.getRoomId() != packet.getRoomId()) {
                            raidRoom.setFirstGuid(-1);
                            raidRoom.setFirstPropId(-1);
                        }
                        //leave slot
                        if (raidRoom.getSecondGuid() == user.getGuid() && raidRoom.getRoomId() != packet.getRoomId()) {
                            raidRoom.setSecondGuid(-1);
                            raidRoom.setSecondPropId(-1);
                        }
                    }
                    if(changed){
                        //broadcast to all online players, sigh...
                        RaidsService.getInstance().broadcastStatus();
                    }
                    return;
*/

                }
                System.out.println("Not found: " + packet.getEctypeId());
                packet.getSmartServer().sendMessage("Sorry, that instance is not done yet!");
                response.setEctypeId((short) 0);
                response.setGateId(UnsignedChar.of(0));
                response.setState((byte) 0);
                packet.reply(response);
            }
        }
    }

    @PacketProcessor
    public void onEctypeInfo(RequestEctypeInfoPacket packet) throws BadGuidException {

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
        Optional<Match> optionalMatch = BattleService.getInstance().getVirtualViewing(loggedGameUser);

        if (optionalMatch.isEmpty()) {
            optionalMatch = BattleService.getInstance().getVirtual(user.getGuid());
        }

        // leave League queue
        if (packet.getRequest() == 0 && packet.getEctypeId() == 1000) {
            LeagueMatchService.getInstance().removePlayer(user.getGuid());
            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
            response.setEctypeId(packet.getEctypeId());
            response.setGateId(UnsignedChar.of(0));
            response.setState((byte) 0);
            packet.reply(response);
        }

        if (optionalMatch.isEmpty()) {
            return;
        }

        Match current = optionalMatch.get();

        // Request:
        // 2 - Leave view
        // 1 - Resume Instance
        // 0 - Stop
        if (packet.getRequest() == 0) {

            if (current.getMatchType().isVirtual() && current.getMatchType() == MatchType.INSTANCE_MATCH) {

                // BotLogger.log("Stopping and deleting instance match: " + current.getId());

                for (BattleFleet battleFleet : current.getFleets()) {

                    Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(battleFleet.getShipTeamId());
                    if (fleet == null) {
                        continue;
                    }

                    if (fleet.getGuid() == -1) {
                        PacketService.getInstance().getFleetCache().delete(fleet);
                    }

                    if (fleet.getGuid() == user.getGuid()) {
                        fleet.setGalaxyId(user.getPlanet().getPosition().galaxyId());
                        fleet.save();
                    }

                }
                BattleService.getInstance().stopMatch(current, StopCause.MANUAL);

            }

        } else if (packet.getRequest() == 1) {

            if (current.getEctype() == 1002) {
                ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
                response.setEctypeId((short) current.getEctype());
                response.setGateId(UnsignedChar.of(0));
                response.setState((byte) 0);

                packet.reply(response);
                return;
            }

            loggedGameUser.setMatchViewing(current.getId());

            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

            response.setEctypeId((short) current.getEctype());
            response.setGateId(UnsignedChar.of(0));
            response.setState((byte) 1);

            packet.reply(response);

        } else if (packet.getRequest() == 2) {

            loggedGameUser.setViewing(user.getGalaxyId());
            loggedGameUser.setMatchViewing(null);
            switch (current.getMatchType()) {
                case ARENA_MATCH:
                    ArenaMatch arenaMatch = (ArenaMatch) current;
                    ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

                    status.setGuid(user.getGuid());
                    status.setCName(SmartString.of(user.getUsername(), 32));

                    status.setRoomId(arenaMatch.getSourceGuid());
                    status.setRequest(UnsignedChar.of(6));

                    status.setStatus((byte) 1);
                    packet.reply(status);

                    BotLogger.log(status);
                    break;
                case LEAGUE_MATCH:
                    ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
                    response.setEctypeId(packet.getEctypeId());
                    response.setGateId(UnsignedChar.of(0));
                    response.setState((byte) 2);
                    packet.reply(response);
                    break;
                case CHAMPION_MATCH, IGL_MATCH:
                    ResponseEctypeStatePacket response3 = new ResponseEctypeStatePacket();
                    response3.setEctypeId(packet.getEctypeId());
                    response3.setGateId(UnsignedChar.of(0));
                    response3.setState((byte) 0);
                    packet.reply(response3);

                    Optional<Match> optionalMatch2 = BattleService.getInstance().getVirtual(user.getGuid());
                    if (optionalMatch2.isPresent()) {
                        Match current2 = optionalMatch2.get();
                        if (!current2.getId().equals(current.getId())) {
                            ResponseEctypeStatePacket response4 = new ResponseEctypeStatePacket();
                            response4.setEctypeId((short) current2.getEctype());
                            response4.setGateId(UnsignedChar.of(0));
                            response4.setState((byte) 2);
                            packet.reply(response4);
                        }

                    }
                    break;
                default:
                    ResponseEctypeStatePacket ectypeStatePacket = BattleService.getInstance().getCurrentEctypeState(user, 2);
                    if (ectypeStatePacket.getEctypeId() != -1 && ectypeStatePacket.getState() != 0) {
                        packet.reply(ectypeStatePacket);
                    }
                    BotLogger.log((int) ectypeStatePacket.getState());
                    break;
            }
        }
    }
}
