package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TaskAwardMeta extends JsonData {

    private double metal;
    private double gold;
    private double he3;
    private double vouchers;

    private int propId;
    private int propNum;

}
