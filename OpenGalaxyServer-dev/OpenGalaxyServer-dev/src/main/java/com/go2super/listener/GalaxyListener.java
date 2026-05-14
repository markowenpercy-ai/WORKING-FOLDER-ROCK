package com.go2super.listener;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.HumaroidPlanet;
import com.go2super.database.entity.sub.ResourcePlanet;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.chat.ResponseGalaxyBroadcastPacket;
import com.go2super.packet.construction.ResponseBuildInfoPacket;
import com.go2super.packet.construction.ResponseConsortiaWealthPacket;
import com.go2super.packet.galaxy.RequestGalaxyPacket;
import com.go2super.service.*;
import com.go2super.service.battle.match.WarMatch;
import com.go2super.service.exception.BadGuidException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.*;

public class GalaxyListener implements PacketListener {

    private final Map<Integer, Integer> lastSequence = new ConcurrentHashMap<>();

    @PacketProcessor
    public void onGalaxy(RequestGalaxyPacket packet) throws BadGuidException {
        LoginService.validate(packet, packet.getGuid());

        User requester = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (requester == null) {
            return;
        }

        Optional<LoggedGameUser> optionalLoggedGameUser = requester.getLoggedGameUser();
        if (optionalLoggedGameUser.isEmpty()) {
            return;
        }

        LoggedGameUser gameUser = optionalLoggedGameUser.get();

        GalaxyTile tile = new GalaxyTile(packet.getGalaxyId());
        Planet planet = GalaxyService.getInstance().getPlanet(tile);

        if (planet != null
                && gameUser.getViewing() == planet.getPosition().galaxyId()
                && lastSequence.containsKey(requester.getGuid())
                && lastSequence.get(requester.getGuid()) >= packet.getSeqId() - 1) {

            lastSequence.put(requester.getGuid(), packet.getSeqId());
            return;

        }

        lastSequence.put(requester.getGuid(), packet.getSeqId());
        // BotLogger.log("GALAXY PKT :: " + packet);
        if (planet == null) {
            return;
        }

        if (planet.getType() == PlanetType.HUMAROID_PLANET) {

            HumaroidPlanet humaroidPlanet = (HumaroidPlanet) planet;

            // ResponseBuildInfoPacket MSG_RESP_BUILDINFO [1]
            ResponseBuildInfoPacket buildInfoPacket = getBuildInfoPacket(packet.getGuid(), humaroidPlanet);
            Optional<WarMatch> optionalWarMatch = BattleService.getInstance().getWar(humaroidPlanet);

            if (optionalWarMatch.isPresent()) {

                WarMatch warMatch = optionalWarMatch.get();
                List<Packet> warPackets = BattleService.getInstance().getWarJoinPackets(warMatch, requester, buildInfoPacket);

                gameUser.setMatchViewing(warMatch.getId());
                packet.reply(warPackets);

            } else {

                packet.reply(buildInfoPacket);

            }

            if (gameUser.getViewing() != -1 && gameUser.getViewing() != planet.getPosition().galaxyId()) {

                leaveGalaxyBroadcast(gameUser, requester);
                gameUser.setViewing(planet.getPosition().galaxyId());

                joinGalaxyBroadcast(gameUser, requester);

            }

            return;

        }

        if (planet.getType() == PlanetType.RESOURCES_PLANET) {

            ResourcePlanet resourcePlanet = (ResourcePlanet) planet;

            if (resourcePlanet.isInWar()) {

                // ResponseBuildInfoPacket MSG_RESP_BUILDINFO [1]
                ResponseBuildInfoPacket buildInfoPacket = getBuildInfoPacket(packet.getGuid(), resourcePlanet);
                Optional<WarMatch> optionalWarMatch = BattleService.getInstance().getWar(resourcePlanet);

                if (optionalWarMatch.isPresent()) {

                    WarMatch warMatch = optionalWarMatch.get();
                    List<Packet> warPackets = BattleService.getInstance().getWarJoinPackets(warMatch, requester, buildInfoPacket);

                    gameUser.setMatchViewing(warMatch.getId());
                    packet.reply(warPackets);

                } else {

                    packet.reply(buildInfoPacket);

                }

                if (gameUser.getViewing() != -1 && gameUser.getViewing() != planet.getPosition().galaxyId()) {

                    leaveGalaxyBroadcast(gameUser, requester);
                    gameUser.setViewing(planet.getPosition().galaxyId());

                    joinGalaxyBroadcast(gameUser, requester);

                }

            } else {

                Optional<Corp> rbpCorp = resourcePlanet.getCorp();
                Corp userCorp = requester.getCorp();

                if (userCorp == null || rbpCorp.isEmpty()) {
                    return;
                }
                if (userCorp.getCorpId() != rbpCorp.get().getCorpId()) {
                    return;
                }

                // ResponseBuildInfoPacket MSG_RESP_BUILDINFO [1]
                ResponseBuildInfoPacket responseBuildInfoPacket = getBuildInfoPacket(packet.getGuid(), resourcePlanet);
                responseBuildInfoPacket.setConsortiaLeader((short) (userCorp.getMembers().getLeader().getGuid() == requester.getGuid() ? 1 : 0));

                packet.reply(responseBuildInfoPacket);

                ResponseConsortiaWealthPacket responseConsortiaWealthPacket = new ResponseConsortiaWealthPacket();
                responseConsortiaWealthPacket.setWealth(UnsignedInteger.of(userCorp.getWealth()));

                packet.reply(responseConsortiaWealthPacket);

                List<Fleet> fleets = PacketService.getInstance().getFleetCache().findAllByGalaxyId(resourcePlanet.getPosition().galaxyId());

                if (packet.getGuid() == requester.getGuid()) {

                    // ResponseGalaxyShip MSG_RESP_GALAXYSHIP [2]
                    if (fleets != null && fleets.size() > 0) { // todo friends

                        fleets = fleets.stream().filter(fleet -> !fleet.isInTransmission()).collect(Collectors.toList());
                        packet.reply(GalaxyService.getInstance().getGalaxyShipInfo(requester, fleets));

                    }

                }

                if (gameUser.getViewing() != planet.getPosition().galaxyId()) {

                    leaveGalaxyBroadcast(gameUser, requester);
                    gameUser.setViewing(planet.getPosition().galaxyId());

                    joinGalaxyBroadcast(gameUser, requester);

                }

            }

            return;

        }

        if (planet.getType() == PlanetType.USER_PLANET) {

            UserPlanet userPlanet = (UserPlanet) planet;
            User owner = UserService.getInstance().getUserCache().findByUserId(userPlanet.getUserId());
            if (owner == null) {
                return;
            }

            if (gameUser.getViewing() != planet.getPosition().galaxyId()) {

                leaveGalaxyBroadcast(gameUser, requester);
                gameUser.setViewing(planet.getPosition().galaxyId());

                joinGalaxyBroadcast(gameUser, requester);

            }

            if (userPlanet.isInWar()) {

                // ResponseBuildInfoPacket MSG_RESP_BUILDINFO [1]
                ResponseBuildInfoPacket buildInfoPacket = getBuildInfoPacket(packet.getGuid(), owner);
                Optional<WarMatch> optionalWarMatch = BattleService.getInstance().getWar(userPlanet);

                if (optionalWarMatch.isPresent()) {

                    WarMatch warMatch = optionalWarMatch.get();
                    List<Packet> warPackets = BattleService.getInstance().getWarJoinPackets(warMatch, requester, buildInfoPacket);

                    gameUser.setMatchViewing(warMatch.getId());
                    packet.reply(warPackets);

                } else {

                    packet.reply(buildInfoPacket);

                }

            } else {

                // ResponseBuildInfoPacket MSG_RESP_BUILDINFO [1]
                packet.reply(getBuildInfoPacket(packet.getGuid(), owner));

                List<Fleet> fleets = PacketService.getInstance().getFleetCache().findAllByGalaxyId(userPlanet.getPosition().galaxyId());

                boolean isOwner = requester.getGuid() == planet.getUserId();
                boolean isFriend = requester.isFriend(owner.getGuid());
                boolean isCorps = requester.getConsortiaId() != -1 && requester.getConsortiaId() == owner.getConsortiaId();
                if (isOwner || isFriend || isCorps) {
                    // ResponseGalaxyShip MSG_RESP_GALAXYSHIP [2]
                    if (fleets != null && !fleets.isEmpty()) {
                        fleets = fleets.stream().filter(fleet -> !fleet.isInTransmission()).collect(Collectors.toList());
                        packet.reply(GalaxyService.getInstance().getGalaxyShipInfo(requester, fleets));
                    }

                }

            }

        }

    }

    public void joinGalaxyBroadcast(LoggedGameUser gameUser, User user) {

        List<LoggedGameUser> receivers = new ArrayList<>();

        if (gameUser.getViewing() != -1) {
            receivers.addAll(LoginService.getInstance().getPlanetViewers(gameUser.getViewing()));
        } else if (gameUser.getMatchViewing() != null) {
            receivers.addAll(LoginService.getInstance().getMatchViewers(gameUser.getMatchViewing()));
        }

        if (receivers.isEmpty()) {
            return;
        }
        ResponseGalaxyBroadcastPacket enterBroadcast = new ResponseGalaxyBroadcastPacket();

        enterBroadcast.setGuid(user.getGuid());
        enterBroadcast.setUserId(user.getUserId());
        enterBroadcast.getName().value(user.getUsername());
        enterBroadcast.setKind(0);

        for (LoggedGameUser viewer : receivers) {
            if (viewer != gameUser) {
                viewer.getSmartServer().send(enterBroadcast);
            }
        }

    }

    public void leaveGalaxyBroadcast(LoggedGameUser gameUser, User user) {

        List<LoggedGameUser> receivers = new ArrayList<>();

        if (gameUser.getViewing() != -1) {
            receivers.addAll(LoginService.getInstance().getPlanetViewers(gameUser.getViewing()));
        } else if (gameUser.getMatchViewing() != null) {
            receivers.addAll(LoginService.getInstance().getMatchViewers(gameUser.getMatchViewing()));
        }

        if (receivers.isEmpty()) {
            return;
        }
        ResponseGalaxyBroadcastPacket leaveBroadcast = new ResponseGalaxyBroadcastPacket();

        leaveBroadcast.setGuid(user.getGuid());
        leaveBroadcast.setUserId(user.getUserId());
        leaveBroadcast.getName().value(user.getUsername());
        leaveBroadcast.setKind(1);

        for (LoggedGameUser viewer : receivers) {
            if (viewer != gameUser) {
                viewer.getSmartServer().send(leaveBroadcast);
            }
        }

        gameUser.setMatchViewing(null);
        gameUser.setViewing(-1);

    }

    public static ResponseBuildInfoPacket getBuildInfoPacket(int requester, ResourcePlanet resourcePlanet) {

        ResponseBuildInfoPacket packet = new ResponseBuildInfoPacket();

        packet.setGalaxyMapId(0);
        packet.setGalaxyId(resourcePlanet.getPosition().galaxyId());
        packet.setViewFlag((byte) 2);

        packet.setStarType((byte) 3);
        packet.setBuildInfoList(UserService.getInstance().getBuilds(resourcePlanet)); // getDebugBase() or getInitialBase()
        packet.setDataLen(packet.getBuildInfoList().size());

        return packet;

    }

    public static ResponseBuildInfoPacket getBuildInfoPacket(int requester, HumaroidPlanet humaroidPlanet) {

        ResponseBuildInfoPacket packet = new ResponseBuildInfoPacket();

        packet.setGalaxyMapId(0);
        packet.setGalaxyId(humaroidPlanet.getPosition().galaxyId());
        packet.setViewFlag((byte) 2);

        packet.setStarType((byte) 3);
        packet.setBuildInfoList(new ArrayList<>()); // getDebugBase() or getInitialBase()
        packet.setDataLen(packet.getBuildInfoList().size());

        return packet;

    }

    public static ResponseBuildInfoPacket getBuildInfoPacket(int requester, User user) {

        ResponseBuildInfoPacket packet = new ResponseBuildInfoPacket();

        Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(user.getConsortiaId());

        packet.setGalaxyMapId(0);
        packet.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        packet.setViewFlag((byte) user.getViewFlag(requester));

        if (corp != null) {
            packet.setConsortiaLeader((short) corp.getMembers().getLeader().getGuid());
        }

        packet.setStarType((byte) user.getGround());
        packet.setBuildInfoList(UserService.getInstance().getBuilds(user)); // getDebugBase() or getInitialBase()
        packet.setDataLen(packet.getBuildInfoList().size());

        return packet;

    }

}
