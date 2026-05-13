package com.go2super.socket.data;

import java.util.*;

public interface BasicCommand {

    void run(String[] args, Map<String, Object> packet);

}
