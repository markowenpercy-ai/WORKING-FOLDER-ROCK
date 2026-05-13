package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import lombok.Data;

@Data
public class MapArea extends BufferObject {

    private SmartString consortiaName = SmartString.of(32);
    private SmartString userName = SmartString.of(32);

    private long userId;

    private int galaxyId;
    private int reserve;

    private char starFace;
    private char insertFlagStatus;

    private int consortiaHeadId;
    private int consortiaLevelId;

    private char type;

    /*
     * 0: normal
     * 1: in war
     * 2: in truce
     */
    private char fightFlag;

    /*
     * 0: normal
     * 1: planet
     * 2: humaroid
     * 3: rbp
     */
    private char camp;
    private char spaceLevelId;

    public MapArea(String consortiaName, String userName, long userId, int galaxyId, int reserve, int starFace, int insertFlagStatus, int consortiaHeadId, int consortiaLevelId, int type, int fightFlag, int camp, int spaceLevelId) {

        this.consortiaName.value(consortiaName);
        this.userName.value(userName);

        this.userId = userId;
        this.galaxyId = galaxyId;
        this.reserve = reserve;

        this.starFace = (char) starFace;
        this.insertFlagStatus = (char) insertFlagStatus;

        this.consortiaHeadId = consortiaHeadId;
        this.consortiaLevelId = consortiaLevelId;

        this.type = (char) type;
        this.fightFlag = (char) fightFlag;
        this.camp = (char) camp;
        this.spaceLevelId = (char) spaceLevelId;


    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addString(consortiaName.getValue(), consortiaName.getSize());
        go2buffer.addString(userName.getValue(), userName.getSize());

        go2buffer.addLong(userId);
        go2buffer.addInt(galaxyId);
        go2buffer.addInt(reserve);

        go2buffer.addChar(starFace);
        go2buffer.addChar(insertFlagStatus);

        go2buffer.addUnsignChar(consortiaHeadId);
        go2buffer.addUnsignChar(consortiaLevelId);

        go2buffer.addChar(type);
        go2buffer.addChar(fightFlag);
        go2buffer.addChar(camp);
        go2buffer.addChar(spaceLevelId);

    }

}
