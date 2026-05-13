package com.go2super.listener;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BionicChip;
import com.go2super.database.entity.sub.UserChips;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.CmosInfo;
import com.go2super.obj.game.Prop;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.PacketRouter;
import com.go2super.packet.chiplottery.*;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ChipData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.meta.ChipMeta;
import com.go2super.resources.data.meta.ChipMoneyMeta;
import com.go2super.resources.data.meta.ChipRewardMeta;
import com.go2super.resources.data.props.PropChipData;
import com.go2super.service.CommanderService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.RandomUtil;

import java.util.*;

public class BionicChipListener implements PacketListener {

    @PacketProcessor
    public void onCommanderInsertCmos(RequestCommanderInsertCmosPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Commander commander = user.getCommander(packet.getCommanderId());
        if (commander == null) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByCommanderId(commander.getCommanderId());
        if (fleet != null && (fleet.isMatch() || fleet.isInMatch() || fleet.isInTransmission())) {
            return;
        }

        if (packet.getCmosId() < 0) {
            return;
        }
        if (packet.getCmosType() != 0 && packet.getCmosType() != 1) {
            return;
        }
        if (!(packet.getHoleId() >= 0 && packet.getHoleId() <= 4)) {
            return;
        }


        UserChips userChips = user.getChips();

        if (packet.getCmosType() == 0) { // Insert

            if (commander.getChips().size() >= 5) {
                return;
            }
            if (packet.getCmosId() >= userChips.getChips().size()) {
                return;
            }

            int nextChipHole = commander.getNextChipHole();
            BionicChip selected = userChips.getChips().get(packet.getCmosId());

            if (selected == null) {
                return;
            }
            if (nextChipHole != packet.getHoleId()) {
                return;
            }

            PropData propData = selected.getPropData();
            if (propData == null) {
                return;
            }

            PropChipData chipData = propData.getChipData();
            if (chipData == null) {
                return;
            }

            ChipMeta selectedEffect = chipData.getEffect();
            if (selectedEffect == null) {
                BotLogger.error("ChipMeta not found for chipId: " + chipData);
                return;
            }

            for (BionicChip otherChip : commander.getChips()) {

                PropChipData otherData = otherChip.getChipData();
                if (otherData == null) {
                    continue;
                }

                ChipMeta otherEffect = otherData.getEffect();
                if (otherEffect == null) {
                    continue;
                }

                if (otherEffect.eqauls(selectedEffect)) {
                    return;
                }

            }

            userChips.getChips().remove(selected);

            selected.setHoleId(nextChipHole);
            commander.getChips().add(selected);

        } else { // Remove

            List<BionicChip> commanderChips = commander.getChips();
            if (commanderChips.isEmpty()) {
                return;
            }

            BionicChip selected = commanderChips.stream().filter(chip -> chip.getHoleId() == packet.getHoleId()).findFirst().orElse(null);
            if (selected == null) {
                return;
            }

            commanderChips.remove(selected);
            selected.setHoleId(-1);

            userChips.getChips().add(selected);

        }

        user.save();
        commander.save();

        ResponseCommanderInsertCmosPacket response = CommanderService.getInstance().getCommanderInsertCmosPacket(packet.getCmosType(), packet.getCommanderId(), packet.getHoleId(), packet.getCmosId());
        packet.reply(response);

    }

    @PacketProcessor
    public void onRequestCmosLottery(RequestCmosLotteryInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserChips userChips = user.getChips();
        if (userChips == null) {
            user.setChips(UserChips.builder()
                .chips(new ArrayList<>())
                .build());
            userChips = user.getChips();
        }

        if (userChips.getSlots() < 15) {

            userChips.setSlots(15);

            user.update();
            user.save();

        }

        ResponseCmosLotteryInfoPacket response = getCmosLotteryInfo(user);
        packet.reply(response);

    }

    @PacketProcessor
    public void onRequestGainCmosLottery(RequestGainCmostLotteryPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        if (packet.getPhaseId() < 0 || packet.getPhaseId() > 4) {
            return;
        }

        UserChips chips = user.getChips();
        if (packet.getPhaseId() != 0 && chips.getPhases()[packet.getPhaseId()] != 1) {
            return;
        }

        ChipData level = ResourceManager.getChips().getLevel(packet.getPhaseId() + 1);
        if (level == null) {
            return;
        }

        int slots = chips.getSlots();
        if (chips.getChips().size() + 1 > slots) {
            return;
        }

        UserResources resources = user.getResources();
        ChipMoneyMeta price = level.getPrice();

        boolean corsair = packet.getType() == 0;

        if (corsair && resources.getCorsairs() < price.getCorsair()) {
            return;
        }
        if (!corsair && resources.getMallPoints() < price.getMp()) {
            return;
        }

        int cost = corsair ? price.getCorsair() : price.getMp();

        if (corsair) {
            resources.setCorsairs(resources.getCorsairs() - cost);
        } else {
            resources.setMallPoints(resources.getMallPoints() - cost);
        }

        ChipRewardMeta reward = level.pickOne(corsair);
        if (reward == null) {
            return;
        }

        PropChipData chipData = reward.getChipData();
        boolean next = level.getNext() != -1 && RandomUtil.getRandomInt(0, 100) <= level.getNext();

        int[] phases = chips.getPhases();
        if (next) {
            phases[packet.getPhaseId() + 1] = 1;
        } else {
            phases[packet.getPhaseId()] = 0;
        }
        chips.setPhases(phases);

        chips.getChips().add(BionicChip.builder()
            .chipId(reward.getPropId())
            .chipExperience(0)
            .bound(chipData.hasToBound())
            .build());

        // user.update();
        user.save();

        byte lotteryPhase = getLotteryPhase(chips);

        ResponseGainCmostLotteryPacket response = new ResponseGainCmostLotteryPacket();

        response.setGuid(user.getGuid());
        response.setLotteryId(0);
        response.setPropsId(reward.getPropId());
        response.setType(packet.getType());
        response.setCredit(cost);

        response.setLotteryPhase(lotteryPhase);
        response.setBroFlag((byte) (reward.isBroadcast() ? 1 : 0));

        response.setName(SmartString.of(user.getUsername(), 32));

        if (reward.isBroadcast()) {
            PacketRouter.getInstance().broadcast(response, user);
        }

        packet.reply(response);

    }

    @PacketProcessor
    public void onRequestSellProps(RequestSellPropsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        // Sell in lottery UI
        if (packet.getType() == 1) {

            UserChips chips = user.getChips();

            if (packet.getId() < 0 || packet.getId() >= chips.getChips().size()) {
                return;
            }
            if (chips.getChips().isEmpty()) {
                return;
            }

            BionicChip selection = chips.getChips().get(packet.getId());
            PropData propData = selection.getPropData();

            UserResources resources = user.getResources();
            resources.setCorsairs(resources.getCorsairs() + propData.getSalvage());

            chips.getChips().remove(packet.getId());
            user.save();

            ResponseCmosLotteryInfoPacket response = getCmosLotteryInfo(user);
            packet.reply(response);
            return;

        }

        // Sell in Galactic Trafficker UI
        Prop prop = user.getInventory().getProp(packet.getId(), 0);

        if (prop == null) {
            return;
        }
        if (!user.getInventory().removeProp(prop, packet.getNum(), packet.getLockFlag() == 1)) {
            return;
        }

        UserResources resources = user.getResources();
        PropData propData = prop.getData();

        if (propData == null) {
            return;
        }

        resources.addCorsairs(propData.getSalvage() * packet.getNum());
        user.save();

        ResponseCmosLotteryInfoPacket response = getCmosLotteryInfo(user);
        packet.reply(response);

    }

    @PacketProcessor
    public void onRequestUnionCmos(RequestUnionCmosPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        UserChips chips = user.getChips();

        if (packet.getCmosId1() == packet.getCmosId2()) {
            return;
        }
        if (packet.getCmosId1() < 0 || packet.getCmosId1() >= chips.getChips().size()) {
            return;
        }
        if (packet.getCmosId2() < 0 || packet.getCmosId2() >= chips.getChips().size()) {
            return;
        }
        if (chips.getChips().isEmpty()) {
            return;
        }

        BionicChip base = chips.getChips().get(packet.getCmosId1());
        BionicChip use = chips.getChips().get(packet.getCmosId2());

        base.addExperience(use);

        chips.getChips().remove(use);
        user.save();

        ResponseCmosLotteryInfoPacket response = getCmosLotteryInfo(user);
        packet.reply(response);

    }

    @PacketProcessor
    public void onRequestOpenCmos(RequestOpenCmosPack packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserChips chips = user.getChips();
        UserResources resources = user.getResources();

        if (chips.getSlots() + 1 > 30) {
            return;
        }

        int price = (chips.getSlots() - 15) + 1;
        if (resources.getMallPoints() < price) {
            return;
        }

        chips.setSlots(chips.getSlots() + 1);
        resources.setMallPoints(resources.getMallPoints() - price);

        user.save();

        ResponseCmosLotteryInfoPacket response = getCmosLotteryInfo(user);
        packet.reply(response);

    }

    public ResponseCmosLotteryInfoPacket getCmosLotteryInfo(User user) {

        UserChips chips = user.getChips();
        ResponseCmosLotteryInfoPacket response = new ResponseCmosLotteryInfoPacket();

        byte lotteryPhase = getLotteryPhase(chips);

        response.setPirateMoney(Long.valueOf(user.getResources().getCorsairs()).intValue());
        response.setLotteryPhase(lotteryPhase);
        response.setCmosPackCount(UnsignedChar.of(chips.getSlots()));

        for (BionicChip bionicChip : chips.getChips()) {
            response.getData().add(bionicChip.getCmosInfo());
        }

        response.setDataLen((short) response.getData().size());

        while (response.getData().size() < 30) {
            response.getData().add(new CmosInfo(-1, (short) -1, (short) -1));
        }

        return response;

    }

    private byte getLotteryPhase(UserChips chips) {

        byte lotteryPhase = 0b10000;

        if (chips.getPhases() == null) {
            chips.setPhases(new int[]{1, 0, 0, 0, 0});
        }

        if (chips.getPhases()[1] == 1) {
            lotteryPhase |= (1 << 0);
        }
        if (chips.getPhases()[2] == 1) {
            lotteryPhase |= (1 << 1);
        }
        if (chips.getPhases()[3] == 1) {
            lotteryPhase |= (1 << 2);
        }
        if (chips.getPhases()[4] == 1) {
            lotteryPhase |= (1 << 3);
        }

        return lotteryPhase;

    }

}
