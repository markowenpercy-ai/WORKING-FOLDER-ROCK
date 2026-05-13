package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.IGLService;
import com.go2super.service.RaidsService;
import com.go2super.service.command.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;

public class FeatureCommand extends Command {
    public FeatureCommand() {
        super("feature", "permission.feature");
    }

    @AllArgsConstructor
    @Getter
    enum FeatureType {
        IGL("igl"),
        RAIDS("raids");
        private final String name;

        public static FeatureType fromName(String name) {
            for (FeatureType type : FeatureType.values()) {
                if (type.getName().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) throws IOException, InterruptedException {
        if (parts.length != 3) {
            sendMessage("Please specify a feature ", user);
            return;
        }
        String feature = parts[1];
        FeatureType featureType = FeatureType.fromName(feature);
        if (featureType == null) {
            sendMessage("Feature not found", user);
            return;
        }
        String state = parts[2];

        switch (featureType) {
            case IGL -> {
                if (state.equalsIgnoreCase("on")) {
                    IGLService.getInstance().getEnabled().getAndSet(true);
                    sendMessage("IGL enabled", user);
                    return;
                }
                if (state.equalsIgnoreCase("off")) {
                    IGLService.getInstance().getEnabled().getAndSet(false);
                    sendMessage("IGL disabled", user);
                    return;
                }
                sendMessage("Please specify a state ", user);
            }
            case RAIDS -> {
                if (state.equalsIgnoreCase("on")) {
                    RaidsService.getInstance().getEnabled().getAndSet(true);
                    sendMessage("Raids enabled", user);
                    return;
                }
                if (state.equalsIgnoreCase("off")) {
                    RaidsService.getInstance().getEnabled().getAndSet(false);
                    sendMessage("Raids disabled", user);
                    return;
                }
            }
        }


    }
}
