package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.database.entity.sub.UserTask;
import com.go2super.database.entity.sub.UserTasks;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.task.RequestTaskGainPacket;
import com.go2super.packet.task.RequestTaskInfoPacket;
import com.go2super.packet.task.ResponseTaskGainPacket;
import com.go2super.packet.task.ResponseTaskInfoPacket;
import com.go2super.resources.data.TaskData;
import com.go2super.resources.data.meta.TaskAwardMeta;
import com.go2super.service.LoginService;
import com.go2super.service.TaskService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

import java.util.*;

public class TaskListener implements PacketListener {

    @PacketProcessor
    public void onTaskInfo(RequestTaskInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        ResponseTaskInfoPacket response = TaskService.getInstance().getTaskInfo(user);
        packet.reply(response);

    }

    @PacketProcessor
    public void onTaskGain(RequestTaskGainPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        if (packet.getKind() < 0 || packet.getKind() > 1) {
            return;
        }

        UserTasks userTasks = user.getTasks();
        UserInventory userInventory = user.getInventory();
        UserResources userResources = user.getResources();

        // BotLogger.log(packet);

        switch (packet.getKind()) {

            // Main Task Claim
            case 0:

                UserTask currentMain = userTasks.getCurrentMain();

                if (currentMain.getTaskId() != packet.getTaskId()) {
                    return;
                }

                if (currentMain.isComplete() && !currentMain.isRedeemed()) {

                    TaskData taskData = currentMain.getMainTaskData();

                    currentMain.setObtainable(true);
                    currentMain.setRedeemed(true);

                    TaskService.getInstance().updateTasks(user);
                    TaskAwardMeta awardMeta = taskData.getAward();

                    int gold = (int) awardMeta.getGold();
                    int metal = (int) awardMeta.getMetal();
                    int he3 = (int) awardMeta.getHe3();
                    int vouchers = (int) awardMeta.getVouchers();

                    int propId = awardMeta.getPropId();
                    int propNum = awardMeta.getPropNum();

                    if (awardMeta.getPropId() != -1) {
                        userInventory.addProp(propId, propNum, 0, true);
                    }

                    userResources.addGold(gold);
                    userResources.addMetal(metal);
                    userResources.addHe3(he3);
                    userResources.addVouchers(vouchers);

                    user.save();

                    UserTask nextMain = userTasks.getCurrentMain();

                    ResponseTaskGainPacket response = new ResponseTaskGainPacket();

                    response.setTaskId(packet.getTaskId());
                    response.setKind(packet.getKind());
                    response.setNextTaskId(nextMain == null ? -1 : nextMain.getTaskId());
                    response.setComplete(nextMain == null ? 0 : (nextMain.isComplete() ? 1 : 0));
                    response.setGas(he3);
                    response.setMetal(metal);
                    response.setMoney(gold);
                    response.setPropsId(propId);
                    response.setPropsNum(propNum);
                    response.setCoins(vouchers);

                    packet.reply(response);

                }

                return;

            // Side Task Claim
            case 1:

                List<UserTask> currentSide = userTasks.getCurrentSide();
                UserTask currentSideTask = currentSide.stream().filter(task -> task.getTaskId() == packet.getTaskId()).findFirst().orElse(null);

                if (currentSideTask == null) {
                    return;
                }

                if (currentSideTask.getTaskId() != packet.getTaskId()) {
                    return;
                }

                if (currentSideTask.isComplete() && !currentSideTask.isRedeemed()) {

                    TaskData taskData = currentSideTask.getSideTaskData();

                    currentSideTask.setObtainable(true);
                    currentSideTask.setRedeemed(true);

                    TaskService.getInstance().updateTasks(user);
                    TaskAwardMeta awardMeta = taskData.getAward();

                    int gold = (int) awardMeta.getGold();
                    int metal = (int) awardMeta.getMetal();
                    int he3 = (int) awardMeta.getHe3();
                    int vouchers = (int) awardMeta.getVouchers();

                    int propId = awardMeta.getPropId();
                    int propNum = awardMeta.getPropNum();

                    if (awardMeta.getPropId() != -1) {
                        userInventory.addProp(propId, propNum, 0, true);
                    }

                    userResources.addGold(gold);
                    userResources.addMetal(metal);
                    userResources.addHe3(he3);
                    userResources.addVouchers(vouchers);

                    user.save();

                    ResponseTaskGainPacket response = new ResponseTaskGainPacket();

                    response.setTaskId(packet.getTaskId());
                    response.setKind(packet.getKind());
                    response.setNextTaskId(-1);
                    response.setComplete(0);
                    response.setGas(he3);
                    response.setMetal(metal);
                    response.setMoney(gold);
                    response.setPropsId(propId);
                    response.setPropsNum(propNum);
                    response.setCoins(vouchers);

                    packet.reply(response);

                }

                break;

            case 2:

        }


    }

}
