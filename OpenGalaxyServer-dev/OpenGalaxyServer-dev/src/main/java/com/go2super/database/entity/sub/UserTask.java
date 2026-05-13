package com.go2super.database.entity.sub;

import com.go2super.obj.game.TaskInfo;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.TaskData;
import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserTask {

    private int taskId;
    private int type; // 0 = Main, 1 = Side, 2 = Daily
    private int level;
    private int value;

    private boolean complete;
    private boolean obtainable;
    private boolean redeemed;

    public TaskInfo getTaskInfo(int type) {

        return new TaskInfo(taskId, value, type, level, complete ? 1 : 0, 0);
    }

    public TaskData getMainTaskData() {

        return ResourceManager.getTasks().getMainTask(taskId);
    }

    public TaskData getSideTaskData() {

        return ResourceManager.getTasks().getSideTask(taskId, level);
    }

}
