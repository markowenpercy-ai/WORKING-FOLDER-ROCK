package com.go2super.listener;

import com.go2super.database.entity.*;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.MatchType;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.*;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.BonusType;
import com.go2super.obj.type.JumpType;
import com.go2super.obj.utility.*;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.raids.ResponseJumpShipTeamNoticePacket;
import com.go2super.packet.ship.*;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.service.*;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import com.google.common.collect.Lists;

import java.util.*;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_NUM;

public class FleetListener implements PacketListener {

    @PacketProcessor
    public void onShipTeamInfo(RequestShipTeamInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());
        if (fleet == null) {
            return;
        }

        Commander commander = CommanderService.getInstance().getCommander(fleet.getCommanderId());

        if (commander == null) {

            // If commander is null the fleet
            // is owned by pirates (bots).
            // This means that the fleet is
            // in battle, so it has a BattleFleet.

            BattleFleet battleFleet = fleet.getBattleFleet();

            if (battleFleet == null) {

                BotLogger.error("Non expected behaviour, fleet " + fleet.getShipTeamId() + " has to be in battle!");
                return;

            }

            BattleCommander battleCommander = battleFleet.getBattleCommander();

            ResponseShipTeamInfoPacket response = new ResponseShipTeamInfoPacket();

            response.setShipTeamId(fleet.getShipTeamId());
            response.setUserId(-1);
            response.setGas(UnsignedInteger.of((int) battleFleet.getHe3()));
            response.setCommanderId(-1);

            response.setTeamName(SmartString.of(fleet.getName(), 32));
            response.setCommanderName(SmartString.of(battleCommander.getName(), 32));
            response.setTeamOwner(SmartString.of("Pirate", 32));

            response.setConsortia(SmartString.of("Pirate", 32));

            response.setTeamBody(fleet.getFleetBody());
            response.setSkillId((short) battleCommander.getSkillId());

            response.setAttackObjInterval((byte) fleet.getRangeType());
            response.setAttackObjType((byte) fleet.getPreferenceType());

            response.setLevelId(UnsignedChar.of(battleCommander.getLevel()));
            response.setCardLevel((byte) battleCommander.getStars());

            packet.reply(response);
            return;

        }

        User owner = commander.getUser();
        Corp corp = owner == null ? null : CorpService.getInstance().getCorpCache().findByGuid(owner.getGuid());

        ResponseShipTeamInfoPacket response = new ResponseShipTeamInfoPacket();

        response.setShipTeamId(fleet.getShipTeamId());
        response.setUserId(owner == null ? -1 : owner.getPlanet().getUserId());
        response.setGas(UnsignedInteger.of(fleet.getHe3()));
        response.setCommanderId(fleet.getCommanderId());

        response.setTeamName(SmartString.of(fleet.getName(), 32));
        response.setCommanderName(SmartString.of(commander.getName(), 32));
        response.setTeamOwner(SmartString.of(owner == null ? "Pirates" : owner.getUsername(), 32));

        response.setConsortia(SmartString.of(corp == null ? "" : corp.getName(), 32));

        response.setTeamBody(fleet.getFleetBody());
        response.setSkillId((short) commander.getSkill());

        response.setAttackObjInterval((byte) fleet.getRangeType());
        response.setAttackObjType((byte) fleet.getPreferenceType());

        response.setLevelId(UnsignedChar.of(commander.getLevel().getLevel()));
        response.setCardLevel((byte) commander.getStars());

        packet.reply(response);

    }

    @PacketProcessor
    public void onViewJumpShipTeam(RequestViewJumpShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());
        if (fleet == null) {
            return;
        }

        Commander commander = fleet.getCommander();
        if (commander == null) {
            return;
        }

        User owner = fleet.getUser();
        if (owner == null) {
            return;
        }

        CommanderAttributes baseAttributes = CommanderService.getInstance().getBaseAttributes(commander);
        ResponseViewJumpShipTeam response = new ResponseViewJumpShipTeam();

        response.setTeamModel(fleet.getViewTeamModel());

        response.setUserId(owner.getUserId());
        response.setCommanderUserId(owner.getUserId());

        response.setTeamName(SmartString.of(fleet.getName(), 32));
        response.setTeamOwner(SmartString.of(owner.getUsername(), 32));

        response.setShipTeamId(fleet.getShipTeamId());

        response.setAim((short) baseAttributes.getAim());
        response.setBlench((short) baseAttributes.getDodge());
        response.setPriority((short) baseAttributes.getSpeed());
        response.setElectron((short) baseAttributes.getElectron());
        response.setSkillId((short) commander.getSkill());

        response.setCardLevel((byte) commander.getStars());

        packet.reply(response);

    }

    @PacketProcessor
    public void onCancelJumpShipTeam(RequestCancelJumpShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());
        if (fleet == null) {
            return;
        }
        if (fleet.getGuid() != user.getGuid()) {
            return;
        }
        if (fleet.isInMatch() || !fleet.isInTransmission()) {
            return;
        }

        FleetTransmission transmission = fleet.getFleetTransmission();
        if (transmission == null) {
            return;
        }

        int userGalaxyId = user.getGalaxyId();

        if (transmission.getGalaxyId() == userGalaxyId || transmission.getJumpType() == JumpType.RECALL) {
            return;
        }

        int remains = DateUtil.remains(transmission.getUntil()).intValue();
        int spare = transmission.getTotal() - remains;

        Date until = DateUtil.now(spare);

        fleet.setGalaxyId(transmission.getGalaxyId());
        fleet.setFleetTransmission(FleetTransmission.builder()
            .galaxyId(user.getGalaxyId())
            .jumpType(JumpType.RECALL)
            .total(spare)
            .until(until)
            .build());

        fleet.save();

        ResponseCancelJumpShipTeamPacket response = new ResponseCancelJumpShipTeamPacket();

        response.setShipTeamId(fleet.getShipTeamId());
        response.setNeedTime(spare);

        packet.reply(response);

    }

    @PacketProcessor
    public void onJumpShipTeam(RequestJumpShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        GalaxyTile targetTile = new GalaxyTile(packet.getToGalaxyId());
        Planet planet = PacketService.getInstance().getPlanetCache().findByPosition(targetTile);

        if (planet == null) {
            return;
        }
        if (packet.getDataLen() == 0 || packet.getShipTeamId().getArray().length <= packet.getDataLen()) {
            return;
        }

        List<Fleet> formation = new ArrayList<>();

        for (int i = 0; i < packet.getDataLen(); i++) {

            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId().getArray()[i]);
            if (fleet == null || fleet.isInMatch() || fleet.isInTransmission() || fleet.getGalaxyId() == targetTile.galaxyId() || fleet.getGuid() != user.getGuid()) {
                return;
            }

            formation.add(fleet);
        }

        if (formation.isEmpty()) {
            return;
        }

        Optional<User> userNotice = Optional.empty();
        Optional<Corp> corpNotice = Optional.empty();

        boolean attack = packet.getJumpType() == 1;
        boolean sync = packet.getKind() == 1;

        if (attack) {
            //deduct SP
            user.getStats().setSp(Math.max(0, user.getStats().getSp() - (2 * formation.size())));
            user.save();
        }

        double allyFlyBonus = 0;
        switch (planet.getType()) {

            case USER_PLANET -> {

                UserPlanet userPlanet = (UserPlanet) planet;
                User userTarget = userPlanet.getUser().orElse(null);

                Corp targetCorp = userTarget.getCorp();
                if (targetCorp != null && targetCorp.getName() != null && targetCorp.getName().equals(CorpService.COUNCIL_CORP_NAME)) {

                    packet.getSmartServer().sendMessage("Commander, we can't attack the administration team!");
                    return;

                }

                if (attack) {

                    if (user.getCorp() != null && userTarget.getCorp() != null) {
                        if (userTarget.getCorp().getCorpId() == user.getCorp().getCorpId() && !userPlanet.isInWar()) {
                            return;
                        }
                    }

                    if (userTarget.getStats().hasTruce()) {
                        return;
                    }

                    userNotice = Optional.ofNullable(userTarget);
                    corpNotice = Optional.ofNullable(userTarget.getCorp());

                } else {
                  if (user.getCorp() != null && userTarget.getCorp() != null) {
                    if (userTarget.getCorp().getCorpId() == user.getCorp().getCorpId()) {
                      UserBuilding building = userTarget.getBuildings().getBuilding("build:alliance");
                      if (building != null) {
                        allyFlyBonus = building.getLevelData().getEffect("allyFlyBonus").getValue();
                      }
                    }
                  }
                }

            }

            case HUMAROID_PLANET -> {

                HumaroidPlanet humaroidPlanet = (HumaroidPlanet) planet;

                if (!attack) {
                    return;
                }
                if (humaroidPlanet.hasTruce() || humaroidPlanet.isInWar()) {
                    return;
                }

            }

            case RESOURCES_PLANET -> {

                ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
                Optional<Corp> targetCorp = resourcePlanet.getCorp();

                if (!attack) {
                    return;
                }
                if (resourcePlanet.hasTruce()) {
                    return;
                }

                Optional<Corp> optionalCorpTarget = resourcePlanet.getCorp();
                Corp userCorp = user.getCorp();

                if (attack && userCorp != null && optionalCorpTarget.isPresent()) {
                    Corp corp = optionalCorpTarget.get();
                    if (corp.getCorpId() == userCorp.getCorpId()) {
                        return;
                    }
                }

                corpNotice = targetCorp;
                break;

            }

        }

        if (user.getStats().hasTruce()) {

            user.getStats().removeBoost(BonusType.PLANET_PROTECTION);

            user.update();
            user.save();
            packet.reply(user.getQueuesAsPacket());
        }

        double syncTransmission = -1;
        Date syncUntil = null;

        if (sync) {

            for (Fleet fleet : formation) {

                GalaxyTile from = new GalaxyTile(fleet.getGalaxyId());
                Commander commanderCache =
                    CommanderService.getInstance().getCommanderCache().findByCommanderIdAndUserId(fleet.getCommanderId(),
                        fleet.getUser().getUserId());
                double currentDistance = from.getManhattanDistance(targetTile);
                double currentTransmission = calculateTransmission(fleet, currentDistance, commanderCache, allyFlyBonus);

                if (syncTransmission == -1 || currentTransmission > syncTransmission) {
                    syncTransmission = currentTransmission;
                }

            }

            if (syncTransmission == -1) {
                return;
            }
            syncUntil = DateUtil.now((int) syncTransmission);

        }

        List<JumpShipTeamInfo> teamInfos = new ArrayList<>();

        Date asyncUntil = null;

        for (Fleet fleet : formation) {

            if (!sync) {

                GalaxyTile from = new GalaxyTile(fleet.getGalaxyId());
                Commander commanderCache =
                    CommanderService.getInstance().getCommanderCache().findByCommanderIdAndUserId(fleet.getCommanderId(),
                        fleet.getUser().getUserId());
                double currentDistance = from.getManhattanDistance(targetTile);
                double currentTransmission = calculateTransmission(fleet, currentDistance, commanderCache, allyFlyBonus);

                asyncUntil = DateUtil.now((int) currentTransmission);

            }

            Date until = sync ? syncUntil : asyncUntil;
            int spare = DateUtil.remains(until).intValue();

            fleet.setFleetTransmission(FleetTransmission.builder()
                .galaxyId(targetTile.galaxyId())
                .jumpType(attack ? JumpType.ATTACK : JumpType.DEFEND)
                .total(spare)
                .until(until)
                .build());

            teamInfos.add(JumpShipTeamInfo.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .shipTeamId(fleet.getShipTeamId())
                .fromGalaxyId(fleet.getGalaxyId())
                .toGalaxyId(targetTile.galaxyId())
                .spareTime(spare)
                .totalTime(spare)
                .fromGalaxyMapId(0)
                .toGalaxyMapId(0)
                .kind(packet.getKind())
                .galaxyType((byte) 1)
                .build());

            fleet.save();

        }

        for (JumpShipTeamInfo teamInfo : teamInfos) {

            ResponseJumpShipTeamPacket response = new ResponseJumpShipTeamPacket();
            response.setData(teamInfo);

            List<LoggedGameUser> loggedGameUsers = LoginService.getInstance().getPlanetViewers(teamInfo.getFromGalaxyId());

            for (LoggedGameUser loggedGameUser : loggedGameUsers) {

                ResponseDeleteShipTeamBroadcastPacket broadcast = new ResponseDeleteShipTeamBroadcastPacket();

                broadcast.setGalaxyMapId(0);
                broadcast.setGalaxyId(teamInfo.getFromGalaxyId());
                broadcast.setShipTeamId(teamInfo.getShipTeamId());

                loggedGameUser.getSmartServer().send(broadcast);

            }

            packet.reply(response);

        }

        if (corpNotice.isPresent()) {

            Corp corp = corpNotice.get();

            ResponseJumpShipTeamNoticePacket noticePacket = new ResponseJumpShipTeamNoticePacket();
            noticePacket.setKind(1);

            for (CorpMember corpMember : corp.getMembers().getMembers()) {
                Optional<LoggedGameUser> loggedGameUser = LoginService.getInstance().getGame(corpMember.getGuid());
                if (loggedGameUser.isPresent()) {
                    loggedGameUser.get().getSmartServer().send(noticePacket);
                }
            }

        }

        if (userNotice.isPresent()) {

            Optional<LoggedGameUser> optionalNotice = userNotice.get().getLoggedGameUser();

            if (optionalNotice.isPresent()) {

                ResponseJumpShipTeamNoticePacket noticePacket = new ResponseJumpShipTeamNoticePacket();
                noticePacket.setKind(0);

                optionalNotice.get().getSmartServer().send(noticePacket);
            }

        }

    }

    @PacketProcessor
    public void onJumpShipTeamInfo(RequestJumpShipTeamInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserPlanet planet = user.getPlanet();
        if (planet == null) {
            return;
        }

        List<Fleet> transition = PacketService.getInstance().getFleetCache().getRadarFleets(user.getGuid(), planet.getPosition().galaxyId());
        if (transition.isEmpty()) {
            return;
        }

        Set<Fleet> filtered = new HashSet<>();

        for (Fleet fleet : transition) {
            if (filtered.stream().anyMatch(filterFleet -> filterFleet.getId().toString().equals(fleet.getId().toString()))) {
                continue;
            }
            filtered.add(fleet);
        }

        int index = 0;

        ResponseJumpShipTeamInfoPacket currentResponse = null;
        List<ResponseJumpShipTeamInfoPacket> responsePackets = new ArrayList<>();

        Map<Integer, User> cacheUsers = new HashMap<>();
        List<JumpShipTeamInfo> jumps = new ArrayList<>();

        for (Fleet fleet : filtered) {

            long ownerUserId = -1;
            String ownerName = "Unknown";

            if (!cacheUsers.containsKey(fleet.getGuid())) {
                User owner = UserService.getInstance().getUserCache().findByGuid(fleet.getGuid());
                if (owner != null) {
                    cacheUsers.put(fleet.getGuid(), owner);
                    ownerName = owner.getUsername();
                    ownerUserId = owner.getUserId();
                }
            } else {
                ownerName = cacheUsers.get(fleet.getGuid()).getUsername();
                ownerUserId = cacheUsers.get(fleet.getGuid()).getUserId();
            }

            int kind = 0;
            int galaxyType = 1;

            int spare = -1;
            int toGalaxyId = -1;
            int totalTime = -1;

            FleetTransmission transmission = fleet.getFleetTransmission();

            if (transmission != null) {

                spare = DateUtil.remains(transmission.getUntil()).intValue();

                if (fleet.getGuid() == user.getGuid()) {

                    kind = 0;

                } else {

                    if (transmission.getJumpType() == JumpType.ATTACK) {
                        kind = 1;
                    } else {
                        kind = 2;
                    }

                }

                toGalaxyId = transmission.getGalaxyId();
                totalTime = transmission.getTotal();

                Planet toPlanet = PacketService.getInstance().getPlanetCache().findByPosition(new GalaxyTile(toGalaxyId));
                if (toPlanet != null) {
                    switch (toPlanet.getType()) {
                        case USER_PLANET -> {
                            UserPlanet userPlanet = (UserPlanet) toPlanet;
                            Optional<User> toUser = userPlanet.getUser();
                            if (toUser.isPresent()) {
                                ownerName = toUser.get().getUsername();
                                galaxyType = 1;
                            } else {
                                ownerName = "Yield Planet";
                                galaxyType = 0;
                            }
                        }
                        case HUMAROID_PLANET -> {
                            galaxyType = 2;
                        }
                        default -> {
                            galaxyType = 0;
                        }
                    }
                } else {
                    galaxyType = 0;
                }

            } else if (fleet.isInMatch() && fleet.getGuid() == user.getGuid()) {

                kind = 3;

                toGalaxyId = fleet.getCurrentMatch().getGalaxyId();
                totalTime = 0;

                Planet toPlanet = PacketService.getInstance().getPlanetCache().findByPosition(new GalaxyTile(toGalaxyId));

                if (toPlanet != null) {
                    if (toPlanet.getType() == PlanetType.USER_PLANET) {
                        UserPlanet userPlanet = (UserPlanet) toPlanet;
                        Optional<User> toUser = userPlanet.getUser();
                        if (toUser.isPresent()) {
                            ownerName = toUser.get().getUsername();
                            galaxyType = 1;
                        } else {
                            ownerName = "Yield Planet";
                            galaxyType = 0;
                        }
                    } else {
                        galaxyType = 0;
                    }
                } else {
                    galaxyType = 0;
                }

            } else if (fleet.getGuid() == user.getGuid() && fleet.getGalaxyId() != planet.getPosition().galaxyId()) {

                if (fleet.getFleetInitiator() != null && fleet.getFleetInitiator().getJumpType() == JumpType.ATTACK) {
                    kind = 3;
                } else {
                    kind = 4;
                }

                toGalaxyId = fleet.getGalaxyId();
                Planet toPlanet = PacketService.getInstance().getPlanetCache().findByPosition(new GalaxyTile(toGalaxyId));

                if (toPlanet != null) {
                    if (toPlanet.getType() == PlanetType.USER_PLANET) {
                        UserPlanet userPlanet = (UserPlanet) toPlanet;
                        Optional<User> toUser = userPlanet.getUser();
                        if (toUser.isPresent()) {
                            galaxyType = 1;
                        } else {
                            galaxyType = 0;
                        }
                    } else {
                        galaxyType = 0;
                    }
                } else {
                    galaxyType = 0;
                }

            } else {

                continue;

            }

            if (++index > 14) {

                responsePackets.add(currentResponse);
                currentResponse = null;
                jumps = new ArrayList<>();

            }

            if (currentResponse == null) {

                index = 0;
                currentResponse = new ResponseJumpShipTeamInfoPacket();
                currentResponse.setTransmission(jumps);

            }

            JumpShipTeamInfo teamInfo = JumpShipTeamInfo.builder()
                .userId(ownerUserId)
                .userName(ownerName)
                .shipTeamId(fleet.getShipTeamId())
                .fromGalaxyId(fleet.getGalaxyId())
                .toGalaxyId(toGalaxyId)
                .spareTime(spare)
                .totalTime(totalTime)
                .fromGalaxyMapId(0)
                .toGalaxyMapId(0)
                .kind((byte) kind) // todo
                .galaxyType((byte) galaxyType)
                .build();

            jumps.add(teamInfo);
            currentResponse.setDataLen(jumps.size());

        }

        if (currentResponse == null) {

            currentResponse = new ResponseJumpShipTeamInfoPacket();
            currentResponse.setDataLen((byte) 0);

        }

        if (currentResponse != null) {
            responsePackets.add(currentResponse);
        }
        packet.reply(responsePackets);

    }

    @PacketProcessor
    public void onJumpGalaxyShip(RequestJumpGalaxyShipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        BotLogger.log(packet);
        Corp jumpCorp = user.getCorp();

        GalaxyTile targetTile = new GalaxyTile(packet.getGalaxyId());
        MatchType matchType = MatchType.INSTANCE_MATCH;
        int jumpType = 0;
        double allyFlyBonus = 0;
        main:
        switch (packet.getKind()) {

            case 0 -> { // Planet

                Planet planet = PacketService.getInstance().getPlanetCache().findByPosition(targetTile);

                switch (planet.getType()) {

                    case USER_PLANET:

                        matchType = MatchType.PVP_MATCH;

                        UserPlanet userPlanet = (UserPlanet) planet;

                        Optional<User> optionalUserTarget = userPlanet.getUser();
                        if (optionalUserTarget.isEmpty()) {
                            return;
                        }

                        User userTarget = optionalUserTarget.get();

                        if (user.getCorp() != null && userTarget.getCorp() != null && (userTarget.getCorp().getCorpId() == user.getCorp().getCorpId())) {
                            jumpType = 0;
                            if (user.getCorp() != null && userTarget.getCorp() != null) {
                              if (userTarget.getCorp().getCorpId() == user.getCorp().getCorpId()) {
                                UserBuilding building = userTarget.getBuildings().getBuilding("build:alliance");
                                if (building != null) {
                                  allyFlyBonus = building.getLevelData().getEffect("allyFlyBonus").getValue();
                                }
                              }
                            }
                            break main;
                        }

                        if (userTarget.getStats().hasTruce()) {
                            return;
                        }
                        if (userPlanet.isInWar()) {
                            jumpType = 2;
                        } else {
                            jumpType = 1;
                        }
                        break;

                    case RESOURCES_PLANET:

                        matchType = MatchType.PVP_MATCH;

                        ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
                        if (resourcePlanet.hasTruce()) {
                            return;
                        }

                        Optional<Corp> optionalCorpTarget = resourcePlanet.getCorp();

                        if (optionalCorpTarget.isPresent() && jumpCorp != null) {
                            Corp corp = optionalCorpTarget.get();
                            if (corp.getCorpId() == jumpCorp.getCorpId()) {
                                return;
                            }
                        }

                        jumpType = 1;
                        break;

                    case HUMAROID_PLANET:

                        matchType = MatchType.PVP_MATCH;

                        HumaroidPlanet humaroidPlanet = (HumaroidPlanet) planet;
                        if (humaroidPlanet.hasTruce()) {
                            return;
                        }

                        jumpType = 1;
                        break;

                }

                // ! Debug user
                // packet.getSmartServer().sendMessage("PvP feature is not done yet!");
                // return;
                break;
            }

            case 1 -> { // RBP Planet

                if (jumpCorp == null) {
                    return;
                }

                Planet planet = PacketService.getInstance().getPlanetCache().findByPosition(targetTile);
                if (planet.getType() != PlanetType.RESOURCES_PLANET) {
                    return;
                }

                ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
                Optional<Corp> optionalCorp = resourcePlanet.getCorp();
                if (optionalCorp.isEmpty()) {
                    return;
                }

                Corp corp = optionalCorp.get();
                if (corp.getCorpId() != jumpCorp.getCorpId()) {
                    return;
                }
                jumpType = 0;
                break;
            }

            case 2 -> { // Any instance
                matchType = MatchType.INSTANCE_MATCH;
            }

            case 3 -> { // Arena
                matchType = MatchType.ARENA_MATCH;
            }

            default -> {
                return;
            }

        }

        int targetGalaxyId = matchType.isVirtual() ? -1 : targetTile.galaxyId();

        List<Fleet> fleets = user.getFleets();
        List<JumpGalaxyShipInfo> jumps = new ArrayList<>();

        List<ResponseJumpGalaxyShipPacket> responsePackets = new ArrayList<>();
        ResponseJumpGalaxyShipPacket currentResponse = null;

        int index = 0;

        for (Fleet fleet : fleets) {

            Planet sourcePlanet = PacketService.getInstance().getPlanetCache().findByPosition(new GalaxyTile(fleet.getGalaxyId()));

            if (sourcePlanet != null && sourcePlanet.isInWar()) {
                continue;
            }

            if (sourcePlanet != null && (sourcePlanet.getType() == PlanetType.RESOURCES_PLANET || sourcePlanet.getType() == PlanetType.HUMAROID_PLANET)) {
                continue;
            }

            if (matchType.isVirtual() && fleet.getGalaxyId() != user.getGalaxyId()) {
                continue;
            }

            if (fleet.isInMatch() || fleet.isInTransmission() || fleet.getGalaxyId() == targetGalaxyId) {
                continue;
            }

            if (++index > 17) {

                responsePackets.add(currentResponse);
                currentResponse = null;
                jumps = new ArrayList<>();

            }

            if (currentResponse == null) {

                index = 0;
                currentResponse = new ResponseJumpGalaxyShipPacket();

                currentResponse.setGalaxyId(targetGalaxyId);
                currentResponse.setGalaxyMapId((byte) 0);

                currentResponse.setJumpType((byte) jumpType);
                currentResponse.setKind((byte) packet.getKind());
                currentResponse.setShips(jumps);

            }

            JumpGalaxyShipInfo ship = new JumpGalaxyShipInfo();

            double percent = (double) fleet.getHe3() / (double) fleet.getMaxHe3();
            percent = percent * 100.0;

            if (!matchType.isVirtual()) {

                GalaxyTile from = new GalaxyTile(fleet.getGalaxyId());

                double distance = from.getManhattanDistance(targetTile);
                Commander commanderCache =
                    CommanderService.getInstance().getCommanderCache().findByCommanderIdAndUserId(fleet.getCommanderId(),
                        fleet.getUser().getUserId());
                double transmission = calculateTransmission(fleet, distance, commanderCache, allyFlyBonus);
                commanderCache.setStars(fleet.getCommander().getStars());
                ship.setJumpNeedTime((int) transmission);

            }

            ship.setTeamName(fleet.getName());
            ship.setBodyId(fleet.getBodyId());
            ship.setCommanderId(fleet.getCommanderId());
            ship.setGas(fleet.getHe3());
            ship.setGasPercent((int) percent);

            ship.setShipNum(fleet.ships());
            ship.setShipTeamId(fleet.getShipTeamId());

            jumps.add(ship);
            currentResponse.setDataLen((byte) jumps.size());

        }

        if (currentResponse == null) {

            currentResponse = new ResponseJumpGalaxyShipPacket();

            currentResponse.setGalaxyId(targetGalaxyId);
            currentResponse.setGalaxyMapId((byte) 0);

            currentResponse.setJumpType((byte) jumpType);
            currentResponse.setKind((byte) packet.getKind());
            currentResponse.setDataLen((byte) 0);

        }

        if (currentResponse != null) {
            responsePackets.add(currentResponse);
        }
        packet.reply(responsePackets);

    }

    @PacketProcessor
    public void onEditShipTeam(RequestEditShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getShipTeamId() < 0) {
            return;
        }

        Commander commander = user.getCommander(packet.getCommanderId());
        Fleet ccFleet = commander != null ? commander.getFleet() : null;

        // Check if commander is restricted from use
        if (commander != null && RestrictedItemsService.getInstance().isRestricted(CommanderService.getInstance().getPropId(commander))) {
            Optional<LoggedGameUser> gameUser = LoginService.getInstance().getGame(user);
            if (gameUser.isPresent()) {
                gameUser.get().getSmartServer().sendMessage("This commander is not available in early game.");
            }
            return;
        }

        if (commander == null || !commander.canPilot() || (ccFleet != null && ccFleet.getShipTeamId() != packet.getShipTeamId())) {
            return;
        }

        Fleet fleet = PacketService.getInstance()
            .getFleetCache()
            .findByShipTeamId(packet.getShipTeamId());

        if (fleet == null) {
            return;
        }
        if (fleet.getShipTeamId() != packet.getShipTeamId() || fleet.getGuid() != packet.getGuid()) {
            return;
        }
        if (fleet.isMatch() || fleet.isInMatch() || fleet.isInTransmission()) {
            return;
        }

        Planet planet = GalaxyService.getInstance().getPlanet(new GalaxyTile(fleet.getGalaxyId()));
        if (planet != null && planet.isInWar()) {
            return;
        }

        ShipTeamBody oldTeamBody = fleet.getFleetBody();
        ShipTeamBody newTeamBody = packet.getTeamBody();

        UserShips ships = user.getShips();

        // Sanity checks:
        //   Fleets have 9 stacks
        //   Fleets have at least 1 ship
        //   Stacks cannot have more than 3000 ships
        //   Fleets have up to 1 flagship stack

        if (newTeamBody.getCells().size() != 9) {
            return;
        }

        int total = 0;
        boolean hasFlagship = false;

        for (ShipTeamNum newNum : newTeamBody.getCells()) {
            if (newNum.getShipModelId() <= -1 || newNum.getNum() <= 0) {
                // Ignore empty stacks
                continue;
            }

            total += newNum.getNum();

            if (newNum.getNum() > 3000) {
                // Stacks cannot be larger than 3000
                return;
            }

            ShipBodyData shipData = ResourceManager
                .getShipBodies()
                .findByBodyId(newNum.getBodyId());

            if (shipData.getBodyType().equals("flagship")) {
                if (hasFlagship) {
                    // The fleet has more than 1 stack of flagships
                    return;
                } else {
                    // This is the first stack of flagships seen
                    hasFlagship = true;
                }
            }
        }

        if (total == 0) {
            // No ships in new fleet, abort
            return;
        }

        // Passed preliminary checks, time to start making changes

        // Return all ships from the old fleet to inventory
        for (ShipTeamNum oldNum : oldTeamBody.getCells()) {
            if (oldNum.getNum() != 0 && oldNum.getShipModelId() > -1) {
                ships.addShip(oldNum.getShipModelId(), oldNum.getNum());
            }
        }

        int error = -1;

        for (int idx = 0; idx < 9; ++idx) {
            ShipTeamNum newNum = newTeamBody.getCells().get(idx);

            if (newNum.getShipModelId() <= -1 || newNum.getNum() <= 0) {
                // Ignore empty stacks
                continue;
            }

            if (!ships.removeShip(newNum.getShipModelId(), newNum.getNum())) {
                // Fleets can only use ships that exist in inventory
                error = idx;
                break;
            }

            ShipModel model = PacketService.getShipModel(newNum.getShipModelId());

            if (model == null) {
                // Fleets can only use designs that exist
                // This might be redundant due to the ships.removeShip check above
                error = idx;
                break;
            }
        }

        if (error > -1) {
            // An error was found above, undo the changes to the fleet and abort
            for (int idx = 0; idx < error; ++idx) {
                // Re-add any ships to inventory that were removed earlier
                ShipTeamNum newNum = newTeamBody.getCells().get(idx);
                ships.addShip(newNum.getShipModelId(), newNum.getNum());
            }

            // Take the old ships back out of inventory
            for (ShipTeamNum oldNum : oldTeamBody.getCells()) {
                if (oldNum.getNum() != 0 && oldNum.getShipModelId() > -1) {
                    ships.removeShip(oldNum.getShipModelId(), oldNum.getNum());
                }
            }

            return;
        }

        // All clear, apply the rest of the changes & save
        if (fleet.getCommanderId() != commander.getCommanderId()) {
            fleet.setCommanderId(commander.getCommanderId());
        }

        fleet.setName(packet.getName().noSpaces());
        fleet.setFleetBody(newTeamBody);
        fleet.setBodyId(fleet.bodyId());

        if (packet.getRange() < 0 || packet.getRange() > 1) {
            packet.setRange((byte) 0);
        }

        if (packet.getPreference() < 0 || packet.getPreference() > 6) {
            packet.setPreference((byte) 0);
        }

        fleet.setRangeType(packet.getRange());
        fleet.setPreferenceType(packet.getPreference());

        user.save();
        fleet.save();


        ResponseEditShipTeamPacket response = new ResponseEditShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(fleet.getGalaxyId());

        GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

        fleetInfo.setShipTeamId(fleet.getShipTeamId());
        fleetInfo.setShipNum(fleet.ships());
        fleetInfo.setBodyId((short) fleet.getBodyId());
        fleetInfo.setReserve((short) 0);
        fleetInfo.setDirection((byte) fleet.getDirection());

        fleetInfo.setPosX((byte) fleet.getPosX());
        fleetInfo.setPosY((byte) fleet.getPosY());
        fleetInfo.setOwner((byte) 2);

        response.setGalaxyFleetInfo(fleetInfo);
        packet.reply(response);
    }

    @PacketProcessor
    public void onCreateFleet(RequestCreateShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Commander commander = user.getCommander(packet.getCommanderId());
        if (commander == null) {
            return;
        }
        if (!commander.canPilot()) {
            return;
        }
        if (PacketService.getInstance().getFleetCache().findByCommanderId(commander.getCommanderId()) != null) {
            return;
        }

        UserPlanet userPlanet = user.getPlanet();
        if (userPlanet.isInWar()) {
            return;
        }

        ShipTeamBody teamBody = packet.getTeamBody();
        UserShips ships = user.getShips();

        // --------------------

        // Sanity checks:
        //   Fleets have 9 stacks
        //   Fleets have at least 1 ship
        //   Stacks cannot have more than 3000 ships
        //   Fleets have up to 1 flagship stack
        if (teamBody.getCells().size() != 9) {
            return;
        }

        int total = 0;
        boolean hasFlagship = false;

        for (ShipTeamNum newNum : teamBody.getCells()) {
            if (newNum.getShipModelId() <= -1 || newNum.getNum() <= 0) {
                // Ignore empty stacks
                continue;
            }

            total += newNum.getNum();

            if (newNum.getNum() > 3000) {
                // Stacks cannot be larger than 3000
                return;
            }

            ShipBodyData shipData = ResourceManager
                .getShipBodies()
                .findByBodyId(newNum.getBodyId());

            if (shipData.getBodyType().equals("flagship")) {
                if (hasFlagship) {
                    // The fleet has more than 1 stack of flagships
                    return;
                } else {
                    // This is the first stack of flagships seen
                    hasFlagship = true;
                }
            }
        }

        if (total == 0) {
            // No ships in new fleet, abort
            return;
        }

        // Passed preliminary checks, time to start making changes
        int error = -1;

        for (int idx = 0; idx < 9; ++idx) {
            ShipTeamNum newNum = teamBody.getCells().get(idx);

            if (newNum.getShipModelId() <= -1 || newNum.getNum() <= 0) {
                // Ignore empty stacks
                continue;
            }

            if (!ships.removeShip(newNum.getShipModelId(), newNum.getNum())) {
                // Fleets can only use ships that exist in inventory
                error = idx;
                break;
            }

            ShipModel model = PacketService.getShipModel(newNum.getShipModelId());

            if (model == null) {
                // Fleets can only use designs that exist
                // This might be redundant due to the ships.removeShip check above
                error = idx;
                break;
            }
        }

        if (error > -1) {
            // An error was found above, undo the changes to the fleet and abort
            for (int idx = 0; idx < error; ++idx) {
                // Re-add any ships to inventory that were removed earlier
                ShipTeamNum newNum = teamBody.getCells().get(idx);
                ships.addShip(newNum.getShipModelId(), newNum.getNum());
            }

            return;
        }

        // --------------------

        if (packet.getRange() < 0 || packet.getRange() > 1) {
            packet.setRange((byte) 0);
        }

        if (packet.getPreference() < 0 || packet.getPreference() > 6) {
            packet.setPreference((byte) 0);
        }

        if (total <= 0) {
            return;
        }

        Fleet fleet = Fleet.builder()
            .shipTeamId(AutoIncrementService.getInstance().getNextFleetId())
            .galaxyId(user.getPlanet().getPosition().galaxyId())
            .fleetBody(teamBody)
            .name(packet.getName().noSpaces())
            .commanderId(commander.getCommanderId())
            .guid(user.getGuid())
            .rangeType(packet.getRange())
            .preferenceType(packet.getPreference())
            .posX(13)
            .posY(13)
            .build();

        fleet.setBodyId(fleet.bodyId());

        user.getMetrics().add("action:build.fleet", 1);
        user.update();
        user.save();

        PacketService.getInstance().getFleetCache().save(fleet);

        Optional<LoggedGameUser> optionalGameUser = user.getLoggedGameUser();
        if (optionalGameUser.isEmpty()) {
            return;
        }

        ResponseCreateShipTeamPacket response = new ResponseCreateShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());

        for (LoggedGameUser viewer : LoginService.getInstance().getPlanetViewers(fleet.getGalaxyId())) {

            GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

            fleetInfo.setShipTeamId(fleet.getShipTeamId());
            fleetInfo.setShipNum(fleet.ships());
            fleetInfo.setBodyId((short) fleet.getBodyId());
            fleetInfo.setReserve((short) 0);
            fleetInfo.setDirection((byte) fleet.getDirection());

            fleetInfo.setPosX((byte) fleet.getPosX());
            fleetInfo.setPosY((byte) fleet.getPosY());
            fleetInfo.setOwner((byte) (BattleService.getInstance().getFleetColor(viewer, fleet)));

            response.setGalaxyFleetInfo(fleetInfo);
            viewer.getSmartServer().send(response);

        }

    }

    @PacketProcessor
    public void onArrangement(RequestArrangeShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        List<ShipTeamNum> list = new ArrayList<>();

        List<Fleet> fleets = PacketService.getInstance().getFleetCache().findAllByGuid(packet.getGuid());
        UserShips ships = user.getShips();
        ShipTeamNum reference = new ShipTeamNum();

        Set<Integer> shipModels = new HashSet<>();

        if (ships.getShips() != null && !ships.getShips().isEmpty()) {
            for (ShipTeamNum shipTeamNum : ships.getShips()) {
                if (shipTeamNum.getShipModelId() == -1) {
                    continue;
                }
                list.add(shipTeamNum);
                shipModels.add(shipTeamNum.getShipModelId());
            }
        }

        for (Fleet fleet : fleets) {
            for (ShipTeamNum shipTeamNum : fleet.getFleetBody().getCells()) {
                if (shipTeamNum.getShipModelId() == -1) {
                    continue;
                }
                shipModels.add(shipTeamNum.getShipModelId());
            }
        }

        while (list.size() < 120) {
            list.add(reference.trash());
        }

        // Send model ID
        // BotLogger.log("Sending models (" + shipModels + ")");
        packet.reply(PacketService.getInstance().getShipModels(user, shipModels));
        // BotLogger.log("Sent");

        ResponseArrangeShipTeamPacket response = new ResponseArrangeShipTeamPacket();

        response.setDataLen((short) ships.getShips().size());
        response.setKind((short) packet.getKind());
        response.setShipNums(list);

        List<TeamModelSlot> teamModelSlots = TeamModelService.getInstance().getTeamModelsRepository().findByGuid(user.getGuid());
        List<TeamModel> models = Lists.newArrayList(new TeamModel(), new TeamModel(), new TeamModel());

        for (TeamModelSlot modelSlot: teamModelSlots) {
            models.set(modelSlot.getIndexId(), modelSlot.getTeamModel());
        }

        ResponseTeamModelInfoPacket responseTeamModels = ResponseTeamModelInfoPacket.builder()
            .dataLen(models.size())
            .teamModel(models)
            .build();

        packet.reply(responseTeamModels);
        packet.reply(response);

    }

    @PacketProcessor
    public void onCommanderArrangement(RequestCommanderInfoArrangePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        ResponseCommanderInfoArrangePacket response = new ResponseCommanderInfoArrangePacket();
        List<Commander> commanders = user.getCommanders();

        response.setDataLen(MAX_COMMANDER_NUM);

        for (int i = 0; i < commanders.size(); i++) {
            Commander commander = commanders.get(i);
            if (!commander.hasFleet() && commander.canPilot()) {
                response.getData().set(i, commanders.get(i).getCommanderId());
            }
        }

        packet.reply(response);

    }

    @PacketProcessor
    public void onMoveShipTeam(RequestMoveShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        if (packet.getPosX() < 0 || packet.getPosY() < 0) {
            return;
        }
        if (packet.getPosX() > 24 || packet.getPosY() > 24) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());

        if (fleet == null || fleet.getGuid() != packet.getGuid() || fleet.getGalaxyId() == -1) {
            return;
        }
        if (fleet.isMatch() || fleet.isInMatch() || fleet.isInTransmission()) {
            return;
        }

        Planet planet = GalaxyService.getInstance().getPlanet(new GalaxyTile(fleet.getGalaxyId()));
        if (planet != null && planet.isInWar()) {
            return;
        }

        Optional<LoggedGameUser> loggedGameUser = user.getLoggedGameUser();
        if (loggedGameUser.isPresent() && loggedGameUser.get().getMatchViewing() != null) {
            return;
        }


        fleet.setPosX(packet.getPosX());
        fleet.setPosY(packet.getPosY());

        fleet.save();

        ResponseMoveShipTeamPacket response = new ResponseMoveShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(fleet.getGalaxyId());

        response.setShipTeamId(fleet.getShipTeamId());
        response.setPosX(fleet.getPosX());
        response.setPosY(fleet.getPosY());

        for (LoggedGameUser gameUser : LoginService.getInstance().getPlanetViewers(fleet.getGalaxyId())) {
            gameUser.getSmartServer().send(response);
        }

    }

    @PacketProcessor
    public void onDirectionShipTeam(RequestDirectionShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }
        if (packet.getDirection() < 0 || packet.getDirection() > 3) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());

        if (fleet == null || fleet.getGalaxyId() == -1 || fleet.getGuid() != packet.getGuid() || fleet.isInTransmission() || fleet.isInMatch()) {
            return;
        }
        if (fleet.isMatch() || fleet.isInMatch()) {
            return;
        }

        fleet.setDirection(packet.getDirection());
        fleet.save();

        ResponseDirectionShipTeamPacket response = new ResponseDirectionShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(fleet.getGalaxyId());

        response.setShipTeamId(fleet.getShipTeamId());
        response.setDirection((byte) fleet.getDirection());

        for (LoggedGameUser gameUser : LoginService.getInstance().getPlanetViewers(fleet.getGalaxyId())) {
            gameUser.getSmartServer().send(response);
        }

    }

    @PacketProcessor
    public void onDisbandShipTeam(RequestDisbandShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());
        if (fleet == null || fleet.getGuid() != packet.getGuid() || fleet.isInTransmission() || fleet.isInMatch()) {
            return;
        }

        UserShips ships = user.getShips();

        for (ShipTeamNum teamNum : fleet.getFleetBody().getCells()) {
            if (teamNum.getNum() > 0 && teamNum.getShipModelId() > -1) {
                ships.addShip(teamNum.getShipModelId(), teamNum.getNum());
            }
        }

        fleet.remove();

        user.getResources().addHe3(fleet.getHe3());

        user.update();
        user.save();

        ResponseDeleteShipTeamBroadcastPacket response = new ResponseDeleteShipTeamBroadcastPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(fleet.getGalaxyId());
        response.setShipTeamId(fleet.getShipTeamId());

        for (LoggedGameUser gameUser : LoginService.getInstance().getPlanetViewers(fleet.getGalaxyId())) {
            gameUser.getSmartServer().send(response);
        }

        packet.reply(ResourcesService.getInstance().getPlayerResourcePacket(user));

    }

    @PacketProcessor
    public void onUnionShipTeam(RequestUnionShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
        }

    }

    @PacketProcessor
    public void onShipTeamGoHome(RequestShipTeamGoHomePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserPlanet planet = user.getPlanet();
        if (planet == null) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());
        if (fleet == null || fleet.getGuid() != packet.getGuid() || fleet.isInTransmission() || fleet.isInMatch() || planet.isInWar()) {
            return;
        }

        GalaxyTile targetTile = planet.getPosition();
        GalaxyTile from = new GalaxyTile(fleet.getGalaxyId());

        double currentDistance = from.getManhattanDistance(targetTile);

        double allyFlyBonus = 0;
        UserBuilding building = user.getBuildings().getBuilding("build:alliance");
        if (building != null) {
            allyFlyBonus = building.getLevelData().getEffect("allyFlyBonus").getValue();
        }

        Planet fromPlanet = PacketService.getInstance().getPlanetCache().findByPosition(from);
        if (fromPlanet != null && fromPlanet.getType() == PlanetType.RESOURCES_PLANET) {
            allyFlyBonus = 1;
        }
        double currentTransmission = calculateTransmission(fleet, currentDistance, user.getCommander(fleet.getCommanderId()), allyFlyBonus);

        List<JumpShipTeamInfo> teamInfos = new ArrayList<>();

        Date until = DateUtil.now((int) currentTransmission);
        int spare = DateUtil.remains(until).intValue();

        fleet.setFleetTransmission(FleetTransmission.builder()
            .galaxyId(targetTile.galaxyId())
            .jumpType(JumpType.RECALL)
            .total(spare)
            .until(until)
            .build());

        teamInfos.add(JumpShipTeamInfo.builder()
            .userId(user.getUserId())
            .userName(user.getUsername())
            .shipTeamId(fleet.getShipTeamId())
            .fromGalaxyId(fleet.getGalaxyId())
            .toGalaxyId(targetTile.galaxyId())
            .spareTime(spare)
            .totalTime(spare)
            .fromGalaxyMapId(0)
            .toGalaxyMapId(0)
            .kind((byte) 2)
            .galaxyType((byte) 1)
            .build());

        fleet.save();

        for (JumpShipTeamInfo teamInfo : teamInfos) {

            ResponseJumpShipTeamPacket response = new ResponseJumpShipTeamPacket();
            response.setData(teamInfo);

            List<LoggedGameUser> loggedGameUsers = LoginService.getInstance().getPlanetViewers(teamInfo.getFromGalaxyId());

            for (LoggedGameUser loggedGameUser : loggedGameUsers) {

                ResponseDeleteShipTeamBroadcastPacket broadcast = new ResponseDeleteShipTeamBroadcastPacket();

                broadcast.setGalaxyMapId(0);
                broadcast.setGalaxyId(teamInfo.getFromGalaxyId());
                broadcast.setShipTeamId(teamInfo.getShipTeamId());

                loggedGameUser.getSmartServer().send(broadcast);

            }

            packet.reply(response);

        }

    }


    private static double calculateTransmission(Fleet fleet, double distance, Commander commanderCache, double allyFlyBonus) {
        double transmission = fleet.getTransmissionRate() * distance + fleet.getTransmissionStart();
        transmission = transmission - commanderCache.getChipAdditionalTransmission();
        transmission = transmission * (1 - allyFlyBonus);
        transmission = Math.max(transmission, 0);
        return transmission;
    }

}