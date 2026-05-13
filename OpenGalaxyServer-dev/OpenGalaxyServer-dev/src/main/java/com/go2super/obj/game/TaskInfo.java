package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class TaskInfo extends BufferObject {

    private short taskId;
    private short num;

    private char type;
    private char levelId;
    private char completeFlag;
    private char gainFlag;

    public TaskInfo(short taskId, short num, char type, char levelId, char completeFlag, char gainFlag) {

        this.taskId = taskId;
        this.num = num;
        this.type = type;
        this.levelId = levelId;
        this.completeFlag = completeFlag;
        this.gainFlag = gainFlag;
    }

    public TaskInfo(int taskId, int num, int type, int levelId, int completeFlag, int gainFlag) {

        this.taskId = (short) taskId;
        this.num = (short) num;
        this.type = (char) type;
        this.levelId = (char) levelId;
        this.completeFlag = (char) completeFlag;
        this.gainFlag = (char) gainFlag;
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addShort(taskId);
        go2buffer.addShort(num);

        go2buffer.addChar(type);
        go2buffer.addChar(levelId);
        go2buffer.addChar(completeFlag);
        go2buffer.addChar(gainFlag);

    }

}
