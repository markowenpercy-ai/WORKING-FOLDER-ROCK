package com.go2super.service.jobs.other;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.BonusType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.service.ChatService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.*;

@Getter
@Setter
public class DefendJob implements OfflineJob {

    private static DefendJob instance;

    private long lastExecution = DateUtil.nowMillis(15000).getTime();

    private int currentFlag = -1;
    private int currentFlagGalaxyId = -1;
    private int currentFlagCorp = -1;

    private ScheduledFuture future;
    private boolean selection;
    private boolean register;

    private Set<Integer> participants = new HashSet<>();

    public DefendJob() {

        instance = this;

    }

    @Override
    public void setup() {

    }

    @Override
    public void run() {

        if (DateUtil.millis() - lastExecution < getInterval() || register || selection || currentFlag != -1 || future != null) {
            return;
        }

        lastExecution = DateUtil.millis();

        try {

            currentFlagCorp = -1;
            currentFlagGalaxyId = -1;
            currentFlag = -1;

            register = true;
            participants.clear();

            ChatService.getInstance().broadcastMessage("[Corp Base Defense] registers are opened!");
            ChatService.getInstance().broadcastMessage("Type: /" + ChatGameJob.getInstance().convert("enlist"));
            ChatService.getInstance().broadcastMessage("To enlist your planet as a base defender!");
            ChatService.getInstance().broadcastMessage("The draw will end in 1 minute!");

            register = false;
            selection = true;

            if (participants.isEmpty()) {

                ChatService.getInstance().broadcastMessage("No one enlisted! The draw will be canceled!");
                register = false;
                selection = false;
                return;

            }

            ChatService.getInstance().broadcastMessage("Raffling... (" + participants.size() + " participants)");

            List<Integer> shuffle = new ArrayList<>(participants);
            Collections.shuffle(shuffle);

            Integer selected = shuffle.get(0);
            User selectedUser = UserService.getInstance().getUserCache().findByGuid(selected);

            Corp corp = selectedUser.getCorp();
            UserPlanet userPlanet = selectedUser.getPlanet();

            if (selectedUser == null || corp == null || userPlanet == null) {

                ChatService.getInstance().broadcastMessage("No one enlisted! The draw will be canceled!");
                register = false;
                selection = false;
                return;

            }

            start(selectedUser, userPlanet, corp);

        } catch (Exception e) {

            register = false;
            e.printStackTrace();

        }

    }

    public void start(User user, UserPlanet userPlanet, Corp corp) {

        register = false;
        selection = false;

        currentFlagCorp = corp.getCorpId();
        currentFlagGalaxyId = userPlanet.getPosition().galaxyId();
        currentFlag = user.getGuid();

        if (user.getStats().hasTruce()) {
            user.getStats().removeBoost(BonusType.PLANET_PROTECTION);
        }

        user.update();
        user.save();

        GalaxyTile galaxyTile = userPlanet.getPosition();

        ChatService.getInstance().broadcastMessage("The planet [Name: " + user.getUsername() + ", ID: " + user.getGuid() + ", Corp: " + corp.getName() + "] is now the base defender!");
        ChatService.getInstance().broadcastMessage("Coordinates: (" + galaxyTile.getX() + ", " + galaxyTile.getY() + ")");
        for (LoggedGameUser online : LoginService.getInstance().getGameUsers()) {
            if (online.getUpdatedUser().getConsortiaId() == corp.getCorpId()) {
                online.getSmartServer().sendMessage("You have 1 hour to defend the Space Station! (Prize 1000 vouchers)");
            } else {
                online.getSmartServer().sendMessage("You have 1 hour to destroy the Space Station! (Prize 500 vouchers)");
            }
        }

    }

    public void future(User user, UserPlanet userPlanet, Corp corp) {

        register = false;
        selection = false;
        future = null;

        currentFlagCorp = -1;
        currentFlagGalaxyId = -1;
        currentFlag = -1;

        participants.clear();

        for (CorpMember corpMember : corp.getMembers().getMembers()) {

            User member = UserService.getInstance().getUserCache().findByGuid(corpMember.getGuid());
            if (member == null) {
                continue;
            }

            UserEmailStorage userEmailStorage = member.getUserEmailStorage();
            Email email = Email.builder()
                .autoId(userEmailStorage.nextAutoId())
                .type(2)
                .name("System")
                .subject("Base Defender Rewards")
                .emailContent(
                    "Here are the goods for our efforts!")
                .readFlag(0)
                .date(DateUtil.now())
                .goods(new ArrayList<>())
                .guid(-1)
                .build();

            email.addGood(EmailGood.builder()
                .goodId(935)
                .lockNum(1000)
                .build());

            userEmailStorage.addEmail(email);

            member.update();
            member.save();

            Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(member);

            if (gameUserOptional.isPresent()) {

                LoggedGameUser loggedGameUser = gameUserOptional.get();
                ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();

                loggedGameUser.getSmartServer().send(response);

            }

        }

        for (LoggedGameUser online : LoginService.getInstance().getGameUsers()) {
            if (online.getUpdatedUser().getConsortiaId() == corp.getCorpId()) {
                ChatService.getInstance().broadcastMessage("Congrats! Your corp has won the base defender game!");
            }
        }

        ChatService.getInstance().broadcastMessage("The planet [Name: " + user.getUsername() + ", ID: " + user.getGuid() + ", Corp: " + corp.getName() + "] is no longer the base defender!");

    }

    @Override
    public long getInterval() {

        return 5000L;
    }

    public static DefendJob getInstance() {

        if (instance == null) {
            instance = new DefendJob();
        }
        return instance;
    }

}
