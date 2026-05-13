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
public class CommanderPullulateData extends JsonData {

    private int commandType;
    private int commandStar;

    private int minPullulate;
    private int maxPullulate;

    private int frigate;
    private int cruiser;
    private int battleship;

}
