package com.go2super.service.battle.match;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.BattleFort;
import com.go2super.database.entity.sub.BattleTag;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.fight.ResponseFightResultPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.service.*;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.type.AttackSideType;
import com.go2super.service.battle.type.BattleResultType;
import com.go2super.service.battle.type.StopCause;
import com.go2super.service.league.LeagueMatchService;
import com.go2super.service.league.LeagueRankService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@ToString(callSuper = true)
public class LeagueMatch extends Match {
    private int sourceGuid = -1;
    private int targetGuid = -1;
    private long sourceUserId = -1;
    private long targetUserId = -1;
    private List<BattleTag> tags = new ArrayList<>();
    private List<BattleFleet> removedFleets = new ArrayList<>();
    private int leagueId;

    @Override
    public void stop(StopCause cause) {
        if (sourceGuid == -1 && targetGuid == -1) {
            return;
        }
        returnAllFleets();

        Optional<LoggedGameUser> optionalOwner = LoginService.getInstance().getGame(sourceUserId);
        Optional<LoggedGameUser> optionalOpponent = LoginService.getInstance().getGame(targetUserId);

        ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

        response.setEctypeId((short) 1000);
        response.setGateId(UnsignedChar.of(0));
        response.setState((byte) 0);

        User player1 = UserService.getInstance().getUserCache().findByUserId(sourceUserId);
        User player2 = UserService.getInstance().getUserCache().findByUserId(targetUserId);

        boolean targetSent = false;
        boolean sourceSent = false;

        if (StopCause.MANUAL.equals(cause)) {
            optionalOpponent.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(response));
            optionalOwner.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(response));
            return;
        }

        BattleResultType resultType;
        if (hasWon()) {
            resultType = BattleResultType.WIN;
        } else if (hasLost()) {
            resultType = BattleResultType.LOSE;
        } else {
            resultType = BattleResultType.DRAW;
        }

        ResponseFightResultPacket fightResultPacket = BattleService.getInstance().getLeagueFightResult(this, resultType);
        for (LoggedGameUser viewer : getViewers()) {
            if (viewer.getGuid() == targetGuid) {
                targetSent = true;
                viewer.getSmartServer().send(response);
            } else if (viewer.getGuid() == sourceGuid) {
                sourceSent = true;
                viewer.getSmartServer().send(response);
            }
            viewer.getSmartServer().send(fightResultPacket);
        }

        if (!targetSent && optionalOpponent.isPresent()) {
            optionalOpponent.get().getSmartServer().send(response);
        }

        if (!sourceSent && optionalOwner.isPresent()) {
            optionalOwner.get().getSmartServer().send(response);
        }


        if(optionalOwner.isPresent()){
            optionalOwner.get().setViewing(player1.getGalaxyId());
            optionalOwner.get().setMatchViewing(null);
        }
        if(optionalOpponent.isPresent()){
            optionalOpponent.get().setViewing(player2.getGalaxyId());
            optionalOpponent.get().setMatchViewing(null);
        }

        if (hasWon()) {
            LeagueRankService.getInstance().addWin(player2);
            LeagueRankService.getInstance().addLoss(player1);
        } else if (hasLost()) {
            LeagueRankService.getInstance().addWin(player1);
            LeagueRankService.getInstance().addLoss(player2);
        } else if (hasDrawn()) {
            LeagueRankService.getInstance().addDraw(player1);
            LeagueRankService.getInstance().addDraw(player2);
        }

        if (player1.getLeagueCount() == 0) {
            LeagueMatchService.getInstance().sendMailReward(player1);
        }
        if (player2.getLeagueCount() == 0) {
            LeagueMatchService.getInstance().sendMailReward(player2);
        }

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

    }

    @Override
    public void returnAllFleets() {
        User sourceUser = UserService.getInstance().getUserCache().findByGuid(sourceGuid);
        User targetUser = UserService.getInstance().getUserCache().findByGuid(targetGuid);

        UserPlanet sourcePlanet = sourceUser.getPlanet();
        UserPlanet targetPlanet = targetUser == null ? null : targetUser.getPlanet();

        for (BattleFleet userFleet : getAllFleets()) {
            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(userFleet.getShipTeamId());

            if (fleet == null) {
                continue;
            }

            UserPlanet planet = userFleet.getGuid() == sourceGuid ? sourcePlanet : targetPlanet;

            fleet.setMatch(false);
            fleet.setFleetMatch(null);
            fleet.setGalaxyId(planet.getPosition().galaxyId());
            fleet.save();
        }
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
        maxRound = Math.min(maxRound, 100);
        this.setMaxRound(maxRound);
        this.getPause().set(false);
        return true;
    }

    public void exit(int guid) {
        if (!getPause().get()) {
            return;
        }

        if (sourceGuid != guid && targetGuid != guid) {
            return;
        }

        boolean isSource = sourceGuid == guid;

        List<BattleFleet> targetFleets = getFleets().stream().filter(fleet -> fleet.getGuid() == guid).toList();
        User targetUser = UserService.getInstance().getUserCache().findByGuid(guid);
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
            fleet.setGalaxyId(targetUser.getGalaxyId());
            fleet.save();
        }
        getFleets().removeAll(targetFleets);
        if (isSource) {
            setSourceGuid(-1);
            setSourceUserId(-1);
        } else {
            setTargetGuid(-1);
            setTargetUserId(-1);
        }

        Optional<LoggedGameUser> target = LoginService.getInstance().getGame(guid);
        if (target.isPresent()) {
            LoggedGameUser t = target.get();
            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
            response.setEctypeId((short) 1000);
            response.setGateId(UnsignedChar.of(0));
            response.setState((byte)0);
            t.getSmartServer().send(response);
        }
    }

    public boolean addOpponent(User user, List<Integer> fleetIds, boolean isTarget) {
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

        if (isTarget) {
            setTargetGuid(user.getGuid());
            setTargetUserId(user.getUserId());
            getFleets().addAll(BattleService.getInstance().createBattleFleets(fleets, true, this));
        } else {
            setSourceGuid(user.getGuid());
            setSourceUserId(user.getUserId());
            getFleets().addAll(BattleService.getInstance().createBattleFleets(fleets, false, this));
        }
        return true;

    }

    public boolean hasWon() {
        return getFleets().stream().noneMatch(BattleFleet::isAttacker);
    }

    public boolean hasLost() {
        return getFleets().stream().allMatch(BattleFleet::isAttacker);
    }

    public boolean hasDrawn() {
        return getFleets().stream().anyMatch(BattleFleet::isAttacker) && getFleets().stream().anyMatch(fleet -> !fleet.isAttacker());
    }

    public List<BattleFleet> getAllFleets(int guid) {

        return getAllFleets().stream().filter(fleet -> fleet.getGuid() == guid).collect(Collectors.toList());
    }

    public List<BattleFleet> getAllFleets() {

        return Stream.concat(getFleets().stream(), getRemovedFleets().stream()).collect(Collectors.toList());
    }

    @Override
    public AttackSideType fortressAttackType() {
        return null;
    }

    @Override
    public boolean hasUser(int guid) {
        return getAllFleets(guid).size() > 0;
    }
}
