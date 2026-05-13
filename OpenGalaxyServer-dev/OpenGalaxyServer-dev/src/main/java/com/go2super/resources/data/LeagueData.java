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
public class LeagueData extends JsonData {
    private Integer rank;
    private Integer dailyHonor;
    private String period1Start;
    private String period1End;
    private String period2Start;
    private String period2End;
    private Integer totalPlayers;
    private Integer promoted;
    private Integer downgraded;
    private Integer unchanged;
}
