package com.go2super.service.battle.match;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.BattleFort;
import com.go2super.database.entity.sub.BattleTag;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.fight.ResponseFightResultPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.RaidsService;
import com.go2super.service.UserService;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.type.AttackSideType;
import com.go2super.service.battle.type.BattleResultType;
import com.go2super.service.battle.type.StopCause;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class RaidMatch  extends Match {
    private int defender1;
    private int defender2;
    private int attacker;
    private List<BattleTag> tags = new ArrayList<>();
    @Override
    public void stop(StopCause cause) {
        Optional<LoggedGameUser> optionalOwner1 = LoginService.getInstance().getGame(defender1);
        Optional<LoggedGameUser> optionalOwner2 = LoginService.getInstance().getGame(defender2);
        Optional<LoggedGameUser> optionalOpponent = LoginService.getInstance().getGame(attacker);

        ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

        response.setEctypeId((short) 0);
        response.setGateId(UnsignedChar.of(0));
        response.setState((byte) 0);

        boolean targetSent = false;
        boolean sourceSent = false;

        if (StopCause.MANUAL.equals(cause)) {
            optionalOpponent.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(response));
            optionalOwner1.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(response));
            optionalOwner2.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(response));
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

        ResponseFightResultPacket fightResultPacket = BattleService.getInstance().getRaidFightResult(this, resultType);
        for (LoggedGameUser viewer : getViewers()) {
            if (viewer.getGuid() == defender1 || viewer.getGuid() == defender2) {
                targetSent = true;
                viewer.getSmartServer().send(response);
            } else if (viewer.getGuid() == attacker) {
                sourceSent = true;
                viewer.getSmartServer().send(response);
            }
            viewer.getSmartServer().send(fightResultPacket);
        }

        if (!sourceSent && optionalOpponent.isPresent()) {
            optionalOpponent.get().getSmartServer().send(response);
        }

        if (!targetSent && optionalOwner1.isPresent()) {
            optionalOwner1.get().getSmartServer().send(response);
        }

        if (!targetSent && optionalOwner2.isPresent()) {
            optionalOwner2.get().getSmartServer().send(response);
        }

        // Reset raid room after intercept battle — give rewards to defenders if they won
        // Find room by either defender (the other may have disconnected mid-battle)
        var raidOpt = RaidsService.getInstance().getRaids().stream()
                .filter(r -> (r.getFirstGuid() == defender1 || r.getSecondGuid() == defender1
                        || r.getFirstGuid() == defender2 || r.getSecondGuid() == defender2)
                        && r.getStatus() == com.go2super.service.raids.RaidStatus.INTERCEPTED)
                .findFirst();
        raidOpt.ifPresent(raid -> {
            if (hasWon()) {
                if (raid.getFirstGuid() != -1) {
                    RaidsService.getInstance().giveRewards(defender1, raid.getFirstPropId());
                }
                if (raid.getSecondGuid() != -1) {
                    RaidsService.getInstance().giveRewards(defender2, raid.getSecondPropId());
                }
            }
            raid.setTime(-1);
            raid.setFirstGuid(-1);
            raid.setSecondGuid(-1);
            raid.setFirstPropId(-1);
            raid.setSecondPropId(-1);
            raid.setStatus(com.go2super.service.raids.RaidStatus.EMPTY);
            raid.setFirstDefenceFleets(new ArrayList<>());
            raid.setSecondDefenceFleets(new ArrayList<>());
            RaidsService.getInstance().broadcastStatus();
        });
    }
    public boolean hasWon() {
        return getFleets().stream().noneMatch(BattleFleet::isAttacker);
    }
    public boolean hasLost() {
        return getFleets().stream().allMatch(BattleFleet::isAttacker);
    }
    @Override
    public void updateFleet(BattleFleet battleFleet) {

    }

    @Override
    public void removeFleet(BattleFleet battleFleet) {

    }

    @Override
    public void removeFort(BattleFort battleFort) {

    }

    @Override
    public void returnAllFleets() {

    }

    @Override
    public AttackSideType fortressAttackType() {
        return null;
    }

    @Override
    public boolean hasUser(int guid) {

        return defender1 == guid || defender2 == guid || attacker == guid;
    }
}
