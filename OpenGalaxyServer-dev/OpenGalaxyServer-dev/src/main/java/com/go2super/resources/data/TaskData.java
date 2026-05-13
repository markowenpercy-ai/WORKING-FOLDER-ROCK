package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.TaskAwardMeta;
import com.go2super.resources.data.meta.TaskRequirementMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TaskData extends JsonData {

    private int taskId;
    private String taskName;
    private String goal;

    private int lv = -1;
    private int depends = -1;

    private TaskRequirementMeta[] requirement;
    private TaskAwardMeta award;

}
