package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.VariableType;
import lombok.Data;

@Data
public class ConsortiaJobName extends BufferObject {

    private String name0;
    private String name1;
    private String name2;
    private String name3;
    private String name4;

    public ConsortiaJobName(String name0, String name1, String name2, String name3, String name4) {

        this.name0 = name0;
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.name4 = name4;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addString(name0, VariableType.MAX_NAME);
        go2buffer.addString(name1, VariableType.MAX_NAME);
        go2buffer.addString(name2, VariableType.MAX_NAME);
        go2buffer.addString(name3, VariableType.MAX_NAME);
        go2buffer.addString(name4, VariableType.MAX_NAME);

    }

    @Override
    public void read(Go2Buffer go2Buffer) {

        go2Buffer.getByte((4 - go2Buffer.getBuffer().position() % 4) % 4);

        name0 = go2Buffer.getString(VariableType.MAX_NAME);
        name1 = go2Buffer.getString(VariableType.MAX_NAME);
        name2 = go2Buffer.getString(VariableType.MAX_NAME);
        name3 = go2Buffer.getString(VariableType.MAX_NAME);
        name4 = go2Buffer.getString(VariableType.MAX_NAME);

    }

    @Override
    public ConsortiaJobName trash() {

        return new ConsortiaJobName("", "", "", "", "");
    }

}
