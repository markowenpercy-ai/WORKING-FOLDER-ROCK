package com.go2super.obj.entry;

import com.go2super.buffer.Go2Buffer;
import com.go2super.logger.BotLogger;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.Packet;
import com.go2super.packet.chat.ChatMessagePacket;
import com.go2super.server.GameServerReceiver;
import com.go2super.service.JobService;
import com.go2super.service.jobs.TaskInfo;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import lombok.Data;
import lombok.SneakyThrows;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

@Data
public abstract class SmartServer {

    public static TaskInfo maxPeak = TaskInfo.builder().taskName("Unknown").time(0).build();

    private LinkedBlockingQueue<Go2Buffer> packetQueue = new LinkedBlockingQueue<>();
    private HashSet<Long> sentMoreInfos = new HashSet<>();

    private Socket socket;
    private int port;

    private LittleEndianDataInputStream inputStream;
    private LittleEndianDataOutputStream outputStream;

    private Thread thread;
    private boolean hadLoggedIn;

    public SmartServer(int port) {

        this(null, port);
    }

    public SmartServer(Socket socket, int port) {

        this.socket = socket;
        this.port = port;
        this.hadLoggedIn = false;
        this.thread = new Thread(() -> {
            while (true) {
                Go2Buffer go2Buffer = null;
                try {
                    go2Buffer = packetQueue.take();
                    outputStream.write(go2Buffer.getBuffer().array());
                    outputStream.flush();
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    BotLogger.error("Error sending packet: " + go2Buffer);
                    BotLogger.error(e);
                }
            }
        });

        this.thread.start();

    }

    public void sendMessage(String message) {

        ChatMessagePacket packet = new ChatMessagePacket();

        WideString buffer = WideString.of(message, 128);

        packet.setSeqId(-1);
        packet.setSrcUserId(0);
        packet.setObjUserId(0);
        packet.setGuid(-1);
        packet.setObjGuid(0);
        packet.setChannelType((short) 6);
        packet.setSpecialType((short) 0);
        packet.setPropsId(-1);
        packet.setCorpAcronym(WideString.of("", 64));
        packet.setName(WideString.of("", 32));
        packet.setToName(WideString.of("", 32));
        packet.setBuffer(buffer);
        packet.setCreationTime(0L);
        packet.setResponseTime(0L);

        send(packet);

    }

    public <E extends Packet> void send(List<E> packets) {

        for (Packet packet : packets) {
            send(packet.unmap());
            log(packet);
        }
    }

    public void send(Packet... packets) {

        for (Packet packet : packets) {
            send(packet.unmap());
            log(packet);
        }
    }

    public void send(Packet packet) {

        send(packet.unmap());
        log(packet);
    }

    public void log(Packet packet) {

        if (!(packet.getResponseTime() > Integer.MAX_VALUE)) {
            if (maxPeak.getTime() < packet.getResponseTime()) {
                maxPeak = TaskInfo.builder().taskName(packet.getClass().getSimpleName()).time(packet.getResponseTime()).build();
            }
        }

        if (!Arrays.asList(505, 504).contains(packet.getType())) {
            BotLogger.packet("↗ SEND " + packet.getType() + " " + packet.getClass().getSimpleName() + " [T:" + packet.getResponseTime() + "ms]");
        }

    }

    @SneakyThrows
    public void send(Go2Buffer buffer) {

        try {

            if (packetQueue.size() >= 100 && this instanceof GameServerReceiver serverReceiver) {
                // BotLogger.error("(ID = " + serverReceiver.getGuid() + ") Packet queue is full, dropping packet! [" + packetQueue.size() + "]");
                return;
            }

            packetQueue.add(buffer);

        } catch (Exception e) {

            BotLogger.error(e);
            close();

        }

    }

    public abstract void close();

}
