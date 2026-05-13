package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class ShipHe3Info extends BufferObject {

    private SmartString shipName = SmartString.of(32);

    private int shipTeamId;
    private int shipNum;
    private int shipSpace;
    private int gas;

    // supply is the total fleet he3 consumption (fuel usage)
    private int supply;

    public ShipHe3Info(String shipName, int shipTeamId, int shipNum, int shipSpace, int gas, int supply) {

        this.shipName.value(shipName);

        this.shipTeamId = shipTeamId;
        this.shipNum = shipNum;
        this.shipSpace = shipSpace;
        this.gas = gas;
        this.supply = supply;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.position() % 4) % 4);

        go2buffer.addString(shipName.getValue(), shipName.getSize());
        go2buffer.addInt(shipTeamId);
        go2buffer.addInt(shipNum);
        go2buffer.addInt(shipSpace);
        go2buffer.addInt(gas);

        go2buffer.addInt(supply);

    }

}
