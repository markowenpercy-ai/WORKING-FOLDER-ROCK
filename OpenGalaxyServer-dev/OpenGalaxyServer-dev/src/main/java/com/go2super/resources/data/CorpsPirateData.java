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
public class CorpsPirateData extends JsonData {

    private String name;
    private String comment;

    private int level;
    private int corpsLevel;
    private int honor;
    private int wealth;
    private int shipTeamNum;

}
