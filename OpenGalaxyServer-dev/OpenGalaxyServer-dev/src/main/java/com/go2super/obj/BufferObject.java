package com.go2super.obj;

import com.go2super.buffer.Go2Buffer;
import com.go2super.service.exception.UnimplementedBufferReader;

public abstract class BufferObject {

    public BufferObject trash() throws UnimplementedBufferReader {

        throw new UnimplementedBufferReader();
    }

    public void read(Go2Buffer buffer) throws UnimplementedBufferReader {

        throw new UnimplementedBufferReader();
    }

    public void write(Go2Buffer buffer) throws UnimplementedBufferReader {

        throw new UnimplementedBufferReader();
    }

}
