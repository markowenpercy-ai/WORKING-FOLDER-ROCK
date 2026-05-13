package com.go2super.service;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserTask;
import com.go2super.database.entity.sub.UserTasks;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.ByteArray;
import com.go2super.obj.game.TaskInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.task.ResponseTaskInfoPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.TaskData;
import com.go2super.resources.data.meta.TaskRequirementMeta;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.*;

@Getter
@Service
public class TaskService {

    private static TaskService instance;

    public TaskService() {

        instance = this;

    }

    public void updateTasks(User user) {

        UserTasks userTasks = user.getTasks();
        if (userTasks == null) {
            return;
        }
        Boolean allCompleted = userTasks.getCompleted().stream().filter(x -> x.getType() == 0).toList().size() >= ResourceManager.getTasks().getMainTasks().size();
        if(allCompleted){
            userTasks.setCurrentMain(UserTask.builder().taskId(-1).type(0).level(-1).build());
        }
        if (userTasks.getCurrentMain() == null) {
            userTasks.setCurrentMain(UserTask.builder().taskId(0).type(0).level(-1).build());
        }

        if (userTasks.getCurrentSide() == null) {
            userTasks.setCurrentSide(new ArrayList<>());
        }

        updateMainTask(userTasks, user);
        boolean update = updateSideTasks(userTasks, user);
        if (update) {
            sendTaskUpdate(user);
        }

    }

    public boolean updateMainTask(UserTasks userTasks, User user) {

        UserTask mainTask = userTasks.getCurrentMain();
        TaskData currentTask;

        if (mainTask == null) {
            return false;
        }
        currentTask = mainTask.getMainTaskData();

        if (mainTask.isComplete() && mainTask.isObtainable() && mainTask.isRedeemed()) {

            UserTask last = mainTask;
            mainTask = userTasks.nextMain();

            // No more main tasks
            if (mainTask == null) {

                userTasks.getCompleted().add(last);
                userTasks.setCurrentMain(null);
                return true;

            }

            userTasks.getCompleted().add(last);
            userTasks.setCurrentMain(mainTask);
            currentTask = mainTask.getMainTaskData();

            //

        }

        if (mainTask.isComplete()) {

            mainTask.setObtainable(true);
            mainTask.setRedeemed(false);

            return true;

        }

        TaskData tempData = currentTask;
        if(tempData == null){
            List<TaskData> tasks = ResourceManager.getTasks().getMainTasks();
            TaskData lastTask = tasks.get(tasks.size() - 1);
            tempData = lastTask;
        }
        TaskRequirementMeta[] requirements = tempData.getRequirement();
        boolean completed = true;

        for (TaskRequirementMeta requirement : requirements) {
            if (!requirement.hasCompleted(user)) {
                // BotLogger.log("Requirement not succeed: " + requirement);
                completed = false;
                break;
            }
        }

        if (completed) {

            mainTask.setComplete(true);
            mainTask.setObtainable(true);
            mainTask.setRedeemed(false);

            return true;

        }

        mainTask.setComplete(false);
        mainTask.setObtainable(false);
        mainTask.setRedeemed(false);

        return false;

    }

    public boolean updateSideTasks(UserTasks userTasks, User user) {

        boolean update = false;

        for (TaskData task : ResourceManager.getTasks().getSideTasks()) {

            if (userTasks.hasCompleted(task.getTaskId(), task.getLv())) {
                continue;
            }

            if (!userTasks.hasCompleted(task.getDepends(), -1)) {
                continue;
            }

            if (task.getLv() > 0 && !userTasks.hasCompleted(task.getTaskId(), task.getLv() - 1)) {
                continue;
            }

            UserTask sideTask = userTasks.getCurrentSide().stream().filter(userTask -> userTask.getTaskId() == task.getTaskId() && userTask.getLevel() == task.getLv()).findFirst().orElse(null);

            if (sideTask == null) {

                sideTask = UserTask.builder()
                    .taskId(task.getTaskId())
                    .level(task.getLv())
                    .type(1)
                    .build();

            }

            if (sideTask.isComplete() && sideTask.isObtainable() && sideTask.isRedeemed()) {

                userTasks.getCurrentSide().remove(sideTask);
                userTasks.getCompleted().add(sideTask);

                update = true;
                continue;

            }

            if (sideTask.isComplete() && sideTask.isObtainable() && !sideTask.isRedeemed()) {
                continue;
            }
            if (!userTasks.getCurrentSide().contains(sideTask)) {
                userTasks.getCurrentSide().add(sideTask);
            }

            TaskRequirementMeta[] requirements = task.getRequirement();
            boolean completed = true;

            for (TaskRequirementMeta requirement : requirements) {
                if (!requirement.hasCompleted(user)) {
                    completed = false;
                    break;
                }
            }

            if (completed) {

                sideTask.setComplete(true);
                sideTask.setObtainable(true);
                sideTask.setRedeemed(false);

                update = true;
                continue;

            }

            sideTask.setComplete(false);
            sideTask.setObtainable(false);
            sideTask.setRedeemed(false);

        }

        return update;

    }

    public void sendTaskUpdate(User user) {

        Optional<LoggedGameUser> gameUserOptional = user.getLoggedGameUser();

        if (gameUserOptional.isPresent()) {

            ResponseTaskInfoPacket response = getTaskInfo(user);
            gameUserOptional.get().getSmartServer().send(response);

        }

    }

    public ResponseTaskInfoPacket getTaskInfo(User user) {

        UserTasks userTasks = user.getTasks();
        Boolean allCompleted = userTasks.getCompleted().stream().filter(x -> x.getType() == 0).toList().size() >= ResourceManager.getTasks().getMainTasks().size();
        if(allCompleted){
            userTasks.setCurrentMain(UserTask.builder().taskId(-1).type(0).level(-1).build());
        }
        if (userTasks.getCurrentMain() == null ) {
            userTasks.setCurrentMain(UserTask.builder().taskId(0).type(0).level(-1).build());
        }

        if (userTasks.getCurrentSide() == null) {
            userTasks.setCurrentSide(new ArrayList<>());
        }

        UserTask currentMain = userTasks.getCurrentMain();
        List<UserTask> currentSide = userTasks.getCurrentSide();

        ResponseTaskInfoPacket response = new ResponseTaskInfoPacket();
        response.setAwardLen((short) 0);

        // Los premios claimeados
        // este arreglo utiliza AwardLen
        response.setByteArray(new ByteArray(new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,}));

        if (currentMain != null) {
            response.getTaskInfos().add(currentMain.getTaskInfo(0));
        } else {
            response.getTaskInfos().add(new TaskInfo(192, -1, 0, -1, 0, 0));
        }
        response.getTaskInfos().addAll(currentSide.stream().map(userTask -> userTask.getTaskInfo(1)).collect(Collectors.toList()));

        response.setDataLen((short) response.getTaskInfos().size());

        //for(int i = response.getTaskInfos().size(); i < 100; i++)
        //    response.getTaskInfos().add(new TaskInfo(-1, -1, -1, -1, -1, -1));
        return response;

    }

    public static TaskService getInstance() {

        return instance;
    }

}
