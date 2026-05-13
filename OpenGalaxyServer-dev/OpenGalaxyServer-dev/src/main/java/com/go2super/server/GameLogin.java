package com.go2super.server;

import com.go2super.buffer.Go2Buffer;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.packet.PacketRouter;
import com.go2super.packet.login.PlayerLoginTolPacket;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

@Data
public class GameLogin extends SmartServer implements Runnable {

    private ServerSocket serverSocket;

    private long last = 0;

    private Socket socket;
    private IoBuffer buffer;
    private Go2Buffer current;

    @SneakyThrows
    public GameLogin(int port) {

        super(port);

        serverSocket = new ServerSocket(port);

    }

    @SneakyThrows
    @Override
    public void run() {

        if (getServerSocket() != null) {

            long timeout = 1000L;
            long accepted = 0L;

            while (true) {

                try {

                    socket = serverSocket.accept();
                    accepted = System.currentTimeMillis();

                    setInputStream(new LittleEndianDataInputStream(socket.getInputStream()));
                    setOutputStream(new LittleEndianDataOutputStream(socket.getOutputStream()));

                    while (socket.getInputStream().available() == 0 && System.currentTimeMillis() - accepted < timeout) {
                        Thread.sleep(10);
                    }
                    if (socket.getInputStream().available() == 0) {
                        continue;
                    }

                    int size = getInputStream().readUnsignedShort();
                    int type = getInputStream().readUnsignedShort();

                    if (size == 28732) { // String that contains "<policy-file-request/>"

                        IoBuffer buffer = IoBuffer.allocate(90);
                        buffer.putString("<cross-domain-policy><allow-access-from domain='*' to-ports='*' /> </cross-domain-policy>", StandardCharsets.UTF_8.newEncoder());
                        getOutputStream().write(buffer.array());
                        continue;

                    }

                    if (!PacketRouter.getInstance().containsPacket(type)) {
                        if (type > 0 && type < 5000) {
                            BotLogger.packet("⚠ NOT FOUND " + type);
                        }
                        continue;
                    }

                    if (type != PlayerLoginTolPacket.TYPE) {
                        BotLogger.dev("Invalid entry: " + type);
                        continue;
                    }

                    buffer = IoBuffer.allocate(size).setAutoExpand(true);
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    buffer.putUnsignedShort(size);
                    buffer.putUnsignedShort(type);
                    buffer.put(getInputStream().readNBytes(size - 4));
                    buffer.position(0);

                    current = new Go2Buffer(buffer, true);

                    try {

                        PacketRouter.getInstance().playPacket(size, type, current, getSocket(), this);

                    } catch (BufferUnderflowException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    BotLogger.error("Login server found error!" + e);
                }

            }


        }
    }

    @Override
    public void close() {

        if (getThread() != null) {
            getThread().interrupt();
        }

        BotLogger.thread("[GameLogin] User thread closed!");
        // Thread.currentThread().stop();

    }
}

