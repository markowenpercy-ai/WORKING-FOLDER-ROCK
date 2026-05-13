package com.go2super.listener;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.Prop;
import com.go2super.obj.type.AuditType;
import com.go2super.obj.type.PropAction;
import com.go2super.obj.utility.PropConsumption;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.props.*;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropCommanderData;
import com.go2super.service.CorpService;
import com.go2super.service.DiscordService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

import java.awt.*;
import java.util.List;

public class PropListener implements PacketListener {

    @PacketProcessor
    public void useProp(RequestUsePropsPacket packet) {

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        PropAction consumption = PropAction.getAction(packet.getPropsId());

        if (consumption == null) {
            BotLogger.error("PropId :: [" + packet.getPropsId() + "] is not found");
            return;
        }

        if (packet.getNum() <= 0) {
            return;
        }

        Prop prop = user.getInventory().getProp(packet.getPropsId(), 0);
        boolean lock = packet.getLockFlag() == 1;

        if (prop == null) {
            return;
        }
        if (!user.getInventory().hasProp(prop.getPropId(), packet.getNum(), 0, lock)) {
            return;
        }

        // BotLogger.info("User :: [" + user.getGuid() + "] is using prop :: [" + prop.getPropId() + "]");

        PropConsumption action = consumption.getAction();
        boolean consumed = action.consume(prop, packet.getNum(), lock, packet, user);

        if (!consumed) {
            return;
        }
        user.getInventory().removeProp(prop.getPropId(), packet.getNum(), 0, lock);

    }

    @PacketProcessor
    public void deleteProp(RequestDeletePropsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }
        Prop prop = user.getInventory().getProp(packet.getPropsId(), 0);

        if (prop == null) {
            return;
        }
        if (!user.getInventory().removeProp(prop, 1, packet.getLockFlag() == 1)) {
            return;
        }

        ResponseDeletePropsPacket response = ResponseDeletePropsPacket.builder()
            .propsId(prop.getPropId())
            .lockFlag(packet.getLockFlag())
            .build();

        user.save();
        packet.reply(response);

        // Audit
        Account account = user.getAccount();

        String buffer = "**User:** `" + user.getUsername() + " (ID: " + user.getGuid() + ", EMAIL: " + account.getEmail() + ")`\n" +
                "**Item:** `" + getItemName(prop) + " (Amount: " + 1 + ", Bound: " + (packet.getLockFlag() == 1 ? "Yes" : "No") + ")`";

        DiscordService.getInstance().getRayoBot().sendAudit("Inventory Item Delete", buffer, Color.decode("0xe3ac3d"), AuditType.DELETE);

    }

    @PacketProcessor
    public void onMoveProp(RequestMovePropPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        if (packet.getPropNum() <= 0 || packet.getPropId() < 0) {
            return;
        }

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpByUser(packet.getGuid());
        if (corp == null) {
            return;
        }

        boolean lock = packet.getLockFlag() == 1;
        boolean toCorp = packet.getKind() == 0;

        Prop prop = toCorp ?
                    user.getInventory().getProp(packet.getPropId(), 0) :
                    user.getCorpInventory().getProp(packet.getPropId(), 1);

        if (prop == null) {
            return;
        }
        int toNum = packet.getPropNum();

        if (lock && toNum > prop.getPropLockNum()) {
            return;
        }
        if (!lock && toNum > prop.getPropNum()) {
            return;
        }

        if (toCorp) {

            if (!user.getCorpInventory().addProp(prop.getPropId(), toNum, 1, lock)) {
                return;
            }
            if (!user.getInventory().removeProp(prop, toNum, lock)) {
                return;
            }

        } else {

            if (!user.getInventory().addProp(prop.getPropId(), toNum, 0, lock)) {
                return;
            }
            if (!user.getCorpInventory().removeProp(prop, toNum, lock)) {
                return;
            }

        }

        user.update();
        user.save();

        ResponseMovePropPacket response = ResponseMovePropPacket.builder()
            .propId(packet.getPropId())
            .propNum(packet.getPropNum())
            .lockFlag(packet.getLockFlag())
            .kind(packet.getKind())
            .build();

        packet.reply(response);

    }

    public String getItemName(Prop prop) {

        int propId = prop.getPropId();
        boolean isCommander = false;

        PropData propData = null;

        // Commanders

        List<PropData> props = ResourceManager.getProps().getCommanders();

        for (PropData data : props) {
            if (data.hasCommanderData()) {
                if ((data.getId() + 8) >= propId && data.getId() <= propId) {
                    propData = data;
                    isCommander = true;
                    break;
                }
            }
        }

        if (propData == null) {
            for (PropData data : ResourceManager.getProps().getProps()) {
                if (data.getId() == propId) {
                    propData = data;
                    break;
                }
            }
        }

        if (propData != null) {

            if (isCommander) {

                PropCommanderData commanderData = propData.getCommanderData();
                return commanderData.getCommander().getName() + " " + ((propId - propData.getId()) + 1) + "*";

            }

            return propData.getName();

        }

        return "unknown";

    }

}
