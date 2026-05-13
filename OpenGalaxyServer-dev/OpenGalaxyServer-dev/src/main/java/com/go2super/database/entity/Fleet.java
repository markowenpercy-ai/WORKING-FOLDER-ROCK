package com.go2super.database.entity;

import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.FleetInitiator;
import com.go2super.database.entity.sub.FleetMatch;
import com.go2super.database.entity.sub.FleetTransmission;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.game.TeamModel;
import com.go2super.obj.game.ViewTeamModel;
import com.go2super.service.BattleService;
import com.go2super.service.CommanderService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.battle.Match;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Document(collection = "game_fleets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fleet implements Serializable {

    @Id
    private ObjectId id;

    private int shipTeamId;
    private int galaxyId;

    @Column(unique = true)
    private int guid;
    private String name;

    private int commanderId;
    private int he3;

    private int bodyId;
    private int rangeType;
    private int preferenceType;

    private int posX;
    private int posY;

    private int direction;
    private boolean match;

    private double additionalGrowth;
    private boolean forceTechs;

    private ShipTeamBody fleetBody;
    private FleetMatch fleetMatch;

    private FleetTransmission fleetTransmission;
    private FleetInitiator fleetInitiator;

    public void remove() {

        PacketService.getInstance().getFleetCache().delete(this);
    }

    public void save() {

        PacketService.getInstance().getFleetCache().save(this);
    }

    public Commander getCommander() {

        return CommanderService.getInstance().getCommander(commanderId);
    }

    public int bodyId() {

        return getFleetBody().getBestHull().getId();
    }

    public int getMaxHe3() {

        int storage = 0;

        for (ShipTeamNum teamNum : fleetBody.cells) {

            if (teamNum.getShipModelId() < 0) {
                continue;
            }

            ShipModel model = PacketService.getShipModel(teamNum.getShipModelId());

            if (model == null) {
                continue;
            }

            storage += (teamNum.getNum() * model.getFuelStorage());

        }

        return storage; // todo

    }

    public int getSupply() {

        return (int) Math.ceil(getHe3Consumption());
    }

    public double getHe3Consumption() {

        double result = 0;

        for (ShipTeamNum teamNum : fleetBody.cells) {

            if (teamNum.getShipModelId() < 0) {
                continue;
            }

            ShipModel model = PacketService.getShipModel(teamNum.getShipModelId());

            if (model == null) {
                continue;
            }

            double usage = (teamNum.getNum() * model.getFuelUsage());
            result += usage;

        }


        return result; // todo

    }

    public double getTransmissionRate() {

        double result = -1;

        for (ShipTeamNum teamNum : fleetBody.cells) {

            if (teamNum.getShipModelId() < 0) {
                continue;
            }

            ShipModel model = PacketService.getShipModel(teamNum.getShipModelId());
            if (model == null) {
                continue;
            }

            double rate = model.getTransmissionRate();
            if (result == -1 || rate > result) {
                result = rate;
            }

        }

        return result == -1 ? 0 : result;

    }

    public double getTransmissionStart() {

        double result = -1;

        for (ShipTeamNum teamNum : fleetBody.cells) {

            if (teamNum.getShipModelId() < 0) {
                continue;
            }

            ShipModel model = PacketService.getShipModel(teamNum.getShipModelId());
            if (model == null) {
                continue;
            }

            double start = model.getTransmissionStart();
            if (result == -1 || start > result) {
                result = start;
            }

        }

        return result == -1 ? 0 : result;

    }

    public int ships() {

        int number = 0;
        for (ShipTeamNum cell : fleetBody.getCells()) {
            if (cell.getShipModelId() >= 0) {
                number += cell.getNum();
            }
        }
        return number;
    }

    public ViewTeamModel getViewTeamModel() {

        return getFleetBody().getViewTeamModel();
    }

    public TeamModel getTeamModel() {

        return getFleetBody().getTeamModel();
    }

    public BattleFleet getBattleFleet() {

        Match match = getCurrentMatch();
        if (match == null) {
            return null;
        }

        BattleFleet result = match.getBattleFleetByShipTeamId(shipTeamId);

        return result;

    }

    public User getUser() {

        User user = UserService.getInstance().getUserCache().findByGuid(this.getGuid());
        return user;
    }

    public Match getCurrentMatch() {

        Match match = BattleService.getInstance().getCurrent(this);
        return match;
    }

    public boolean isInTransmission() {

        return getFleetTransmission() != null;
    }

    public boolean isInMatch() {

        return getCurrentMatch() != null;
    }

}
