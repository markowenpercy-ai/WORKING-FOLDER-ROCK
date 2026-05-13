package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.VariableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailInfo extends BufferObject {

    private SmartString subject = SmartString.of(VariableType.MAX_NAME);
    private SmartString name = SmartString.of(VariableType.MAX_NAME);

    private long dateTime;
    private long srcUserId;

    private int srcGuid;
    private int autoId;
    private int fightGalaxyId;

    private byte kind;
    private byte readFlag;
    private byte goodFlag;
    private byte titleType;

    public EmailInfo(String subject, String name, long dateTime, long srcUserId, int srcGuid, int autoId, int fightGalaxyId, byte kind, byte readFlag, byte goodFlag, byte titleType) {

        this.subject.value(subject);
        this.name.value(name);

        this.dateTime = dateTime;
        this.srcUserId = srcUserId;

        this.srcGuid = srcGuid;
        this.autoId = autoId;
        this.fightGalaxyId = fightGalaxyId;

        this.kind = kind;
        this.readFlag = readFlag;
        this.goodFlag = goodFlag;
        this.titleType = titleType;

    }

    @Override
    public void write(Go2Buffer go2Buffer) {

        go2Buffer.pushByte((8 - go2Buffer.getBuffer().position() % 8) % 8);

        go2Buffer.addString(this.subject.noSpaces(), VariableType.MAX_NAME);
        go2Buffer.addString(this.name.noSpaces(), VariableType.MAX_NAME);

        go2Buffer.addLong(this.dateTime);
        go2Buffer.addLong(this.srcUserId);

        go2Buffer.addInt(this.srcGuid);
        go2Buffer.addInt(this.autoId);
        go2Buffer.addInt(this.fightGalaxyId);

        go2Buffer.addByte(this.kind);
        go2Buffer.addByte(this.readFlag);
        go2Buffer.addByte(this.goodFlag);
        go2Buffer.addByte(this.titleType);

    }

    @Override
    public EmailInfo trash() {

        return new EmailInfo("", "", 0, 0, 0, 0, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
    }


}
