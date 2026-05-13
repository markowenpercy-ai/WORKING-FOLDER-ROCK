package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.ExpertiseType;
import com.go2super.service.CommanderService;
import com.google.common.primitives.Chars;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommanderExpertise implements Serializable {

    private ExpertiseType ballistic = ExpertiseType.B;
    private ExpertiseType directional = ExpertiseType.B;
    private ExpertiseType missile = ExpertiseType.B;
    private ExpertiseType carrier = ExpertiseType.B;
    private ExpertiseType defend = ExpertiseType.B;

    private ExpertiseType frigate = ExpertiseType.B;
    private ExpertiseType cruiser = ExpertiseType.B;
    private ExpertiseType battleShip = ExpertiseType.B;

    public String getJZ() {

        return ballistic.name() + directional.name() + missile.name() + carrier.name() + defend.name() + frigate.name() + cruiser.name() + battleShip.name();
    }

    public double getWeaponModifier(String subType) {

        ExpertiseType selected = ExpertiseType.B;

        switch (subType) {

            case "ballistic":
                selected = this.ballistic;
                break;
            case "directional":
                selected = this.directional;
                break;
            case "missile":
                selected = this.missile;
                break;
            case "shipBased":
                selected = this.carrier;
                break;
            case "building":
                selected = this.defend;
                break;

        }

        switch (selected) {
            case S:
                return 0.3d;
            case A:
                return 0.1d;
            case C:
                return -0.1d;
            case D:
                return -0.3d;
        }

        return 0.0d;

    }

    public double getShipDamageModifier(String subType) {

        ExpertiseType selected = ExpertiseType.B;

        switch (subType) {
            case "frigate":
                selected = this.frigate;
                break;
            case "cruiser":
                selected = this.cruiser;
                break;
            case "battleship":
                selected = this.battleShip;
                break;
        }

        switch (selected) {
            case S:
                return 0.1d;
            case A:
                return 0.05d;
            case C:
                return -0.05d;
            case D:
                return -0.1d;
        }

        return 0.0d;

    }

    public double getShipDamageReductionModifier(String subType) {

        ExpertiseType selected = ExpertiseType.B;

        switch (subType) {
            case "frigate":
                selected = this.frigate;
                break;
            case "cruiser":
                selected = this.cruiser;
                break;
            case "battleship":
                selected = this.battleShip;
                break;
        }

        switch (selected) {
            case S:
            case A:
                return -0.1d;
            case C:
            case D:
                return 0.1d;
        }

        return 0.0d;

    }

    public static CommanderExpertise common() {

        CommanderExpertise expertise = new CommanderExpertise();
        List<Character> chars = Chars.asList(CommanderService.getInstance().getCommonExpertisePattern().toCharArray());

        Collections.shuffle(chars);

        expertise.setBallistic(ExpertiseType.getLiteral(chars.get(0)));
        expertise.setDirectional(ExpertiseType.getLiteral(chars.get(1)));
        expertise.setMissile(ExpertiseType.getLiteral(chars.get(2)));
        expertise.setCarrier(ExpertiseType.getLiteral(chars.get(3)));

        expertise.setDefend(ExpertiseType.getLiteral(chars.get(4)));
        expertise.setFrigate(ExpertiseType.getLiteral(chars.get(5)));
        expertise.setCruiser(ExpertiseType.getLiteral(chars.get(6)));
        expertise.setBattleShip(ExpertiseType.getLiteral(chars.get(7)));

        return expertise;

    }

}
