package com.go2super.server;

import com.go2super.buffer.Go2Buffer;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.entry.SmartSession;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.PacketRouter;
import com.go2super.service.LoginService;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.mina.core.buffer.IoBuffer;

import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Data
public class GameServerReceiver extends SmartServer implements Runnable {

    private String receiverId;

    private PacketRouter packetRouter;
    private int currentPacketId = 0;

    private String accountId;
    private String accountName;
    private String accountEmail;

    private String hostname;
    private String ip;
    private int port;

    private long userId = -1;
    private int guid = -1;
    private double userMaxPpt = 0.0d;

    private String discordId;
    private Date loginTime;
    private long last = 0;

    private long packetCount;
    private double ppt;

    private long nextReset;

    private long now;
    private long diff;

    private int size;
    private int type;

    private IoBuffer buffer;
    private Go2Buffer current;
    private final int TIMEOUT = 1800000;
    @SneakyThrows
    public GameServerReceiver(Socket socket, PacketRouter packetRouter, GameServer gameServer) {

        super(socket, gameServer.getPort());

        setSocket(socket);
        setInputStream(new LittleEndianDataInputStream(socket.getInputStream()));
        setOutputStream(new LittleEndianDataOutputStream(socket.getOutputStream()));

        this.receiverId = UUID.randomUUID().toString();
        this.packetRouter = packetRouter;

    }

    @SneakyThrows
    @Override
    public void run() {

        try {
            Socket socket = getSocket();
            BotLogger.dev("User thread opened!");
            last = System.currentTimeMillis();
            while (!socket.isClosed() && socket.isConnected()) {
                tick();
            }
        } catch (Exception e) {
            BotLogger.error(e);
        } finally {
            close();
        }
    }

    @SneakyThrows
    public void tick() {
        try {

            size = getInputStream().readUnsignedShort();
            type = getInputStream().readUnsignedShort();

        } catch (Exception e) {
            BotLogger.error(e);
            close();
            return;

        }

        if (!PacketRouter.getInstance().containsPacket(type)) {
            if (type > 0 && type < 5000) {
                BotLogger.error("⚠ NOT FOUND " + type);
            }
            return;
        }

        buffer = IoBuffer.allocate(size).setAutoExpand(true);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putUnsignedShort(size);
        buffer.putUnsignedShort(type);
        buffer.put(getInputStream().readNBytes(size - 4));
        buffer.position(0);

        setCurrentPacketId(getCurrentPacketId() + 1);
        current = new Go2Buffer(buffer, true);

        try {
            packetRouter.playPacket(size, type, current, getSocket(), this);
        } catch (BufferUnderflowException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public void close() {

        // System.out.println("zxd");

        if (!getSocket().isClosed()) {
            getSocket().close();
        }

        if (getThread() != null) {
            getThread().interrupt();
        }

        Optional<SmartSession> optionalSession = GameServer.clients.stream().filter(session -> session.getServerReceiver().getReceiverId().equals(getReceiverId())).findFirst();
        optionalSession.ifPresent(smartSession -> GameServer.clients.remove(smartSession));

        BotLogger.thread("[GameServerReceiver] User thread closed! (GUID: " + guid + ")");
        Optional<LoggedGameUser> optionalGameUser = LoginService.getInstance().getGame(guid);

        // Optional<User> user = Optional.empty();
        // Optional<String> username = Optional.empty();

        // ? Disconnect user!
        // User updatedUser = optionalGameUser.get().getUpdatedUser();
        // if(updatedUser != null) {
        //     user = Optional.ofNullable(updatedUser);
        //     username = Optional.ofNullable(updatedUser.getUsername());
        // }
        optionalGameUser.ifPresent(loggedGameUser -> LoginService.getInstance().disconnectGame(loggedGameUser));

        // Optional<String> finalUsername = username;
        // Optional<User> finalUser = user;

        // UserActionLog action = UserActionLog.builder()
        //        .action("user-logout")
        //        .message("[Logout] " + finalUsername.orElse("unknown") + " (IP: " + ip + ":" + port + ", Guid: " + guid + ", UserId: " + userId + ")")
        //        .build();

        // if(finalUser.isPresent()) EventLogger.sendUserAction(action, finalUser.get(), this);
        // else EventLogger.send(action, this);

        // try {
        //     Thread.currentThread().stop();
        // } catch(ThreadDeath death) {
        //     death.printStackTrace();
        // }

    }

}
