package com.go2super.resources.json;

import com.go2super.resources.data.LeagueData;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class LeaguesJson {

    private List<LeagueData> leagues;

    public LeagueData lookup(int leagueId) {
            for (LeagueData data : leagues) {
                if (data.getRank() == leagueId) {
                    return data;
                }
            }
            return null;
    }

}