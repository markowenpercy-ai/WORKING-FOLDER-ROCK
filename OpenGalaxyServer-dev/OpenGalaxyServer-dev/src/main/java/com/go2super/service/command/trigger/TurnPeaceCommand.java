package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.ResourcePlanet;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.service.GalaxyService;
import com.go2super.service.command.Command;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TurnPeaceCommand extends Command {

    private static final String pattern = "MM-dd-yyyy HH:mm:ss";
    private static final DateFormat dateFormat = new SimpleDateFormat(pattern);

    public TurnPeaceCommand() {

        super("turnpeace", "permission.turnpeace", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'turnpeace' need more arguments! (example: /turnpeace 31 149)", user);
            return;

        }

        int posX = Integer.parseInt(parts[1]);
        int posY = Integer.parseInt(parts[2]);

        Planet planet = GalaxyService.getInstance().getPlanet(new GalaxyTile(posX, posY));
        if (planet == null) {

            sendMessage("The planet doesn't exists!", user);
            return;

        }

        if (planet.getType() == PlanetType.USER_PLANET) {

            sendMessage("Invalid planet type!", user);
            return;

        }

        if (planet.isInWar()) {

            sendMessage("The planet is in war!", user);
            return;

        }

        if (planet instanceof ResourcePlanet resourcePlanet) {

            resourcePlanet.setPeace(!resourcePlanet.isPeace());

            GalaxyService.getInstance().getPlanetCache().save(resourcePlanet);

            sendMessage("The planet is now in " + (resourcePlanet.isPeace() ? "peace" : "war") + " phase!", user);

        }

    }

}
