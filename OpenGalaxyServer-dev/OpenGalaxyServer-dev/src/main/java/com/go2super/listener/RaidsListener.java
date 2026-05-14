package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.logger.BotLogger;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.chat.ChatMessagePacket;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.packet.raids.RequestCaptureStatePacket;
import com.go2super.packet.raids.ResponseCaptureArkInfoPacket;
import com.go2super.packet.raids.ResponseCaptureArkListPacket;
import com.go2super.service.*;
import com.go2super.service.exception.BadGuidException;
import lombok.Data;

import java.time.LocalDateTime;

public class RaidsListener implements PacketListener {

    @PacketProcessor
    public void onCaptureState(RequestCaptureStatePacket packet) throws BadGuidException {

        if (!RaidsService.getInstance().getEnabled().get()) {
            return;
        }
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }
        //add user to the pool for replying (use guid as key for reliable hashing)
        RaidsService.getInstance().getUserIds().put(user.getGuid(), LocalDateTime.now());
        int request = packet.getRequest().getValue();
        //request 0 == open raid window
        //request 1 == cancel
        //request 2 == observe
        //request 3 == back to space station=
        switch (request) {
            case 0 -> {
                ResponseCaptureArkInfoPacket captureArkInfoPacket = RaidsService.getInstance().getArkInfoPacket(user);
                ResponseCaptureArkListPacket captureArkListPacket = RaidsService.getInstance().getArkRoomsPacket(user);

                packet.reply(captureArkListPacket, captureArkInfoPacket);
            }
            case 1 -> {
                UserInventory inventory = user.getInventory();
                ResponseUsePropsPacket responseUsePropsPacket = null;

                for (var raid : RaidsService.getInstance().getRaids()) {
                    if (raid.getFirstGuid() == packet.getGuid()) {
                        inventory.addProp(raid.getFirstPropId(), 1, 0, true);
                        responseUsePropsPacket = ResourcesService.getInstance().genericUseProps(raid.getFirstPropId(), -1, 1, 1);
                        raid.setFirstPropId(-1);
                        raid.setFirstGuid(-1);
                        raid.setFirstDefenceFleets(new ArrayList<>());
                        break;
                    }
                    if (raid.getSecondGuid() == packet.getGuid()) {
                        inventory.addProp(raid.getSecondPropId(), 1, 0, true);
                        responseUsePropsPacket = ResourcesService.getInstance().genericUseProps(raid.getSecondPropId(), -1, 1, 1);
                        raid.setSecondGuid(-1);
                        raid.setSecondPropId(-1);
                        raid.setSecondDefenceFleets(new ArrayList<>());
                        break;
                    }
                }
                ResponseCaptureArkInfoPacket captureArkInfoPacket = RaidsService.getInstance().getArkInfoPacket(user);

                if (responseUsePropsPacket != null) {
                    packet.reply(responseUsePropsPacket);
                }
                packet.reply(captureArkInfoPacket);
                RaidsService.getInstance().broadcastStatus();
            }
            default -> {
                ResponseCaptureArkInfoPacket captureArkInfoPacket = RaidsService.getInstance().getArkInfoPacket(user);
                ResponseCaptureArkListPacket captureArkListPacket = RaidsService.getInstance().getArkRoomsPacket(user);
                packet.getSmartServer().sendMessage("Sorry, that instance is not done yet!");
                packet.reply(captureArkListPacket, captureArkInfoPacket);
            }
        }

    }

}
