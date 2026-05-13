package com.go2super.service.battle.match;


import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.MailType;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import com.go2super.packet.fight.ResponseFightResultPacket;
import com.go2super.packet.igl.ResponseDuplicateStatusPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.service.BattleService;
import com.go2super.service.IGLService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.type.AttackSideType;
import com.go2super.service.battle.type.BattleResultType;
import com.go2super.service.battle.type.StopCause;
import com.go2super.socket.util.DateUtil;
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
public class IglMatch extends Match {
    private int sourceGuid = -1;
    private int targetGuid = -1;
    private long sourceUserId = -1;
    private long targetUserId = -1;
    private List<BattleTag> tags = new ArrayList<>();
    private List<BattleFleet> removedFleets = new ArrayList<>();

    @Override
    public void stop(StopCause cause) {
        if (sourceGuid == -1 && targetGuid == -1) {
            return;
        }
        Optional<LoggedGameUser> optionalOwner = LoginService.getInstance().getGame(sourceUserId);
        User player1 = UserService.getInstance().getUserCache().findByUserId(sourceUserId);

        if (StopCause.MANUAL.equals(cause)) {
            return;
        }

        UserEmailStorage userEmailStorage = player1.getUserEmailStorage();
        Email email = Email.builder()
                .autoId(userEmailStorage.nextAutoId())
                .type(2)
                .mailType(MailType.IGL)
                .name("System")
                .readFlag(0)
                .subject("")
                .date(DateUtil.now())
                .goods(new ArrayList<>())
                .guid(-1)
                .build();


        ResponseNewEmailNoticePacket packet = ResponseNewEmailNoticePacket.builder()
                .errorCode(0)
                .build();


        BattleResultType resultType;
        if (hasWon()) {
            resultType = BattleResultType.WIN;
            IGLService.getInstance().win((int) sourceUserId, (int) targetUserId);
            email.addGood(EmailGood.builder()
                    .goodId(931)
                    .lockNum(10)
                    .build());
            email.setGuid(1);
            email.setEmailContent("Boss178");
        } else if (hasLost()) {
            resultType = BattleResultType.LOSE;
            email.addGood(EmailGood.builder()
                    .goodId(934)
                    .lockNum(10)
                    .build());
            email.setEmailContent("Boss179");
            IGLService.getInstance().lossOrDraw((int) sourceUserId, (int) targetUserId);
        } else {
            resultType = BattleResultType.DRAW;
            email.addGood(EmailGood.builder()
                    .goodId(934)
                    .lockNum(10)
                    .build());
            email.setEmailContent("Boss179");
        }

        userEmailStorage.addEmail(email);
        optionalOwner.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(packet));


        if (optionalOwner.isPresent()) {
            for (Packet p : getPackets()) {
                if (optionalOwner.get().getMatchViewing() == null || !optionalOwner.get().getMatchViewing().equals(this.getId())) {
                    break;
                }
                optionalOwner.get().getSmartServer().send(p);
                try {
                    Thread.sleep(p.getMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (optionalOwner.get().getMatchViewing() != null && optionalOwner.get().getMatchViewing().equals(this.getId())) {
                ResponseFightResultPacket fightResultPacket = BattleService.getInstance().getIglFightResult(this, resultType);
                optionalOwner.get().setViewing(player1.getGalaxyId());
                optionalOwner.get().setMatchViewing(null);
                ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
                response.setEctypeId((short) 1002);
                response.setGateId(UnsignedChar.of(0));
                response.setState((byte) 0);

                optionalOwner.get().getSmartServer().send(response, fightResultPacket);

                Optional<Match> optionalMatch2 = BattleService.getInstance().getVirtual(sourceGuid);
                if (optionalMatch2.isPresent()) {
                    Match current2 = optionalMatch2.get();
                    if (!current2.getId().equals(this.getId())) {
                        ResponseEctypeStatePacket response4 = new ResponseEctypeStatePacket();
                        response4.setEctypeId((short) current2.getEctype());
                        response4.setGateId(UnsignedChar.of(0));
                        response4.setState((byte) 2);
                        optionalOwner.get().getSmartServer().send(response4);
                    }

                }
                ResponseDuplicateStatusPacket response2 = new ResponseDuplicateStatusPacket();
                response2.setGuid(sourceGuid);
                response2.setDuplicate(UnsignedChar.of(50));
                response2.setStatus(UnsignedChar.of(0));
                optionalOwner.get().getSmartServer().send(response2);
            }

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
        //
    }

    @Override
    public void returnAllFleets() {
        //
    }


    public boolean hasWon() {
        return getFleets().stream().allMatch(BattleFleet::isAttacker);
    }

    public boolean hasLost() {
        return getFleets().stream().noneMatch(BattleFleet::isAttacker);
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
        return !getAllFleets(guid).isEmpty();
    }
}
