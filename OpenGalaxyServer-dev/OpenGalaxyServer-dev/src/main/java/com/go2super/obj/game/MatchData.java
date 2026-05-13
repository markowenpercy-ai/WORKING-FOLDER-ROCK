package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.VariableType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchData extends BufferObject {
    private String name;
    private int matchWin;
    private int matchLost;
    private int matchDogFail;
    private int matchResult;
    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addString(SmartString.of(name, VariableType.MAX_NAME));
        go2buffer.addChar(matchWin);
        go2buffer.addChar(matchLost);
        go2buffer.addChar(matchDogFail);
        go2buffer.addChar(matchResult);

    }

    @Override
    public MatchData trash() {
        return new MatchData("", -1, -1, -1, -1);
    }
}