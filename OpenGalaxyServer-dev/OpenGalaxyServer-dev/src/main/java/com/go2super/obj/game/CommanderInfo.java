package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommanderInfo extends BufferObject {

    private int commanderId;
    private int state;

    public CommanderInfo(int commanderId, int state) {

        this.commanderId = commanderId;
        this.state = state;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(commanderId);
        go2buffer.addInt(state);

    }

    @Override
    public CommanderInfo trash() {

        return new CommanderInfo(0, 0);
    }

}
