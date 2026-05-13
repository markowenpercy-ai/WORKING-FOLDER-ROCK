package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DailyTaskData extends JsonData {
    private int id;
    private String name;
    private int earnPoints;
    private String require;
    private int limit;
}
