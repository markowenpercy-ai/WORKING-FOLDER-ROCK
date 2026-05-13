package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.database.entity.sub.UserStorage;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.boot.RequestPlayerResourcePacket;
import com.go2super.packet.construction.RequestGetStorageResourcePacket;
import com.go2super.packet.construction.ResponseGetStorageResourcePacket;
import com.go2super.service.LoginService;
import com.go2super.service.ResourcesService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

public class ResourceListener implements PacketListener {

    @PacketProcessor
    public void onPlayerResource(RequestPlayerResourcePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        user.update();
        user.save();

        packet.reply(ResourcesService.getInstance().getPlayerResourcePacket(user));

    }

    @PacketProcessor
    public void onGetResources(RequestGetStorageResourcePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        user.update();

        UserStorage storage = user.getStorage();
        UserResources resources = user.getResources();

        int totalGold = storage.getGold();
        int totalHe3 = storage.getHe3();
        int totalMetal = storage.getMetal();

        resources.addGold(totalGold);
        resources.addHe3(totalHe3);
        resources.addMetal(totalMetal);

        storage.reset();

        ResponseGetStorageResourcePacket response = new ResponseGetStorageResourcePacket();

        response.setMoney(totalGold);
        response.setGas(totalHe3);
        response.setMetal(totalMetal);

        user.getMetrics().add("action:harvest", 1);
        user.update();
        user.save();

        packet.reply(response);
        // System.out.println("onGetResources: " + (end - time) + "ms");

    }

}
