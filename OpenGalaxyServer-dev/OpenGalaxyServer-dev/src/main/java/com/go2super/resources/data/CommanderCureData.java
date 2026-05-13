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
public class CommanderCureData extends JsonData {

    private int commandType;
    private int commandStar;

    private int hospitalization;

    private double death;
    private double level;

}
