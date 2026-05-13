package com.go2super.service.jobs.other;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.PrizeWeight;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.service.ChatService;
import com.go2super.service.LoginService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;
import java.util.*;

public class ChatGameJob implements OfflineJob {

    @Getter
    @Setter
    private String current;
    private static ChatGameJob instance;

    private static final Map<Character, String> font;
    private static final String comm = "abcdefghijklmnopqrstuvwxyz";

    private static final List<PrizeWeight> prizes;
    private long lastExecution = 0L;

    static {

        prizes = new ArrayList<>();
        font = new HashMap<>();

        font.put('a', "\uD835\uDC1A");
        font.put('b', "\uD835\uDC1B");
        font.put('c', "\uD835\uDC1C");
        font.put('d', "\uD835\uDC1D");
        font.put('e', "\uD835\uDC1E");
        font.put('f', "\uD835\uDC1F");
        font.put('g', "\uD835\uDC20");
        font.put('h', "\uD835\uDC21");
        font.put('i', "\uD835\uDC22");
        font.put('j', "\uD835\uDC23");
        font.put('k', "\uD835\uDC24");
        font.put('l', "\uD835\uDC25");
        font.put('m', "\uD835\uDC26");
        font.put('n', "\uD835\uDC27");
        font.put('o', "\uD835\uDC28");
        font.put('p', "\uD835\uDC29");
        font.put('q', "\uD835\uDC2A");
        font.put('r', "\uD835\uDC2B");
        font.put('s', "\uD835\uDC2C");
        font.put('t', "\uD835\uDC2D");
        font.put('u', "\uD835\uDC2E");
        font.put('v', "\uD835\uDC2F");
        font.put('w', "\uD835\uDC30");
        font.put('x', "\uD835\uDC31");
        font.put('y', "\uD835\uDC32");
        font.put('z', "\uD835\uDC33");
        font.put(' ', " ");

        prizes.add(PrizeWeight.of(0, 0, 700, 80000, 0, 0, 0));
        prizes.add(PrizeWeight.of(0, 0, 700, 0, 70000, 0, 0));
        prizes.add(PrizeWeight.of(0, 0, 700, 0, 0, 80000, 0));

        prizes.add(PrizeWeight.of(0, 0, 600, 100000, 0, 0, 0));
        prizes.add(PrizeWeight.of(0, 0, 600, 0, 90000, 0, 0));
        prizes.add(PrizeWeight.of(0, 0, 600, 0, 0, 100000, 0));

        prizes.add(PrizeWeight.of(0, 0, 350, 0, 0, 0, 40));
        prizes.add(PrizeWeight.of(0, 0, 200, 0, 0, 0, 50));
        prizes.add(PrizeWeight.of(0, 0, 150, 0, 0, 0, 60));

        prizes.add(PrizeWeight.of(900, 3, 100, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(1119, 45, 100, 0, 0, 0, 0));

        prizes.add(PrizeWeight.of(909, 5, 95, 0, 0, 0, 0));

        prizes.add(PrizeWeight.of(913, 1, 90, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(916, 1, 90, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(919, 1, 90, 0, 0, 0, 0));

        prizes.add(PrizeWeight.of(905, 5, 80, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(906, 5, 80, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(907, 5, 80, 0, 0, 0, 0));

        prizes.add(PrizeWeight.of(939, 1, 70, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(940, 1, 70, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(941, 1, 70, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(942, 1, 70, 0, 0, 0, 0));
        prizes.add(PrizeWeight.of(979, 1, 70, 0, 0, 0, 0));

        prizes.add(PrizeWeight.of(0, 0, 30, 0, 0, 0, 300));
        prizes.add(PrizeWeight.of(0, 0, 20, 0, 0, 0, 500));

    }

    public ChatGameJob() {

        instance = this;

    }

    @Override
    public void setup() {

    }

    @Override
    public void run() {

        if (DateUtil.millis() - lastExecution < getInterval()) {

            if (DateUtil.millis() - lastExecution >= (getInterval() / 2) && current != null) {

                lastExecution -= (((double) getInterval()) / 1.8);
                current = null;

                ChatService.getInstance().broadcastMessage("No one wrote the word on time! Luck for the next!");
                return;

            }

            return;

        }

        lastExecution = DateUtil.millis();

        if (current != null) {

            current = null;
            ChatService.getInstance().broadcastMessage("No one wrote the word on time! Luck for the next!");
            return;

        }

        List<String> words = ResourceManager.getCompactedWords();
        Collections.shuffle(words);

        String selected = words.get(0);

        try {

            current = selected;
            ChatService.getInstance().broadcastMessage("The word is: " + convert(selected));

        } catch (Exception e) {

            current = null;
            e.printStackTrace();

        }

    }

    public String convert(String input) {

        String result = "";
        for (int i = 0; i < input.length(); i++) {
            result += font.get(input.charAt(i));
        }
        return result;
    }

    public void congratulate(User user) {

        PrizeWeight prize = pickOne();

        ResponseUsePropsPacket packet = new ResponseUsePropsPacket();

        packet.setPropsId(-1);
        packet.setNumber(0);
        packet.setLockFlag((byte) 0);

        if (prize.getPropId() != 0 && prize.getAmount() != 0) {

            UserInventory userInventory = user.getInventory();
            userInventory.addProp(prize.getPropId(), prize.getAmount(), 0, true);

            int[] propIds = new int[10];
            int[] propNums = new int[10];

            propIds[0] = prize.getPropId();
            propNums[0] = prize.getAmount();

            packet.setAwardPropsId(new IntegerArray(propIds));
            packet.setAwardPropsNum(new IntegerArray(propNums));
            packet.setAwardPropsLen(1);

            packet.setAwardFlag((byte) 1);
            packet.setAwardLockFlag((byte) 1);

            packet.setAwardCoins(0);
            packet.setAwardActiveCredit(0);
            packet.setAwardMetal(0);
            packet.setAwardGas(0);
            packet.setAwardMoney(0);

        } else if (prize.getGold() != 0 || prize.getMetal() != 0 || prize.getHe3() != 0 || prize.getVouchers() != 0) {

            UserResources userResources = user.getResources();

            userResources.addGold(prize.getGold());
            userResources.addMetal(prize.getMetal());
            userResources.addHe3(prize.getHe3());
            userResources.addVouchers(prize.getVouchers());

            packet.setAwardFlag((byte) 1);
            packet.setAwardLockFlag((byte) 0);

            packet.setAwardGas(prize.getHe3());
            packet.setAwardMetal(prize.getMetal());
            packet.setAwardMoney(prize.getGold());
            packet.setAwardCoins(prize.getVouchers());

        }

        user.update();
        user.save();

        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(user);
        if (gameUserOptional.isPresent()) {
            gameUserOptional.get().getSmartServer().send(packet);
        }

    }

    public PrizeWeight pickOne() {

        float weightSum = 0F;
        for (int i = 0; i < prizes.size(); i++) {
            weightSum += prizes.get(i).getWeight();
        }

        float random = new SecureRandom().nextFloat() * weightSum;

        float lowerRangeLimit = 0;
        float upperRangeLimit;

        for (int i = 0; i < prizes.size(); i++) {

            upperRangeLimit = lowerRangeLimit + prizes.get(i).getWeight();

            if (random < upperRangeLimit) {
                return prizes.get(i);
            }

            lowerRangeLimit = upperRangeLimit;

        }

        Collections.shuffle(prizes);
        return prizes.get(0);

    }

    @Override
    public long getInterval() {

        return 1800000L;
    }

    public static ChatGameJob getInstance() {

        if (instance == null) {
            instance = new ChatGameJob();
        }
        return instance;
    }

}
