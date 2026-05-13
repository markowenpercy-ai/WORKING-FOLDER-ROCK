package com.go2super.service.raids;

import com.go2super.service.battle.Match;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Raid {

    private RaidStatus status;
    private int firstGuid;
    private int secondGuid;

    private int firstPropId;
    private int secondPropId;
    private int roomId;
    private int time;
    private ArrayList<Integer> firstDefenceFleets = new ArrayList<Integer>();
    private ArrayList<Integer> secondDefenceFleets = new ArrayList<Integer>();
    public int getRoomStatus(int guid){
        // 0 = None
        // 1 = Right
        // 2 = Left
        // 3 = Both (time freeze)
        // 4 = Both - Intercept Button (time freeze)
        if(firstGuid == -1 && secondGuid == -1){
            return 0;
        }
        else if(secondGuid == -1){
            return 1;
        }
        else if(firstGuid == -1){
            return 2;
        }
        else if(firstGuid == guid || secondGuid == guid){
            return 3;
        }
        else if(status == RaidStatus.IN_PROGRESS){
            return 4;
        }
        else{
            return 5;
        }
    }
}
