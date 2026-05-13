package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.PacketRouter;
import com.go2super.packet.science.RequestGainLotteryPacket;
import com.go2super.packet.science.ResponseGainLotteryPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.LotteryData;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import lombok.SneakyThrows;

import java.util.*;

public class LotteryListener implements PacketListener {

    public static int LOTTERY_PRICE = 5;

    private static final Map<Integer, Long> lotteryCooldownMap = new HashMap<>();

    @SneakyThrows
    @PacketProcessor
    public void onLottery(RequestGainLotteryPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        if (lotteryCooldownMap.containsKey(packet.getGuid())) {

            long time = lotteryCooldownMap.get(packet.getGuid());
            long cooldown = time - System.currentTimeMillis();

        }

        switch (packet.getType()) {

            case 0:

                if (!(user.getSpins() > 0)) {
                    return;
                }

                user.getResources().removeSpin();
                user.getResources().setLastSpin(DateUtil.now());
                break;

            case 1:

                long mp = user.getResources().getMallPoints();

                if (mp < LOTTERY_PRICE) {
                    return;
                }

                user.getResources().setMallPoints(mp - LOTTERY_PRICE);
                break;

            case 2:

                long vouchers = user.getResources().getVouchers();

                if (vouchers < LOTTERY_PRICE) {
                    return;
                }

                user.getResources().setVouchers(vouchers - LOTTERY_PRICE);
                break;

            default:

                return;

        }

        LotteryData pickOne = ResourceManager.getLottery().pickOne();

        int coins = 0;
        int money = 0;
        int lockFlag = 1;

        int propsId = 0;
        int num = 0;
        int lotteryType = 0;

        switch (pickOne.getReward().getType()) {

            case "prop":

                lotteryType = 5;
                propsId = pickOne.getReward().getId();
                num = pickOne.getReward().getAmount();
                lockFlag = 1;

                if (!user.getInventory().addProp(propsId, num, 0, true)) {
                    return;
                }

                break;

            case "propPool":

                lotteryType = 5;
                propsId = pickOne.getReward().pickOne();
                num = pickOne.getReward().getAmount();
                lockFlag = pickOne.getReward().isLock() ? 1 : 0;

                if (!user.getInventory().addProp(propsId, num, 0, pickOne.getReward().isLock())) {
                    return;
                }

                break;

            case "voucher":

                lotteryType = 1;
                num = pickOne.getReward().getAmount();
                coins = num;
                user.getResources().addVouchers(num);

                break;

            case "gold":

                lotteryType = 2;
                num = pickOne.getReward().getAmount();
                money = num;
                user.getResources().addGold(num);

                break;


        }

        lotteryCooldownMap.put(packet.getGuid(), DateUtil.now().getTime() + 1900L);

        user.save();

        ResponseGainLotteryPacket response = new ResponseGainLotteryPacket();

        response.getName().value(user.getUsername());

        response.setCoins(coins);
        response.setLockFlag(lockFlag);
        response.setPropsId(propsId);
        response.setNum(num);
        response.setMoney(money);
        response.setMetal(0);
        response.setGas(0);

        response.setBroFlag(pickOne.isBroadcast() ? 1 : 0);
        response.setKind(packet.getType());
        response.setLotteryType(lotteryType);
        response.setLotteryId(pickOne.getPosition());
        response.setGuid(packet.getGuid());
        response.setUserId(user.getPlanet().getUserId());

        packet.reply(response);

        if (pickOne.isBroadcast()) {
            PacketRouter.getInstance().broadcast(response, user);
        }

    }

}
