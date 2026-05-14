package com.go2super.service;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.Email;
import com.go2super.database.entity.sub.EmailGood;
import com.go2super.database.entity.sub.UserEmailStorage;
import com.go2super.obj.game.CaptureArk;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.packet.raids.ResponseCaptureArkInfoPacket;
import com.go2super.packet.raids.ResponseCaptureArkListPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.service.raids.Raid;
import com.go2super.service.raids.RaidStatus;
import com.go2super.socket.util.DateUtil;
import com.go2super.resources.ResourceManager;
import com.go2super.service.UserService;
import lombok.Getter;
import org.apache.tomcat.jni.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Service
public class RaidsService {

    private static RaidsService instance;
    private final ArrayList<Raid> raids;
    private final ConcurrentMap<Integer, LocalDateTime> userIds;

    private final AtomicBoolean enabled = new AtomicBoolean(false);
    public RaidsService() {

        instance = this;
        raids = new ArrayList<>();
        userIds = new ConcurrentHashMap<>();
        addRooms();

    }

    public ResponseCaptureArkInfoPacket getArkInfoPacket(User user) {

        ResponseCaptureArkInfoPacket captureArkInfoPacket = new ResponseCaptureArkInfoPacket();
        var room = raids.stream().filter(x -> x.getFirstGuid() == user.getGuid() || x.getSecondGuid() == user.getGuid()).findFirst();
        if(room.isPresent()){
            captureArkInfoPacket.setRoomId(UnsignedChar.of(room.get().getRoomId()));
            captureArkInfoPacket.setCountdown(UnsignedShort.of(room.get().getTime()));

            captureArkInfoPacket.setCapture(UnsignedChar.of((5 << 4) | user.getStats().getRaidInterceptEntries())); // (max << 4) | current
            captureArkInfoPacket.setSearch(UnsignedChar.of((5 << 4) | user.getStats().getRaidAttemptsEntries())); // (max << 4) | current

            // 0 = Can fleet
            // 4 = View Room
            // 5 = Hides buttons (can't fleet)
            captureArkInfoPacket.setPlace(UnsignedChar.of(room.get().getRoomStatus(user.getGuid())));
        }
        else{
            captureArkInfoPacket.setRoomId(UnsignedChar.of(0));
            captureArkInfoPacket.setCountdown(UnsignedShort.of(0));

            captureArkInfoPacket.setCapture(UnsignedChar.of((5 << 4) | user.getStats().getRaidInterceptEntries())); // (max << 4) | current
            captureArkInfoPacket.setSearch(UnsignedChar.of((5 << 4) | user.getStats().getRaidAttemptsEntries())); // (max << 4) | current

            // 0 = Can fleet
            // 5 = Hides buttons (can't fleet)
            captureArkInfoPacket.setPlace(UnsignedChar.of(0));
        }

        // 0 = Ends in
        // 1 = Starts in
        // 2 = In progress
        captureArkInfoPacket.setSpareType(UnsignedChar.of(0));
        captureArkInfoPacket.setSpareTime(UnsignedInteger.of(360));
        return captureArkInfoPacket;

    }

    public ResponseCaptureArkListPacket getArkRoomsPacket(User user) {

        ResponseCaptureArkListPacket captureArkListPacket = new ResponseCaptureArkListPacket();

        captureArkListPacket.setCaptureFleets(UnsignedChar.of(4));
        captureArkListPacket.setSearchFleets(UnsignedChar.of(3));
        captureArkListPacket.setReserve(UnsignedChar.of(0));
        List<CaptureArk> rooms = new ArrayList<>();
        for(int x = 0; x < raids.size(); x++){
            var raid = raids.get(x);
            CaptureArk captureArk = new CaptureArk(raid.getFirstPropId(), raid.getSecondPropId(), Math.max(raid.getTime(), 0), raid.getRoomStatus(user.getGuid()), raid.getRoomId());
            rooms.add(captureArk);
        }
        captureArkListPacket.setRooms(rooms);
        captureArkListPacket.setDataLen(UnsignedChar.of(rooms.size()));

        return captureArkListPacket;

    }

    public void broadcastStatus(){
        for(var guid: getUserIds().keySet()){
            var player = UserService.getInstance().getUserCache().findByGuid(guid);
            if(player != null && player.isOnline()){
                player.getLoggedGameUser().ifPresent(x -> x.getSmartServer().send(getArkRoomsPacket(player)));
            }
        }
    }

    @Scheduled(fixedDelay = 10000L)
    public void removeExpiredPlayers(){
        new HashSet<>(userIds.keySet()).forEach(guid -> {
            var date = userIds.get(guid);
            if(date != null && Math.abs(Duration.between(date, LocalDateTime.now()).toHours()) >= 1){
                userIds.remove(guid);
            }
        });
    }

    @Scheduled(fixedDelay = 1000L)
    public void runRaid(){
        if (!getInstance().getEnabled().get()) {
            return;
        }
        AtomicBoolean changed = new AtomicBoolean(false);
        raids.forEach(x -> {
            if(x.getSecondGuid() > 0 && x.getFirstGuid() > 0){
                if(x.getStatus() == RaidStatus.WAITING){
                    x.setTime(60);
                    x.setStatus(RaidStatus.IN_PROGRESS);
                } else if(x.getTime() > 0 && x.getStatus() == RaidStatus.IN_PROGRESS){
                    x.setTime(x.getTime() - 1);
                } else if(x.getTime() <= 0){
                    //give mail rewards to players, defend success!
                    if (x.getFirstGuid() > 0) {
                        var p1 = giveRewards(x.getFirstGuid(), x.getFirstPropId());
                        if (p1 != null) {
                            p1.getLoggedGameUser().ifPresent((e) -> {
                                e.getSmartServer().send(ResponseNewEmailNoticePacket.builder().errorCode(0).build());
                                e.getSmartServer().send(getArkInfoPacket(p1));
                            });
                        }
                    }
                    if (x.getSecondGuid() > 0) {
                        var p2 = giveRewards(x.getSecondGuid(), x.getSecondPropId());
                        if (p2 != null) {
                            p2.getLoggedGameUser().ifPresent((e) -> {
                                e.getSmartServer().send(ResponseNewEmailNoticePacket.builder().errorCode(0).build());
                                e.getSmartServer().send(getArkInfoPacket(p2));
                            });
                        }
                    }
                    x.setTime(-1);
                    x.setFirstGuid(-1);
                    x.setSecondGuid(-1);
                    x.setFirstPropId(-1);
                    x.setSecondPropId(-1);
                    x.setStatus(RaidStatus.EMPTY);
                    x.setFirstDefenceFleets(new ArrayList<>());
                    x.setSecondDefenceFleets(new ArrayList<>());
                    changed.set(true);
                }
            }
        });
        if(changed.get()){
            broadcastStatus();
        }
    }

    private User giveRewards(int guid, int propId) {
        var player = UserService.getInstance().getUserCache().findByGuid(guid);
        if (player == null) {
            return null;
        }
        player.getStats().setRaidAttemptsEntries(player.getStats().getRaidAttemptsEntries() + 1);
        var reward = ResourceManager.getRewardJson().getReward(propId);
        UserEmailStorage userEmailStorage = player.getUserEmailStorage();
        var emailGoods = new ArrayList<EmailGood>();
        if (reward != null) {
            emailGoods.add(EmailGood.builder().goodId(reward.getPropId()).lockNum(reward.getAmount()).build());
        }
        userEmailStorage.addEmail(Email.builder()
                .autoId(userEmailStorage.nextAutoId())
                .type(2)
                .name("System")
                .subject("Raid Rewards")
                .emailContent("Raid defend success")
                .readFlag(0)
                .date(DateUtil.now())
                .guid(-1)
                .goods(emailGoods).build());
        player.save();
        return player;
    }

    private void addRooms() {

        for (int i = 0; i < 6; i++) {

            Raid raid = new Raid();

            raid.setStatus(RaidStatus.EMPTY);

            raid.setFirstGuid(-1);
            raid.setSecondGuid(-1);

            raid.setFirstPropId(-1);
            raid.setSecondPropId(-1);
            raid.setRoomId(i);
            raid.setTime(-1);
            raids.add(raid);

        }

    }

    public static RaidsService getInstance() {

        return instance;
    }

}
