package com.go2super.service.battle;

import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.service.battle.calculator.ShipTechs;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class BattleFleetTeam implements Serializable {

    private List<BattleFleetCell> cells = new ArrayList<>();

    public ShipTeamBody getTeamBody(int direction) {

        ShipTeamBody teamBody = ShipTeamBody.builder().cells(new ArrayList<>()).build();

        for (BattleFleetCell cell : cells) {
            teamBody.getCells().add(cell.getTeamNum());
        }

        return teamBody;

    }

    public ShipTeamBody getTeamBody() {

        ShipTeamBody teamBody = ShipTeamBody.builder().cells(new ArrayList<>()).build();

        for (BattleFleetCell cell : cells) {
            teamBody.getCells().add(cell.getTeamNum());
        }

        return teamBody;

    }

    public List<BattleFleetCell> sortFleetByDirection(int currentDirection, int desiredDirection) {

        List<BattleFleetCell> result = new ArrayList<>(cells);

        for (int positionIndex = 0; positionIndex < 9; positionIndex++) {

            int currentPosition = BattleFleet.attackMatrix[currentDirection][positionIndex];
            int desiredPosition = BattleFleet.attackMatrix[desiredDirection][positionIndex];

            result.set(desiredPosition, cells.get(currentPosition));

        }

        return result;

    }

    public static BattleFleetTeam fromShipTeamBody(int guid, ShipTeamBody body, BattleCommander commander, ShipTechs techs) {

        BattleFleetTeam team = new BattleFleetTeam();

        for (ShipTeamNum num : body.getCells()) {
            team.getCells().add(BattleFleetCell.getByNum(guid, num, commander, techs));
        }

        return team;

    }

}
