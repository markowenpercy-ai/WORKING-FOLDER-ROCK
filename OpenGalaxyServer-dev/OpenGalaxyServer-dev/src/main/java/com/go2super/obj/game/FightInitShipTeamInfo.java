package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FightInitShipTeamInfo extends BufferObject {

    private String teamName = "";
    private String commanderName = "";
    private String teamOwnerName = "";
    private String consortiaName = "";

    private long userId;
    private int shipTeamId = -1;

    private int gas;
    private int storage;

    private IntegerArray maxShield = new IntegerArray(9);
    private IntegerArray maxEndure = new IntegerArray(9);
    private IntegerArray shield = new IntegerArray(9);
    private IntegerArray endure = new IntegerArray(9);

    private ShipTeamBody teamBody;

    private int skillId;
    private int reserve;

    private int attackObjInterval;
    private int attackObjType;

    private int levelId;
    private int cardLevel;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addString(teamName, 32);
        go2buffer.addString(commanderName, 32);
        go2buffer.addString(teamOwnerName, 32);
        go2buffer.addString(consortiaName, 32);

        go2buffer.addLong(userId);
        go2buffer.addInt(shipTeamId);
        go2buffer.addUnsignInt(gas);
        go2buffer.addUnsignInt(storage);

        maxShield.write(go2buffer);
        maxEndure.write(go2buffer);
        shield.write(go2buffer);
        endure.write(go2buffer);
        teamBody.write(go2buffer);

        go2buffer.addShort(skillId);
        go2buffer.addShort(reserve);
        go2buffer.addChar(attackObjInterval);
        go2buffer.addChar(attackObjType);
        go2buffer.addUnsignChar(levelId);
        go2buffer.addChar(cardLevel);

    }

    @Override
    public FightInitShipTeamInfo trash() {

        return new FightInitShipTeamInfo();
    }

}