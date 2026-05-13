package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.VariableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsortiaRank extends BufferObject {


    private SmartString name = SmartString.of(VariableType.MAX_NAME);

    private int consortiaId;
    private int rankId;
    private int throwWealth;

    private IntegerArray holdGalaxyArea = new IntegerArray(VariableType.MAX_CONSORTIAFIELD);

    private short reserveOne;

    private UnsignedChar headId = UnsignedChar.of(0);
    private UnsignedChar level = UnsignedChar.of(0);
    private UnsignedChar holdGalaxy = UnsignedChar.of(0);
    private UnsignedChar member = UnsignedChar.of(0);
    private UnsignedChar maxMember = UnsignedChar.of(0);

    private char reserveTwo;

    public ConsortiaRank(String name, int consortiaId, int rankId, int throwWealth, IntegerArray holdGalaxyArea, short reserveOne, char headId, char level, char holdGalaxy, char member, char maxMember, char reserveTwo) {

        this.name.setValue(name);

        this.consortiaId = consortiaId;
        this.rankId = rankId;
        this.throwWealth = throwWealth;

        this.holdGalaxyArea = holdGalaxyArea;

        this.reserveOne = reserveOne;

        this.headId.setValue(headId);
        this.level.setValue(level);
        this.holdGalaxy.setValue(holdGalaxy);
        this.member.setValue(member);
        this.maxMember.setValue(maxMember);

        this.reserveTwo = reserveTwo;

    }

    @Override
    public void write(Go2Buffer go2Buffer) {

        go2Buffer.pushByte((4 - go2Buffer.getBuffer().position() % 4) % 4);

        go2Buffer.addString(name.noSpaces(), VariableType.MAX_NAME);

        go2Buffer.addInt(consortiaId);
        go2Buffer.addInt(rankId);
        go2Buffer.addInt(throwWealth);

        for (int value : holdGalaxyArea.getArray()) {
            go2Buffer.addInt(value);
        }

        go2Buffer.addShort(reserveOne);

        go2Buffer.addChar(headId.getValue());
        go2Buffer.addChar(level.getValue());
        go2Buffer.addChar(holdGalaxy.getValue());
        go2Buffer.addChar(member.getValue());
        go2Buffer.addChar(maxMember.getValue());

        go2Buffer.addChar(reserveTwo);

    }

    @Override
    public ConsortiaRank trash() {

        return new ConsortiaRank("", 0, 0, 0, new IntegerArray(VariableType.MAX_CONSORTIAFIELD), (short) 0, (char) 0, (char) 0, (char) 0, (char) 0, (char) 0, (char) 0);
    }

}
