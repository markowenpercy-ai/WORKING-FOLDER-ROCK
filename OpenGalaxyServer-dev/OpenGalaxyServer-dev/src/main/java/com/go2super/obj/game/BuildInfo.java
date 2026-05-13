package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class BuildInfo extends BufferObject {

    public int spareTime;

    public int posX;
    public int posY;
    public int indexId;
    public int buildingId;

    public char levelId;

    public BuildInfo(int spareTime, short posX, short posY, short indexId, char buildingId, char levelId) {

        this.spareTime = spareTime;

        this.posX = posX;
        this.posY = posY;
        this.indexId = indexId;
        this.buildingId = buildingId;

        this.levelId = levelId;

    }

    public BuildInfo(int spareTime, int posX, int posY, int indexId, int buildingId, int levelId) {

        this.spareTime = spareTime;

        this.posX = posX;
        this.posY = posY;
        this.indexId = indexId;
        this.buildingId = buildingId;

        this.levelId = (char) levelId;

    }

    @Override
    public void read(Go2Buffer go2buffer) {

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(spareTime);

        go2buffer.addUnsignShort(posX);
        go2buffer.addUnsignShort(posY);
        go2buffer.addUnsignShort(indexId);
        go2buffer.addUnsignChar(buildingId);

        go2buffer.addChar(levelId);

    }

}
