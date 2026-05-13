package com.go2super.socket.data;

import java.util.*;

public interface DiscordConsumer {

    void send(Map<String, Object> packet);

}
