package com.go2super.service.battle.match;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.fight.ResponseArenaStatusPacket;
import com.go2super.packet.fight.ResponseFightResultPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.type.AttackSideType;
import com.go2super.service.battle.type.StopCause;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.stream.*;

@Getter
@Setter
@ToString(callSuper = true)
public class ArenaMatch extends Match {

    private boolean sourceStop = false;
    private boolean targetStop = false;

    private int sourceGuid = -1;
    private int targetGuid = -1;

    private long sourceUserId = -1;
    private long targetUserId = -1;

    private String sourceUsername = "";
    private String targetUsername = "";

    private List<Integer> sourceIds = new ArrayList<>();
    private List<Integer> targetIds = new ArrayList<>();

    private int sourceSend = 0;
    private int targetSend = 0;

    private int password;

    private List<BattleTag> tags = new ArrayList<>();
    private List<BattleRound> rounds = new ArrayList<>();

    private List<BattleFleet> removedFleets = new ArrayList<>();

    public ArenaMatch(int password) {

        this.password = password;
    }

    public boolean start() {

        Optional<LoggedGameUser> optionalOwner = LoginService.getInstance().getGame(sourceUserId);
        Optional<LoggedGameUser> optionalOpponent = LoginService.getInstance().getGame(targetUserId);

        GameBattle gameBattle = BattleService.getInstance().getBattle(this);

        if (optionalOwner.isEmpty() || optionalOpponent.isEmpty() || gameBattle == null) {
            return false;
        }

        LoggedGameUser owner = optionalOwner.get();
        LoggedGameUser opponent = optionalOpponent.get();

        ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

        response.setEctypeId((short) 1001);
        response.setGateId(UnsignedChar.of(0));
        response.setState((byte) 1);

        owner.setMatchViewing(getId());
        opponent.setMatchViewing(getId());

        opponent.getSmartServer().send(response);
        response.setState((byte) 1);
        owner.getSmartServer().send(response);

        int maxRound = 20 + (getFleets().size());
        maxRound = maxRound > 100 ? 100 : maxRound;

        this.getPause().set(false);
        this.setMaxRound(maxRound);

        gameBattle.getThread().start();
        return true;

    }

    public Optional<LoggedGameUser> getOptionalOwner() {

        return LoginService.getInstance().getGame(sourceUserId);
    }

    public Optional<LoggedGameUser> getOptionalOpponent() {

        return LoginService.getInstance().getGame(targetUserId);
    }

    public void opponentExit(boolean kicked) {

        if (!getPause().get() || targetGuid == -1) {
            return;
        }

        List<BattleFleet> targetFleets = getFleets().stream().filter(fleet -> fleet.getGuid() == targetGuid).collect(Collectors.toList());

        User targetUser = UserService.getInstance().getUserCache().findByGuid(targetGuid);
        returnFleets(targetFleets, targetUser.getGalaxyId());

        int exitGuid = targetGuid;
        String exitUsername = targetUsername;

        if (!kicked) {

            targetGuid = -1;
            targetUserId = -1;
            targetUsername = "";
            targetSend = 0;

            targetIds.clear();

            Optional<LoggedGameUser> optionalOwner = LoginService.getInstance().getGame(sourceUserId);

            if (optionalOwner.isPresent()) {

                LoggedGameUser owner = optionalOwner.get();

                ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

                status.setGuid(exitGuid);
                status.setCName(SmartString.of(exitUsername, 32));

                status.setRoomId(sourceGuid);
                status.setRequest(UnsignedChar.of(2));

                status.setStatus((byte) 1);

                // owner.getSmartServer().send(BattleService.getInstance().getCurrentEctypeState(targetUser));
                owner.getSmartServer().send(status);

            }

        }

        Optional<LoggedGameUser> optionalOpponent = LoginService.getInstance().getGame(targetUserId);

        if (optionalOpponent.isPresent()) {

            LoggedGameUser opponent = optionalOpponent.get();

            ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

            status.setGuid(exitGuid);
            status.setCName(SmartString.of(exitUsername, 32));

            status.setRoomId(sourceGuid);
            status.setRequest(UnsignedChar.of(1));

            status.setStatus((byte) 1);

            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

            response.setEctypeId((short) 1001);
            response.setGateId(UnsignedChar.of(0));
            response.setState((byte) 0);

            opponent.getSmartServer().send(response);
            opponent.getSmartServer().send(status);

        }

        targetGuid = -1;
        targetUserId = -1;
        targetUsername = "";
        targetSend = 0;

        targetIds.clear();

    }

    public void returnFleets(List<BattleFleet> targetFleets, int galaxyId) {

        for (BattleFleet battleFleet : targetFleets) {

            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(battleFleet.getShipTeamId());

            if (fleet == null) {
                continue;
            }

            Commander commander = fleet.getCommander();
            if (commander != null) {
                commander.save();
            }

            fleet.setMatch(false);
            fleet.setFleetMatch(null);
            fleet.setGalaxyId(galaxyId);
            fleet.save();

        }

    }

    @Override
    public void stop(StopCause cause) {

        returnAllFleets();

        Optional<LoggedGameUser> optionalOwner = LoginService.getInstance().getGame(sourceUserId);
        Optional<LoggedGameUser> optionalOpponent = LoginService.getInstance().getGame(targetUserId);

        ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

        response.setEctypeId((short) 1001);
        response.setGateId(UnsignedChar.of(0));
        response.setState((byte) 0);

        ResponseArenaStatusPacket status = new ResponseArenaStatusPacket();

        status.setGuid(-1);
        status.setCName(SmartString.of("", 32));

        status.setRoomId(-1); // sourceGuid
        status.setRequest(UnsignedChar.of(6));

        status.setStatus((byte) 1);

        boolean targetSent = false;
        boolean sourceSent = false;

        ResponseFightResultPacket fightResultPacket = BattleService.getInstance().getArenaFightResult(this, hasWon());

        for (LoggedGameUser viewer : getViewers()) {

            if (viewer.getGuid() == targetGuid) {

                targetSent = true;
                viewer.setMatchViewing(null);
                viewer.getSmartServer().send(response);

            } else if (viewer.getGuid() == sourceGuid) {

                sourceSent = true;
                viewer.setMatchViewing(null);
                viewer.getSmartServer().send(response);

            }

            viewer.getSmartServer().send(status);
            viewer.getSmartServer().send(fightResultPacket);

        }

        if (!targetSent && optionalOpponent.isPresent()) {

            optionalOpponent.get().getSmartServer().send(response);
            optionalOpponent.get().setMatchViewing(null);
            optionalOpponent.get().getSmartServer().send(status);

        }

        if (!sourceSent && optionalOwner.isPresent()) {

            optionalOwner.get().getSmartServer().send(response);
            optionalOwner.get().setMatchViewing(null);
            optionalOwner.get().getSmartServer().send(status);

        }

    }

    public boolean hasWon() {

        return !getFleets().stream().anyMatch(fleet -> fleet.isAttacker());
    }

    @Override
    public void updateFleet(BattleFleet battleFleet) {

    }

    @Override
    public void removeFleet(BattleFleet battleFleet) {

        getFleets().remove(battleFleet);
        removedFleets.add(battleFleet);

    }

    @Override
    public void removeFort(BattleFort battleFort) {

        // ? this never happens

    }

    @Override
    public void returnAllFleets() {

        User sourceUser = UserService.getInstance().getUserCache().findByGuid(sourceGuid);
        User targetUser = UserService.getInstance().getUserCache().findByGuid(targetGuid);

        UserPlanet sourcePlanet = sourceUser.getPlanet();
        UserPlanet targetPlanet = targetUser == null ? null : targetUser.getPlanet();

        for (BattleFleet userFleet : getAllFleets()) {

            if (userFleet.isPirate()) {
                continue;
            }

            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(userFleet.getShipTeamId());

            if (fleet == null) {
                continue;
            }

            Commander commander = fleet.getCommander();
            if (commander != null) {
                commander.save();
            }

            UserPlanet planet = userFleet.getGuid() == sourceGuid ? sourcePlanet : targetPlanet;

            fleet.setMatch(false);
            fleet.setFleetMatch(null);
            fleet.setGalaxyId(planet.getPosition().galaxyId());
            fleet.save();
        }
    }

    @Override
    public AttackSideType fortressAttackType() {

        return AttackSideType.NONE;
    }

    @Override
    public boolean hasUser(int guid) {

        return sourceGuid == guid || targetGuid == guid;
    }

    public boolean addOpponent(User user, List<Integer> fleetIds) {

        List<Fleet> fleets = BattleService.getInstance().getRealFleets(user, fleetIds);

        if (fleets == null) {
            return false;
        }

        for (Fleet fleet : fleets) {

            if (fleet.getGuid() != user.getGuid()) {
                return false;
            }

            if (fleet.isMatch()) {
                return false;
            }

        }

        setTargetGuid(user.getGuid());
        setTargetUserId(user.getUserId());
        setTargetUsername(user.getUsername());
        setTargetSend(BattleService.getInstance().countShips(fleets));

        getFleets().addAll(BattleService.getInstance().createBattleFleets(fleets, true, this));
        return true;

    }

    public List<BattleFleet> getAllFleets(int guid) {

        return getAllFleets().stream().filter(fleet -> fleet.getGuid() == guid).collect(Collectors.toList());
    }

    public List<BattleFleet> getAllFleets() {

        return Stream.concat(getFleets().stream(), getRemovedFleets().stream()).collect(Collectors.toList());
    }

}
