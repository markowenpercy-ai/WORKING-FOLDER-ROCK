package com.go2super.service;

import com.go2super.server.GameLogin;
import com.go2super.server.GameServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SocketService {

    private static SocketService instance;
    private static final AtomicBoolean startupStarted = new AtomicBoolean(false);

    private Thread loginThread;
    private Thread gameThread;

    @Value("${application.game.server-port:5150}")
    private int gamePort;

    @Value("${application.game.login-port:5050}")
    private int loginPort;

    public SocketService() {

        instance = this;
    }

    private static final Object startupLock = new Object();

    public void startSockets() {
        log.warn("startSockets() called - startupStarted={}", startupStarted.get());
        
        if (!startupStarted.compareAndSet(false, true)) {
            log.warn("Socket startup already in progress, skipping");
            return;
        }

        log.info("Starting socket initialization thread");
        Thread startupThread = new Thread(() -> {
            try {
                GameLogin login = new GameLogin(loginPort);
                GameServer game = new GameServer(gamePort);

                loginThread = new Thread(login);
                gameThread = new Thread(game);

                loginThread.setName("game-login-thread");
                gameThread.setName("game-server-thread");

                loginThread.start();
                gameThread.start();
                
                log.info("Game sockets started successfully on ports {} and {}", gamePort, loginPort);
            } catch (Exception e) {
                log.error("Failed to start game sockets: {}", e.getMessage(), e);
            }
        });
        
        startupThread.setName("socket-startup-thread");
        startupThread.start();
    }

    public Thread getLoginThread() {

        return loginThread;
    }

    public Thread getGameThread() {

        return gameThread;
    }

    public static SocketService getInstance() {

        return instance;
    }

}
