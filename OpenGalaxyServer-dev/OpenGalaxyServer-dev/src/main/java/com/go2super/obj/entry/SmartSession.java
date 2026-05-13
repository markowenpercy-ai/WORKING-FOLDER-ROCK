package com.go2super.obj.entry;

import com.go2super.server.GameServerReceiver;
import lombok.Builder;
import lombok.Data;

import java.net.Socket;

@Data
@Builder
public class SmartSession {

    private Socket socket;
    private Thread thread;

    private GameServerReceiver serverReceiver;


}
