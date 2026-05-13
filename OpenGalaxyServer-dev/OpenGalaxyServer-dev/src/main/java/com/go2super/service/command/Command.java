package com.go2super.service.command;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.ChatService;
import lombok.Data;

import java.io.IOException;

@Data
public abstract class Command {

    private String command;
    private String[] permission;

    public Command(String command, String... permissions) {

        this.command = command;
        this.permission = permissions;
    }

    public abstract void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) throws IOException, InterruptedException;

    public void sendMessage(String message, User... users) {

        ChatService.getInstance().sendMessage(message, users);
    }

}
