package com.go2super.socket.data;

import com.go2super.buffer.Go2Buffer;

public interface IncomingPacket {

    void process(int size, int id, Go2Buffer packet);

}
