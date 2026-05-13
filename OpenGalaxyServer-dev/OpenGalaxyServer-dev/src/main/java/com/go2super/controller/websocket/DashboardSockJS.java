package com.go2super.controller.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;

// @Controller
public class DashboardSockJS {

    private final SimpMessagingTemplate template;

    @Autowired
    DashboardSockJS(SimpMessagingTemplate template) {

        this.template = template;
    }

    @MessageMapping("/send/message")
    public void sendMessage(String message) throws Exception {

        System.out.println(message);
        this.template.convertAndSend("/message", message + " 1");
    }

}
