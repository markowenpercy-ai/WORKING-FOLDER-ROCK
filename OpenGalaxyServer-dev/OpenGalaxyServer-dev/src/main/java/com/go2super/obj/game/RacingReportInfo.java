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
public class RacingReportInfo extends BufferObject {

    private int type;
    private int time;
    private int reportDate;
    private int rankChange;
    private String username;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addInt(type);
        go2buffer.addUnsignInt(time);
        go2buffer.addInt(reportDate);
        go2buffer.addInt(rankChange);
        go2buffer.addString(username, 32);

    }

    @Override
    public RacingReportInfo trash() {

        return new RacingReportInfo();
    }

}