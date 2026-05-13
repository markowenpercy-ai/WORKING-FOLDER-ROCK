package com.go2super.socket.data;

import org.apache.mina.core.buffer.IoBuffer;

public interface IncomingBuffer {

    void process(int type, IoBuffer buffer);

}
