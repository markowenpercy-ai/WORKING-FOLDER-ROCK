package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

import java.util.*;

@Data
public class ShipDestroyInfo extends BufferObject {

    private List<ShipTeamNum> teamNums = new ArrayList<>();

    @Override
    public void read(Go2Buffer go2Buffer) {

        for (int i = 0; i < 100; i++) {

            ShipTeamNum shipTeamNum = new ShipTeamNum();
            shipTeamNum.read(go2Buffer);
            teamNums.add(shipTeamNum);

        }

    }

}
