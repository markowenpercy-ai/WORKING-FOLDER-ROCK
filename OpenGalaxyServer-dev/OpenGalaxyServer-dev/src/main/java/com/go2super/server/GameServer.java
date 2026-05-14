package com.go2super.server;

import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.entry.SmartSession;
import com.go2super.packet.PacketRouter;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

@Data
public class GameServer extends SmartServer implements Runnable {

    public static CopyOnWriteArrayList<SmartSession> clients = new CopyOnWriteArrayList<>();

    private ServerSocket serverSocket;
    private PacketRouter packetRouter;

    private long last = 0;

    private Socket socket;
    private IoBuffer buffer;

    @SneakyThrows
    public GameServer(int port) {

        super(port);

        serverSocket = new ServerSocket(port);
        packetRouter = new PacketRouter();

        packetRouter.craftPackets();
        packetRouter.craftListeners();

    }

    @SneakyThrows
    @Override
    public void run() {

        if (getServerSocket() != null) {

            long timeout = 1000L;
            long accepted = 0L;

            while (true) {

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

                if (getInputStream().available() == 23) {

                    buffer = IoBuffer.allocate(90);
                    buffer.putString("<cross-domain-policy><allow-access-from domain='*' to-ports='*' /> </cross-domain-policy>", StandardCharsets.UTF_8.newEncoder());
                    getOutputStream().write(buffer.array());
                    continue;
                }
                GameServerReceiver serverReceiver = new GameServerReceiver(socket, packetRouter, this);

                Thread serverReceiverThread = new Thread(serverReceiver);
                serverReceiverThread.setName("game-receiver-generic");
                serverReceiverThread.start();

                clients.add(SmartSession.builder()
                        .serverReceiver(serverReceiver)
                        .thread(serverReceiverThread)
                        .socket(socket)
                        .build());

            }

        }

    }

    @Override
    public void close() {

        if (getThread() != null) {
            getThread().interrupt();
        }

        BotLogger.thread("[GameServer] User thread closed!");
        // Thread.currentThread().stop();

    }

}

