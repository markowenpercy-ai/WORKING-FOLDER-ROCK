package com.go2super.database.entity.sub;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.TaskData;
import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserTasks {

    private UserTask currentMain;
    private List<UserTask> currentSide = new ArrayList<>();
    private List<UserTask> completed = new ArrayList<>();

    public boolean hasCompleted(int taskId, int level) {

        return completed.stream().anyMatch(completed -> completed.getTaskId() == taskId && completed.getLevel() == level);
    }

    public UserTask nextMain() {
        int currentTaskId = currentMain.getTaskId();
        TaskData mainTaskData = ResourceManager.getTasks().getMainTask(currentTaskId);

        if (mainTaskData == null) {
            return null;
        }

        TaskData nextTask = ResourceManager.getTasks().getNextMainTask(mainTaskData);

        if (nextTask == null) {
            return null;
        }

        return UserTask.builder()
            .taskId(nextTask.getTaskId())
            .type(0)
            .value(0)
            .level(-1)
            .complete(false)
            .obtainable(false)
            .redeemed(false)
            .build();

    }

}
