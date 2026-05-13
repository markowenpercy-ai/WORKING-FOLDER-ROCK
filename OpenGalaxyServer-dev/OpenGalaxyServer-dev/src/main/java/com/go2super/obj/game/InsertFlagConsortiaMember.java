package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.VariableType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertFlagConsortiaMember extends BufferObject {

    private SmartString name = SmartString.of(VariableType.MAX_NAME);

    private int guid;
    private int galaxyId;

    private int assault;

    private short galaxyArea;

    private UnsignedChar levelId = UnsignedChar.of(0);

    private char job;

    public InsertFlagConsortiaMember(String name, int guid, int galaxyId, int assault, short galaxyArea, char levelId, char job) {

        this.name.setValue(name);

        this.guid = guid;
        this.galaxyId = galaxyId;

        this.assault = assault;

        this.galaxyArea = galaxyArea;

        this.levelId.setValue(levelId);

        this.job = job;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addString(this.name.noSpaces(), VariableType.MAX_NAME);

        go2buffer.addInt(guid);
        go2buffer.addInt(galaxyId);
        go2buffer.addInt(assault);
        go2buffer.addShort(galaxyArea);

        go2buffer.addChar(levelId.getValue());
        go2buffer.addChar(job);


    }

    @Override
    public InsertFlagConsortiaMember trash() {

        return new InsertFlagConsortiaMember("", -1, -1, -1, (short) -1, (char) -1, (char) -1);
    }

}