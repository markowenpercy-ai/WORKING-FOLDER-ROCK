package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import lombok.Data;
import lombok.ToString;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_ID;

@Data
@ToString
public class Prop extends BufferObject {

    private int propId;
    private int propNum;
    private int propLockNum;

    private int storageType;
    private int reserve;

    public Prop(int propId, int propNum, int propLockNum, int storageType, int reserve) {

        this.propId = propId;
        this.propNum = propNum;
        this.propLockNum = propLockNum;
        this.storageType = storageType;
        this.reserve = reserve;
    }

    public PropData getData() {

        if (isCommander()) {

            int realPropId = propId;
            PropData propData = ResourceManager.getProps().getData(realPropId);

            while (propData == null) {

                propData = ResourceManager.getProps().getData(realPropId--);
                if (propData == null) {
                    continue;
                }

                propData = new PropData(realPropId, propData.getName(), propData.getType(), propData.getData(), null, propData.getSalvage());
                double baseSalvage = propData.getSalvage();

                // BotLogger.log("BASE SALVAGE: " + baseSalvage + ", FINAL: " + ((propId - realPropId) - 1) + ", RESULT: " + ((int) (baseSalvage * Math.pow(2, (propId - realPropId) - 1))));

                propData.setSalvage((int) (baseSalvage * Math.pow(2, (propId - realPropId) - 1)));
                return propData;

            }

            return propData;

        }

        return ResourceManager.getProps().getData(propId);

    }

    public boolean isCommander() {

        return propId >= 0 && propId <= 512 || propId >= 2007 && propId <= MAX_COMMANDER_ID;
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);
        go2buffer.addShort(propId);
        go2buffer.addShort(propNum);
        go2buffer.addShort(propLockNum);
        go2buffer.addChar(storageType);
        go2buffer.addChar(reserve);

    }

    public static Prop of(int id, int amount) {

        return new Prop(id, amount, 0, 0, 0);
    }

}
