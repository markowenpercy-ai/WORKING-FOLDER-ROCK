package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BionicChip;
import com.go2super.database.entity.sub.UserChips;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.obj.game.Prop;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.mall.RequestBuyGoodsPacket;
import com.go2super.packet.mall.ResponseBuyGoodsPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropMallData;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

import java.util.*;

public class MallListener implements PacketListener {

    @PacketProcessor
    public void onBuyGoods(RequestBuyGoodsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        if (packet.getNum() <= 0 || packet.getNum() > 9999) {
            return;
        }

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        List<PropData> props = ResourceManager.getProps().getInSell();

        Optional<PropData> optionalGood = props.stream().filter(cache -> cache.getId() == packet.getPropsId()).findAny();
        if (optionalGood.isEmpty()) {
            return;
        }

        UserChips chips = user.getChips();
        UserResources resources = user.getResources();

        PropData good = optionalGood.get();
        List<PropMallData> mallData = Arrays.asList(good.getMall());

        Optional<PropMallData> optionalMall = mallData.stream().filter(cache -> cache.currencyCode() == packet.getCurrency()).findFirst();
        if (!optionalMall.isPresent()) {
            return;
        }

        PropMallData mall = optionalMall.get();

        int quantity = 0;
        int price = packet.getNum() * mall.getValue();

        if (good.getId() == 921) {
            quantity = packet.getNum() * mall.getAmount() * 10;
        } else {
            quantity = packet.getNum() * mall.getAmount();
        }

        if (quantity > 9999) {
            return;
        }

        boolean canBuy = false;
        int lockFlag = mall.isBound() ? 1 : 0;

        if (good.getType().equals("chip")) {

            int slots = chips.getSlots();
            if (packet.getNum() > 30) {
                return;
            }
            if ((chips.getChips().size() + packet.getNum()) > slots) {
                return;
            }

            canBuy = true;

        } else {

            Prop prop = user.getInventory().getProp(good.getId(), lockFlag);

            if (prop != null && ((mall.isBound() ? prop.getPropLockNum() : prop.getPropNum()) + quantity) <= 9999) {
                canBuy = true;
            } else if (prop == null && user.getInventory().countStacks(0) + 1 <= user.getInventory().getMaximumStacks()) {
                canBuy = true;
            }

        }

        if (!canBuy) {
            return;
        }

        switch (mall.currencyCode()) {

            case 0: // MP

                if (resources.getMallPoints() >= price) {
                    resources.setMallPoints(user.getResources().getMallPoints() - price);
                } else {
                    return;
                }

                break;

            case 1: // VOUCHERS

                if (resources.getVouchers() >= price) {
                    resources.setVouchers(user.getResources().getVouchers() - price);
                } else {
                    return;
                }

                break;

            case 2: // BADGE

                if (resources.getBadge() >= price) {
                    resources.setBadge(user.getResources().getBadge() - price);
                } else {
                    return;
                }

                break;

            case 3: // HONOR

                if (resources.getHonor() >= price) {
                    resources.setHonor(user.getResources().getHonor() - price);
                } else {
                    return;
                }

                break;

            case 4: // CHAMPS

                if (resources.getChampionPoints() >= price) {
                    resources.setChampionPoints(user.getResources().getChampionPoints() - price);
                } else {
                    return;
                }

                break;

            default:
                return;

        }

        if (good.getType().equals("chip")) {

            for (int i = 0; i < packet.getNum(); i++) {
                chips.getChips().add(BionicChip.builder()
                    .chipId(good.getId())
                    .chipExperience(0)
                    .bound(false)
                    .build());
            }

        } else if (!user.getInventory().addProp(good.getId(), quantity, 0, mall.isBound())) {
            return;
        }

        ResponseBuyGoodsPacket response = UserService.getInstance().getBuyGoodsPacket(good, mall, lockFlag, quantity, price);

        user.update();
        user.save();

        packet.reply(response);

    }

}
