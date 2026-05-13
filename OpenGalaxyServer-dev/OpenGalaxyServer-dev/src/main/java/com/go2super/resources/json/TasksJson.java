package com.go2super.resources.json;

import com.go2super.resources.data.TaskData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class TasksJson {

    private List<TaskData> mainTasks;
    private List<TaskData> sideTasks;

    public TaskData getSideTask(int taskId, int lv) {

        for (TaskData sideTaskData : sideTasks) {
            if (sideTaskData.getTaskId() == taskId && sideTaskData.getLv() == lv) {
                return sideTaskData;
            }
        }
        return null;
    }

    public TaskData getMainTask(int taskId) {

        for (TaskData mainTaskData : mainTasks) {
            if (mainTaskData.getTaskId() == taskId) {
                return mainTaskData;
            }
        }
        return null;
    }

    public TaskData getNextMainTask(TaskData mainTaskData) {

        int index = mainTasks.indexOf(mainTaskData);

        if (index + 1 >= mainTasks.size()) {
            return null;
        }

        return mainTasks.get(index + 1);

    }

}
