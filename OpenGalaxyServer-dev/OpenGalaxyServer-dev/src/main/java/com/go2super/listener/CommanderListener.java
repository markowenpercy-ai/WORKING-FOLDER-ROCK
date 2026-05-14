package com.go2super.listener;

import com.go2super.database.entity.*;
import com.go2super.database.entity.sub.UserBuilding;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.logger.Lookup;
import com.go2super.logger.data.DataCompilation;
import com.go2super.logger.data.UserActionLog;
import com.go2super.obj.game.Prop;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.type.AuditType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.Gem;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.PacketRouter;
import com.go2super.packet.commander.*;
import com.go2super.packet.gems.*;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.LevelData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.props.PropCommanderData;
import com.go2super.resources.data.props.PropGemData;
import com.go2super.resources.data.props.PropScrollData;
import com.go2super.service.*;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.MathUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.*;

public class CommanderListener implements PacketListener {

    @PacketProcessor
    public void onEditFleet(RequestCommanderEditShipTeamPacket packet) {
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getShipTeamId() < 0) {
            return;
        }

        Fleet fleet = PacketService.getInstance()
            .getFleetCache()
            .findByShipTeamId(packet.getShipTeamId());

        if (fleet == null) {
            return;
        }
        if (fleet.getShipTeamId() != packet.getShipTeamId() || fleet.getGuid() != packet.getGuid()) {
            return;
        }
        if (fleet.isMatch() || fleet.isInMatch() || fleet.isInTransmission()) {
            return;
        }

        Planet planet = GalaxyService.getInstance().getPlanet(new GalaxyTile(fleet.getGalaxyId()));
        if (planet != null && planet.isInWar()) {
            return;
        }

        ShipTeamBody oldTeamBody = fleet.getFleetBody();
        ShipTeamBody newTeamBody = packet.getTeamBody();

        UserShips ships = user.getShips();

        // Sanity checks:
        //   Fleets have 9 stacks
        //   Fleets have at least 1 ship
        //   Stacks cannot have more than 3000 ships
        //   Fleets have up to 1 flagship stack

        if (newTeamBody.getCells().size() != 9) {
            return;
        }

        int total = 0;
        boolean hasFlagship = false;

        for (ShipTeamNum newNum : newTeamBody.getCells()) {
            if (newNum.getShipModelId() <= -1 || newNum.getNum() <= 0) {
                // Ignore empty stacks
                continue;
            }

            total += newNum.getNum();

            if (newNum.getNum() > 3000) {
                // Stacks cannot be larger than 3000
                return;
            }

            ShipBodyData shipData = ResourceManager
                .getShipBodies()
                .findByBodyId(newNum.getBodyId());

            if (shipData.getBodyType().equals("flagship")) {
                if (hasFlagship) {
                    // The fleet has more than 1 stack of flagships
                    return;
                } else {
                    // This is the first stack of flagships seen
                    hasFlagship = true;
                }
            }
        }

        if (total == 0) {
            // No ships in new fleet, abort
            return;
        }

        // Passed preliminary checks, time to start making changes

        // Return all ships from the old fleet to inventory
        for (ShipTeamNum oldNum : oldTeamBody.getCells()) {
            if (oldNum.getNum() != 0 && oldNum.getShipModelId() > -1) {
                ships.addShip(oldNum.getShipModelId(), oldNum.getNum());
            }
        }

        int error = -1;

        for (int idx = 0; idx < 9; ++idx) {
            ShipTeamNum newNum = newTeamBody.getCells().get(idx);

            if (newNum.getShipModelId() <= -1 || newNum.getNum() <= 0) {
                // Ignore empty stacks
                continue;
            }

            if (!ships.removeShip(newNum.getShipModelId(), newNum.getNum())) {
                // Fleets can only use ships that exist in inventory
                error = idx;
                break;
            }

            ShipModel model = PacketService.getShipModel(newNum.getShipModelId());

            if (model == null) {
                // Fleets can only use designs that exist
                // This might be redundant due to the ships.removeShip check above
                error = idx;
                break;
            }
        }

        if (error > -1) {
            // An error was found above, undo the changes to the fleet and abort
            for (int idx = 0; idx < error; ++idx) {
                // Re-add any ships to inventory that were removed earlier
                ShipTeamNum newNum = newTeamBody.getCells().get(idx);
                ships.addShip(newNum.getShipModelId(), newNum.getNum());
            }

            // Take the old ships back out of inventory
            for (ShipTeamNum oldNum : oldTeamBody.getCells()) {
                if (oldNum.getNum() != 0 && oldNum.getShipModelId() > -1) {
                    ships.removeShip(oldNum.getShipModelId(), oldNum.getNum());
                }
            }

            return;
        }

        fleet.setFleetBody(newTeamBody);
        fleet.setBodyId(fleet.bodyId());

        if (packet.getRange() < 0 || packet.getRange() > 1) {
            packet.setRange((byte) 0);
        }

        if (packet.getPreference() < 0 || packet.getPreference() > 6) {
            packet.setPreference((byte) 0);
        }

        fleet.setRangeType(packet.getRange());
        fleet.setPreferenceType(packet.getPreference());

        user.save();
        fleet.save();

        ResponseCommanderEditShipTeamPacket response = new ResponseCommanderEditShipTeamPacket();

        response.setErrorCode(0);
        response.setKind(packet.getEditType());

        packet.reply(response);
    }

    @PacketProcessor
    public void onCommanderCreate(RequestCreateCommanderPacket packet) {

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        if (user.getStats().getNextInvitation() != null && DateUtil.remains(user.getStats().getNextInvitation()) > 0) {
            return;
        }

        LevelData levelData = user.getStats().getLevelData();

        if (levelData == null) {
            return;
        }

        List<Commander> commanders = CommanderService.getInstance().getCommanderCache().findByUserId(user.getUserId());

        if (commanders.size() + 1 > levelData.getCommandernum()) {
            return;
        }

        UserBuilding building = user.getBuildings().getBuilding("build:commander");

        if (building == null) {
            return;
        }

        int spareTime = (int) building.getLevelData().getEffect("recruitCooldown").getValue();

        user.getStats().setNextInvitation(DateUtil.now(spareTime));

        Commander commander = CommanderService.getInstance().common(user.getUserId());
        commander.save();

        user.getMetrics().add("action:recruit", 1);
        user.update();
        user.save();

        ResponseCreateCommanderPacket response = CommanderService.getInstance().getCreateCommander(commander);
        packet.reply(response);

    }

    @PacketProcessor
    public void onCommanderInfo(RequestCommanderInfoPacket packet) {

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        CommanderService.getInstance().sendInfoPacket(packet.getCommanderId(), packet.getShowType(), packet.getSmartServer(), user);

    }

    @PacketProcessor
    public void onCommanderInsertStone(RequestCommanderInsertStonePacket packet) throws BadGuidException {

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

        if (packet.getGemType() != 0 && packet.getGemType() != 1) {
            return;
        }
        if (!(packet.getHoleId() >= 0 && packet.getHoleId() <= 11)) {
            return;
        }

        int maxGems = commander.getLevel().getLevelData().getGem();
        if (maxGems < packet.getHoleId() + 1) {
            return;
        }

        List<Integer> gems = commander.getGems();
        final int[] validator = new int[]{1, 2, 3, -1, 1, 2, 3, -1, 1, 2, 3, -1};

        if (gems.size() == 0) {
            for (int i = 0; i < 12; i++) {
                gems.add(-1);
            }
        }

        if (packet.getGemType() == 0) {

            Prop gem = user.getInventory().getProp(packet.getPropsId());
            if (gem == null) {
                return;
            }

            PropData data = gem.getData();
            if (!data.getType().equals("gem")) {
                return;
            }

            PropGemData gemData = data.getGemData();
            if (gemData == null) {
                return;
            }

            int validation = validator[packet.getHoleId()];
            if (validation != -1 && gemData.getColor() != validation) {
                return;
            }
            if (gems.get(packet.getHoleId()) != -1) {
                return;
            }
            if (!user.getInventory().removeProp(data.getId(), 1, 0, packet.getLockFlag() == 1)) {
                return;
            }

            gems.set(packet.getHoleId(), data.getId());

            ResponseCommanderInsertStonePacket response = CommanderService.getInstance().getCommanderInsertStonePacket(packet.getGemType(), packet.getCommanderId(), packet.getHoleId(), packet.getPropsId(), packet.getLockFlag());

            packet.reply(response);
            user.save();
            commander.save();
            return;

        }

        if (!user.getInventory().addProp(gems.get(packet.getHoleId()), 1, 0, true)) {
            return;
        }

        gems.set(packet.getHoleId(), -1);

        ResponseCommanderInsertStonePacket response = CommanderService.getInstance().getCommanderInsertStonePacket(packet.getGemType(), packet.getCommanderId(), packet.getHoleId(), packet.getPropsId(), 1);

        packet.reply(response);
        user.save();
        commander.save();

    }

    private void replyMergeError(RequestUnionCommanderCardPacket packet) {
        packet.reply(new ResponseUnionCommanderCardPacket());
    }

    @PacketProcessor
    public void onUnionCommanderCard(RequestUnionCommanderCardPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        if (packet.getCard3() == -1 && packet.getCard1() != packet.getCard2()) {
            replyMergeError(packet);
            return;
        }

        int mergeCard = packet.getCard1();
        int nextCard = mergeCard + 1;

        // Check if result card would be restricted
        if (RestrictedItemsService.getInstance().isRestricted(nextCard)) {
            replyMergeError(packet);
            return;
        }

        PropData mergeData = CommanderService.getInstance().getCommanderPropData(mergeCard);

        if(mergeData == null){
            replyMergeError(packet);
            return;
        }

        PropData nextData = CommanderService.getInstance().getCommanderPropData(nextCard);

        PropData card2 = CommanderService.getInstance().getCommanderPropData(packet.getCard2());
        if (nextData == null || mergeData.getId() != nextData.getId()) {
            replyMergeError(packet);
            return;
        }

        int stars1 = packet.getCard1() - mergeData.getId();
        int stars2 = packet.getCard2() - card2.getId();

        int resultStars = nextCard - mergeData.getId();
        int success = 100;
        int chipSuccess = 0;
        int corpMergeBonus = 0;

        if (resultStars >= 3) {
            success = Math.max(success - (resultStars - 2) * 10, 0);
        }

        if (packet.getGoods() == 925 || packet.getGoods() == 944) {

            Prop chip = user.getInventory().getProp(packet.getGoods());

            if (user.getInventory().removeOneProp(chip, packet.getGoodsLockFlag() == 1)) {
                chipSuccess += packet.getGoods() == 925 ? 20 : 30;
            }

        }

        Corp corp = CorpService.getInstance().getCorpByUser(user.getGuid());
        if (corp != null) {
            corpMergeBonus += corp.getMergeBonus() * 100.0d;
        }

        success = success + corpMergeBonus + chipSuccess;

        boolean failed = MathUtil.random(0, 100) > success;

        if (packet.getCard3() == -1) {

            Prop prop = user.getInventory().getProp(mergeCard);

            if (prop == null || prop.getPropNum() < 2) {
                replyMergeError(packet);
                return;
            }
            if (!user.getInventory().removeProp(prop, failed ? 1 : 2, false)) {
                replyMergeError(packet);
                return;
            }
            if (!failed && !user.getInventory().addProp(nextCard, 1, 0, false)) {
                // Rollback: return the consumed cards
                user.getInventory().addProp(mergeCard, failed ? 1 : 2, 0, false);
                replyMergeError(packet);
                return;
            }
            if (!failed) {
                user.getMetrics().add("action:combine.cards", 1);
            }

            user.update();
            user.save();

            ResponseUnionCommanderCardPacket response = CommanderService.getInstance().getUnionCommanderCardPacket(!failed ? nextCard : -1, packet.getCard1(), packet.getCard2(), packet.getCard3(), packet.getGoods(), packet.getGoodsLockFlag());
            packet.reply(response);

            if (resultStars > 5 || failed) {

                ResponseUnionCommanderCardBroPacket broadcast = CommanderService.getInstance().getUnionCommanderCardBroPacket(user, mergeData.getCommanderData().getCommander().getId(), resultStars, failed);
                PacketRouter.getInstance().broadcast(broadcast);

            }

            return;

        }

        PropData card3 = CommanderService.getInstance().getCommanderPropData(packet.getCard3());
        if (card2 == null || card3 == null) {
            replyMergeError(packet);
            return;
        }

        int stars3 = packet.getCard3() - card3.getId();
        if (stars1 != stars2 || stars1 != stars3) {
            replyMergeError(packet);
            return;
        }

        if (!card2.getCommanderData().getCommander().getType().equals(mergeData.getCommanderData().getCommander().getType()) ||
            !card3.getCommanderData().getCommander().getType().equals(mergeData.getCommanderData().getCommander().getType())) {
            replyMergeError(packet);
            return;
        }

        // Only consume cards on success — 3-card merge is all-or-nothing
        if (!failed) {
            if (!user.getInventory().removeProp(packet.getCard1(), 1, 0, false) ||
                !user.getInventory().removeProp(packet.getCard2(), 1, 0, false) ||
                !user.getInventory().removeProp(packet.getCard3(), 1, 0, false)) {
                replyMergeError(packet);
                return;
            }
            if (!user.getInventory().addProp(nextCard, 1, 0, false)) {
                // Rollback: return all three cards
                user.getInventory().addProp(packet.getCard1(), 1, 0, false);
                user.getInventory().addProp(packet.getCard2(), 1, 0, false);
                user.getInventory().addProp(packet.getCard3(), 1, 0, false);
                replyMergeError(packet);
                return;
            }
            user.getMetrics().add("action:combine.cards", 1);
        }

        user.update();
        user.save();

        ResponseUnionCommanderCardPacket response = CommanderService.getInstance().getUnionCommanderCardPacket(!failed ? nextCard : -1, packet.getCard1(), packet.getCard2(), packet.getCard3(), packet.getGoods(), packet.getGoodsLockFlag());
        packet.reply(response);

        if (resultStars > 5 || failed) {

            ResponseUnionCommanderCardBroPacket broadcast = CommanderService.getInstance().getUnionCommanderCardBroPacket(user, mergeData.getCommanderData().getCommander().getId(), resultStars, failed);
            PacketRouter.getInstance().broadcast(broadcast);

        }

    }

    @PacketProcessor
    public void onCommanderUnionStone(RequestCommanderUnionStonePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Prop gem = user.getInventory().getProp(packet.getPropsId());
        if (gem == null) {
            return;
        }

        PropData data = gem.getData();
        if (!data.getType().equals("gem")) {
            return;
        }

        PropGemData gemData = data.getGemData();
        if (gemData.getLevel() >= 4) {
            return;
        }

        boolean lock = packet.getLockFlag() != 0;
        if (!user.getInventory().removeProp(gem, 4, lock)) {
            return;
        }

        int result = gem.getPropId() + 1;

        if (gem.getPropId() == 1119) {

            List<PropData> gems = ResourceManager.getProps().getGems();
            List<PropData> raws = gems.stream().filter(g -> g.getGemData().getType() >= 1 && g.getGemData().getType() <= 11 && g.getGemData().getLevel() == 0).collect(Collectors.toList());

            Collections.shuffle(raws);
            if (raws.isEmpty()) {
                return;
            }
            result = raws.get(0).getId();

        }

        if (!user.getInventory().addProp(result, 1, 0, lock)) {
            return;
        }

        user.update();
        user.save();

        boolean bro = gemData.getLevel() >= 3;

        ResponseCommanderUnionStonePacket response = CommanderService.getInstance().getCommanderUnionStonePacket(user, result, lock, bro);
        if (packet.getSmartServer().getSocket().isConnected()) {
            packet.reply(response);
        }

        if (bro) {
            PacketRouter.getInstance().broadcast(response, user);
        }

    }

    @PacketProcessor
    public void onCommanderPropertyStone(RequestCommanderPropertyStonePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        PropData objGemData = ResourceManager.getProps().getGemData(packet.getObjStoneId());

        PropData srcData1 = ResourceManager.getProps().getGemData(packet.getSrcStoneId1());
        PropData srcData2 = ResourceManager.getProps().getGemData(packet.getSrcStoneId2());
        PropData srcData3 = ResourceManager.getProps().getGemData(packet.getSrcStoneId3());

        if (objGemData == null || srcData1 == null || srcData2 == null) {
            return;
        }
        if (!objGemData.getType().equals("gem") || !srcData1.getType().equals("gem") || !srcData2.getType().equals("gem")) {
            return;
        }
        if (srcData3 != null && !srcData3.getType().equals("gem")) {
            return;
        }

        Gem obj = Gem.of(objGemData.getGemData());

        Gem src1 = Gem.of(srcData1.getGemData());
        Gem src2 = Gem.of(srcData2.getGemData());
        Gem src3 = srcData3 == null ? null : Gem.of(srcData3.getGemData());

        boolean locked = packet.getLockFlag() == 1;
        boolean validation = CommanderService.getInstance().validateGemEX(obj, src1, src2, src3);

        if (!validation) {
            return;
        }

        if (!user.getInventory().removeProp(srcData1.getId(), 1, 0, locked)) {
            return;
        }
        if (!user.getInventory().removeProp(srcData2.getId(), 1, 0, locked)) {
            return;
        }
        if (srcData3 != null && !user.getInventory().removeProp(srcData3.getId(), 1, 0, locked)) {
            return;
        }

        if (!user.getInventory().addProp(objGemData.getId(), 1, 0, locked)) {
            return;
        }
        if (user.getResources().getGold() < 10000) {
            return;
        }

        user.getResources().setGold(user.getResources().getGold() - 10000);
        boolean bro = obj.getLevel() >= 3;

        ResponseCommanderPropertyStonePacket response = CommanderService.getInstance().getCommanderPropertyStonePacket(user, packet.getType(), packet.getObjStoneId(), packet.getSrcStoneId1(), packet.getSrcStoneId2(), packet.getSrcStoneId3(), locked, bro);

        packet.reply(response);

        user.update();
        user.save();

        if (bro) {
            PacketRouter.getInstance().broadcast(response, user);
        }

    }

    @PacketProcessor
    public void onCommanderSeal(RequestCommanderChangeCardPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Prop card = user.getInventory().getProp(926);
        if (card == null) {
            return;
        }

        Commander commander = user.getCommander(packet.getCommanderId());
        if (commander == null || !commander.getChips().isEmpty() || commander.hasGems() || commander.isCommon()) {
            return;
        }

        if(commander.getState() != 0){
            //commander state is not normal
            return;
        }
        DataCompilation beforeData = new DataCompilation();
        beforeData.add("inventory", Lookup.getUserInventories(user));
        beforeData.add("commander", Lookup.getCommander(commander));

        Pair<Boolean, Boolean> removedProp = user.getInventory().removeProp(card, 1);
        if (!removedProp.getKey()) {
            return;
        }

        ResponseCommanderChangeCardPacket response = CommanderService.getInstance().getSealCommanderPacket(user, card, commander);
        user.save();

        packet.reply(response);

        DataCompilation afterData = new DataCompilation();
        afterData.add("inventory", Lookup.getUserInventories(user));
        afterData.add("commander", Lookup.getCommander(commander));

        UserActionLog action = UserActionLog.builder()
            .action("user-seal-commander")
            .message("[Commander Seal] " + user.getUsername() + " (Name: " + commander.getName() + ", Stars: " + commander.getStars() + ", Electron: " + commander.getGrowthElectron() + ", Dodge: " + commander.getGrowthDodge() + ", Speed: " + commander.getGrowthSpeed() + ", Accuracy: " + commander.getGrowthAim() + ", Variance: " + commander.getVariance() + ")")
            .beforeData(beforeData)
            .afterData(afterData)
            .build();

        // EventLogger.sendUserAction(action, user, (GameServerReceiver) packet.getSmartServer());

        // Audit
        Account account = user.getAccount();

        String buffer = "**User:** `" + user.getUsername() + " (ID: " + user.getGuid() + ", EMAIL: " + account.getEmail() + ")`\n" +
                "**Commander:** `" + commander.getName() + " (CommanderId: " + commander.getCommanderId() + ", Stars: " + (commander.getStars() + 1) + ", Electron: " + commander.getGrowthElectron() + ", Dodge: " + commander.getGrowthDodge() + ", Speed: " + commander.getGrowthSpeed() + ", Accuracy: " + commander.getGrowthAim() + ", Variance: " + commander.getVariance() + ")`";

        DiscordService.getInstance().getRayoBot().sendAudit("Commander Seal", buffer, Color.decode("0x1c27ff"), AuditType.COMMANDER);

    }

    @PacketProcessor
    public void onResume(RequestResumeCommanderPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Prop card = user.getInventory().getProp(904);
        if (card == null) {
            return;
        }

        Commander commander = user.getCommander(packet.getCommanderId());
        if (commander == null || commander.getState() != 1) {
            return;
        }

        Pair<Boolean, Boolean> removedProp = user.getInventory().removeProp(card, 1);
        if (!removedProp.getKey()) {
            return;
        }

        ResponseResumeCommanderPacket response = CommanderService.getInstance().getResumeCommanderPacket(card, commander);
        user.save();

        packet.reply(response);

    }

    @PacketProcessor
    public void onRelive(RequestReliveCommanderPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Prop card = user.getInventory().getProp(903);
        if (card == null) {
            return;
        }

        Commander commander = user.getCommander(packet.getCommanderId());
        if (commander == null || commander.getState() != 2) {
            return;
        }

        Pair<Boolean, Boolean> removedProp = user.getInventory().removeProp(card, 1);
        if (!removedProp.getKey()) {
            return;
        }

        ResponseReliveCommanderPacket response = CommanderService.getInstance().getReliveCommanderPacket(card, commander);
        user.save();

        packet.reply(response);

    }

    @PacketProcessor
    public void onReset(RequestClearCommanderPercentPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Prop card = user.getInventory().getProp(924);
        if (card == null) {
            return;
        }

        Commander commander = user.getCommander(packet.getCommanderId());
        if (commander == null || commander.hasGems()) {
            return;
        }

        Fleet fleet = commander.getFleet();
        if (fleet != null && (fleet.isInMatch() || fleet.isMatch() || fleet.isInTransmission())) {
            return;
        }

        DataCompilation beforeData = new DataCompilation();
        beforeData.add("inventory", Lookup.getUserInventories(user));
        beforeData.add("commander", Lookup.getCommander(commander));

        Pair<Boolean, Boolean> removedProp = user.getInventory().removeProp(card, 1);
        if (!removedProp.getKey()) {
            return;
        }

        ResponseClearCommanderPercentPacket response = CommanderService.getInstance().getResetCommanderPacket(removedProp.getValue(), commander);

        user.getMetrics().add("action:reset.commander", 1);
        user.update();
        user.save();

        packet.reply(response);

        DataCompilation afterData = new DataCompilation();
        afterData.add("inventory", Lookup.getUserInventories(user));
        afterData.add("commander", Lookup.getCommander(commander));

        UserActionLog action = UserActionLog.builder()
            .action("user-reset-commander")
            .message("[Commander Reset] " + user.getUsername() + " (Name: " + commander.getName() + ", Stars: " + commander.getStars() + ", Electron: " + commander.getGrowthElectron() + ", Dodge: " + commander.getGrowthDodge() + ", Speed: " + commander.getGrowthSpeed() + ", Accuracy: " + commander.getGrowthAim() + ", Variance: " + commander.getVariance() + ")")
            .beforeData(beforeData)
            .afterData(afterData)
            .build();

        // EventLogger.sendUserAction(action, user, (GameServerReceiver) packet.getSmartServer());

        // Audit
        Account account = user.getAccount();

        String buffer = "**User:** `" + user.getUsername() + " (ID: " + user.getGuid() + ", EMAIL: " + account.getEmail() + ")`\n" +
                "**Commander:** `" + commander.getName() + " (CommanderId: " + commander.getCommanderId() + ", Stars: " + (commander.getStars() + 1) + ", Electron: " + commander.getGrowthElectron() + ", Dodge: " + commander.getGrowthDodge() + ", Speed: " + commander.getGrowthSpeed() + ", Accuracy: " + commander.getGrowthAim() + ", Variance: " + commander.getVariance() + ")`";

        DiscordService.getInstance().getRayoBot().sendAudit("Commander Reset", buffer, Color.decode("0x2bdfff"), AuditType.COMMANDER);

    }

    @PacketProcessor
    public void onCommanderStoneInfo(RequestCommanderStoneInfoPacket packet) {

        Commander commander = CommanderService.getInstance().getCommander(packet.getCommanderId());

        ResponseCommanderStoneInfoPacket response = CommanderService.getInstance().getCommanderStoneInfo(commander);

        packet.reply(response);

    }

    @PacketProcessor
    public void onDeleteCommander(RequestDeleteCommanderPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Commander commander = CommanderService.getInstance().getCommander(packet.getCommanderId());
        if (commander == null || commander.getUserId() != user.getUserId() || commander.hasGems() || commander.hasChips() || commander.getState() == 4 || commander.hasFleet()) {
            return;
        }

        DataCompilation beforeData = new DataCompilation();
        beforeData.add("commander", Lookup.getCommander(commander));

        ResponseDeleteCommanderPacket response = CommanderService.getInstance().getDeleteCommanderPacket(commander);
        if (response == null) {
            return;
        }

        packet.reply(response);

        /*DataCompilation afterData = new DataCompilation();
        afterData.add("commanders", Lookup.getUserCommanders(user));

        UserActionLog action = UserActionLog.builder()
                .action("user-delete-commander")
                .message("[Commander Delete] " + user.getUsername() + " (Name: " + commander.getName() + ", CommanderId: " + commander.getCommanderId() + ", Stars: " + commander.getStars() + ", Electron: " + commander.getGrowthElectron() + ", Dodge: " + commander.getGrowthDodge() + ", Speed: " + commander.getGrowthSpeed() + ", Accuracy: " + commander.getGrowthAim() + ", Variance: " + commander.getVariance() + ")")
                .beforeData(beforeData)
                .afterData(afterData)
                .build();*/

        // EventLogger.sendUserAction(action, user, (GameServerReceiver) packet.getSmartServer());

        // Audit
        Account account = user.getAccount();

        String buffer = "**User:** `" + user.getUsername() + " (ID: " + user.getGuid() + ", EMAIL: " + account.getEmail() + ")`\n" +
                "**Commander:** `" + commander.getName() + " (CommanderId: " + commander.getCommanderId() + ", Stars: " + (commander.getStars() + 1) + ", Electron: " + commander.getGrowthElectron() + ", Dodge: " + commander.getGrowthDodge() + ", Speed: " + commander.getGrowthSpeed() + ", Accuracy: " + commander.getGrowthAim() + ", Variance: " + commander.getVariance() + ")`";

        DiscordService.getInstance().getRayoBot().sendAudit("Commander Dismiss", buffer, Color.decode("0xf54284"), AuditType.COMMANDER);

    }

    @PacketProcessor
    public void onUnionDoubleCommanderCard(RequestUnionDoubleSkillCardPacket packet) throws BadGuidException {
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if(user == null) return;

        Prop card1 = user.getInventory().getProp(packet.getCard1());
        Prop card2 = user.getInventory().getProp(packet.getCard2());
        if(card1 == null || card2 == null || card1.getPropNum() < 1 || card2.getPropNum() < 1) return;
        if(!card1.isCommander() || !card2.isCommander()) return;

        PropCommanderData card1Data = card1.getData().getCommanderData();
        PropCommanderData card2Data = card2.getData().getCommanderData();
        if(card1Data == null || card2Data == null) return;

        PropData baseCard1Data = CommanderService.getInstance().getCommanderPropData(packet.getCard1());
        PropData baseCard2Data = CommanderService.getInstance().getCommanderPropData(packet.getCard2());
        if(baseCard1Data == null || baseCard2Data == null) return;

        int stars1 =  card1.getPropId() - baseCard1Data.getId();
        int stars2 =  card2.getPropId() - baseCard2Data.getId();

        if(stars1 < 0 || stars1 > 8 || stars2 < 0 || stars2 > 8) return;

        int maxStars = Math.max(stars1, stars2);
        int minStars = Math.min(stars1, stars2);

        int resultStars = (int) Math.floor((maxStars + minStars) / 2.0);

        Prop scroll = user.getInventory().getProp(packet.getGoods());
        if(scroll == null) return;

        // Check if scroll is restricted
        if (RestrictedItemsService.getInstance().isRestricted(scroll.getPropId())) {
            packet.getSmartServer().sendMessage("This item is not available in early game.");
            return;
        }

        boolean scrollLocked = packet.getGoodsLockFlag() == 1;

        if(scrollLocked && scroll.getPropLockNum() < 1) return;
        if(!scrollLocked && scroll.getPropNum() < 1) return;
        if(!user.getInventory().hasProp(scroll.getPropId(), 1, 0, scrollLocked)) return;

        PropScrollData scrollData = scroll.getData().getScrollData();
        if(scrollData == null) return;

        Optional<PropData> result = findUnionDouble(scrollData, card1Data, card2Data);
        if(result == null || !result.isPresent()) return;

        PropData resultPropData = result.get();
        if(resultPropData == null) return;

        PropCommanderData resultCommanderData = resultPropData.getCommanderData();
        if(resultCommanderData == null) return;

        // System.out.println("A1");

        int propResultId = resultPropData.getId() + resultStars;

        int chipSuccess = 0;
        int corpMergeBonus = 0;

        Prop toRemoveChip = null;

        if(packet.getChip() == 925 || packet.getChip() == 944) {

            Prop chip = user.getInventory().getProp(packet.getChip());

            if(user.getInventory().hasProp(chip.getPropId(), 1, 0, packet.getChipLockFlag() == 1)) {
                toRemoveChip = chip;
                chipSuccess += packet.getGoods() == 925 ? 20 : 30;
            }

        }

        Corp corp  = CorpService.getInstance().getCorpByUser(user.getGuid());
        if(corp != null) corpMergeBonus += corp.getMergeBonus() * 100.0d;

        int success = 70 + chipSuccess + corpMergeBonus;

        if(MathUtil.random(0, 100) > success) {

            ResponseUnionDoubleSkillCardPacket response = CommanderService.getInstance().getUnionDoubleCommanderCardPacket(-1, packet.getCard1(), packet.getCard2(), packet.getGoods(), packet.getGoodsLockFlag(), packet.getChip(), packet.getChipLockFlag());
            packet.reply(response);

            if(resultStars > 3) {

                ResponseUnionCommanderCardBroPacket broadcast = CommanderService.getInstance().getUnionCommanderCardBroPacket(user, resultCommanderData.getCommander().getId(), resultStars, true);
                PacketRouter.getInstance().broadcast(broadcast);

            }

            user.getInventory().removeOneProp(scroll, scrollLocked);
            user.update();
            user.save();
            return;

        }

        if(!user.getInventory().hasProp(card1.getPropId(), 1, 0, false)) return;
        if(!user.getInventory().hasProp(card2.getPropId(), 1, 0, false)) return;
        if(!user.getInventory().addProp(propResultId, 1, 0, false)) return;

        if(toRemoveChip != null) user.getInventory().removeOneProp(toRemoveChip, packet.getChipLockFlag() == 1);
        user.getInventory().removeOneProp(scroll, scrollLocked);
        user.getInventory().removeOneProp(card1, false);
        user.getInventory().removeOneProp(card2, false);

        ResponseUnionDoubleSkillCardPacket response = CommanderService.getInstance().getUnionDoubleCommanderCardPacket(propResultId, packet.getCard1(), packet.getCard2(), packet.getGoods(), packet.getGoodsLockFlag(), packet.getChip(), packet.getChipLockFlag());
        packet.reply(response);

        if(resultStars > 5) {

            ResponseUnionCommanderCardBroPacket broadcast = CommanderService.getInstance().getUnionCommanderCardBroPacket(user, resultCommanderData.getCommander().getId(), resultStars, false);
            PacketRouter.getInstance().broadcast(broadcast);

        }

        user.update();
        user.save();
    }

    private Optional<PropData> findUnionDouble(PropScrollData scrollData, PropCommanderData card1, PropCommanderData card2) {

        String commanderNameId1 = card1.getCommander().getName();
        String commanderNameId2 = card2.getCommander().getName();
        if (commanderNameId1.equals(commanderNameId2)) {
            return Optional.empty();
        }

        String[] skills = scrollData.getSrcCommander();
        if (skills == null || skills.length != 2) {
            return Optional.empty();
        }

        if (skills[0].equals(commanderNameId1) && skills[1].equals(commanderNameId2)) {
            return scrollData.getPropDataResult();
        }
        if (skills[0].equals(commanderNameId2) && skills[1].equals(commanderNameId1)) {
            return scrollData.getPropDataResult();
        }

        return Optional.empty();

    }

}