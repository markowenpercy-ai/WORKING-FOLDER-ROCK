package com.go2super.listener;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserRewards;
import com.go2super.database.entity.type.UserRank;
import com.go2super.packet.Packet;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.payment.ResponsePaymentSucceedPacket;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.packet.reward.RequestGetOnlineAwardPacket;
import com.go2super.packet.reward.ResponseGetOnlineAwardPacket;
import com.go2super.packet.reward.ResponseOnlineAwardPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.RewardData;
import com.go2super.resources.json.RewardsJson;
import com.go2super.service.LoginService;
import com.go2super.service.ResourcesService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class OnlineListener implements PacketListener {

    @PacketProcessor
    public void onOnlineAward(RequestGetOnlineAwardPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }
        Account account = user.getAccount();
        UserRewards userRewards = user.getRewards();
        if (userRewards == null || userRewards.getLevel() == -1) {
            return;
        }

        boolean isMain = account.getMainPlanetId() != null && user.getUserId() == account.getMainPlanetId();
        RewardsJson rewardsJson = ResourceManager.getRewards(isMain);
        boolean reset = user.getLastDayUpdate() != null && !DateUtil.currentDay(user.getLastDayUpdate());

        if (userRewards.getUntil() == null) {
            reset = true;
        }

        if (reset) {

            userRewards.setLevel(0);
            userRewards.setUntil(DateUtil.now(rewardsJson.getReward(0).getTime()));

            ResponseOnlineAwardPacket response = new ResponseOnlineAwardPacket();

            response.setSpareTime(DateUtil.remains(userRewards.getUntil()).intValue());
            response.setPropsNum(0);
            response.setPropsId(-1);

            packet.reply(response);
            return;

        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(userRewards.getUntil());

        if (calendar.getTime().before(new Date())) {

            RewardData reward = rewardsJson.getReward(userRewards.getLevel());

            int next = rewardsJson.getRewards().size() <= userRewards.getLevel() + 1 ? -1 : userRewards.getLevel() + 1;
            int time = next == -1 ? -1 : rewardsJson.getReward(next).getTime();

            if (next == -1) {

                userRewards.setLevel(next);
                userRewards.setUntil(null);

                List<Packet> responses = giveReward(reward, user);
                if (responses == null) {
                    return;
                }

                user.update();
                user.save();

                packet.reply(responses);
                return;

            }

            List<Packet> responses = giveReward(reward, user);
            if (responses == null) {
                return;
            }

            Date until = DateUtil.now(time);

            userRewards.setLevel(next);
            userRewards.setUntil(until);

            user.update();
            user.save();

            ResponseOnlineAwardPacket response = new ResponseOnlineAwardPacket();

            response.setSpareTime(time);
            response.setPropsNum(0);
            response.setPropsId(-1);

            packet.reply(responses);
            packet.reply(response);

        }

    }

    private List<Packet> giveReward(RewardData rewardData, User user) {

        List<Packet> responses = new ArrayList<>();

        int propsId = -1;
        int num = 0;

        switch (rewardData.getReward().getType()) {

            case "prop":

                propsId = rewardData.getReward().getId();
                num = rewardData.getReward().getAmount();

                if (!user.getInventory().addProp(propsId, num, 0, true)) {
                    return null;
                }

                break;

            case "propPool":

                propsId = rewardData.getReward().pickOne();
                num = rewardData.getReward().getAmount();

                if (!user.getInventory().addProp(propsId, num, 0, true)) {
                    return null;
                }

                break;

            case "propPoolUnlock":

                int propPoolUnlockId = rewardData.getReward().pickOne();
                int propPoolUnlockNum = rewardData.getReward().getAmount();

                if (!user.getInventory().addProp(propPoolUnlockId, propPoolUnlockNum, 0, false)) {
                    return null;
                }

                Packet unlockedPoolProp = ResourcesService.getInstance().smartUseProps(-1, 0, 0, 0, 0, 0, 1, 0, List.of(Pair.of(propPoolUnlockId, propPoolUnlockNum)));
                responses.add(unlockedPoolProp);
                break;

            case "voucher":

                num = rewardData.getReward().getAmount();
                user.getResources().addVouchers(num);

                ResponseUsePropsPacket voucherPacket = new ResponseUsePropsPacket();

                voucherPacket.setPropsId(-1);
                voucherPacket.setNumber(0);
                voucherPacket.setLockFlag((byte) 0);

                voucherPacket.setAwardFlag((byte) 1);
                voucherPacket.setAwardLockFlag((byte) 0);

                voucherPacket.setAwardGas(0);
                voucherPacket.setAwardMetal(0);
                voucherPacket.setAwardMoney(0);
                voucherPacket.setAwardCoins(num);

                responses.add(voucherPacket);
                break;

            case "gold":

                num = rewardData.getReward().getAmount();
                user.getResources().addGold(num);

                ResponseUsePropsPacket goldPacket = new ResponseUsePropsPacket();

                goldPacket.setPropsId(-1);
                goldPacket.setNumber(0);
                goldPacket.setLockFlag((byte) 0);

                goldPacket.setAwardFlag((byte) 1);
                goldPacket.setAwardLockFlag((byte) 0);

                goldPacket.setAwardGas(0);
                goldPacket.setAwardMetal(0);
                goldPacket.setAwardMoney(num);
                goldPacket.setAwardCoins(0);

                responses.add(goldPacket);
                break;

            case "he3":

                num = rewardData.getReward().getAmount();
                user.getResources().addHe3(num);

                ResponseUsePropsPacket he3Packet = new ResponseUsePropsPacket();

                he3Packet.setPropsId(-1);
                he3Packet.setNumber(0);
                he3Packet.setLockFlag((byte) 0);

                he3Packet.setAwardFlag((byte) 1);
                he3Packet.setAwardLockFlag((byte) 0);

                he3Packet.setAwardGas(num);
                he3Packet.setAwardMetal(0);
                he3Packet.setAwardMoney(0);
                he3Packet.setAwardCoins(0);

                responses.add(he3Packet);
                break;

            case "metal":

                num = rewardData.getReward().getAmount();
                user.getResources().addMetal(num);

                ResponseUsePropsPacket metalPacket = new ResponseUsePropsPacket();

                metalPacket.setPropsId(-1);
                metalPacket.setNumber(0);
                metalPacket.setLockFlag((byte) 0);

                metalPacket.setAwardFlag((byte) 1);
                metalPacket.setAwardLockFlag((byte) 0);

                metalPacket.setAwardGas(0);
                metalPacket.setAwardMetal(num);
                metalPacket.setAwardMoney(0);
                metalPacket.setAwardCoins(0);

                responses.add(metalPacket);
                break;

            case "mallPoint":

                num = rewardData.getReward().getAmount();
                user.getResources().addMallPoints(num);

                ResponsePaymentSucceedPacket responsePaymentSucceedPacket = new ResponsePaymentSucceedPacket();
                responsePaymentSucceedPacket.setCredit(num);

                responses.add(responsePaymentSucceedPacket);
                break;


        }

        if (propsId != -1) {
            responses.add(preparePacket(propsId, num));
        }
        return responses;

    }

    private ResponseGetOnlineAwardPacket preparePacket(int propId, int propNum) {

        ResponseGetOnlineAwardPacket onlineAwardPacket = new ResponseGetOnlineAwardPacket();

        onlineAwardPacket.setErrorCode(0);
        onlineAwardPacket.setPropsId(propId);
        onlineAwardPacket.setPropsNum(propNum);

        return onlineAwardPacket;

    }

}
