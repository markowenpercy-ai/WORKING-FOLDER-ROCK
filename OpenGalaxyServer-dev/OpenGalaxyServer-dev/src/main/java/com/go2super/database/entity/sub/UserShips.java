package com.go2super.database.entity.sub;

import com.go2super.database.entity.Fleet;
import com.go2super.obj.game.CreateShipInfo;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.service.PacketService;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserShips {

    public ArrayList<ShipTeamNum> ships = new ArrayList<>();
    public ArrayList<FactoryShip> factory = new ArrayList<>();

    public ArrayList<BruiseShip> repair = new ArrayList<>();
    public FactoryShip repairFactory;

    public ArrayList<ShipTeamNum> getShips() {

        if (ships == null) {
            ships = new ArrayList<>();
        }
        return ships;
    }

    public ArrayList<FactoryShip> getFactory() {

        if (factory == null) {
            factory = new ArrayList<>();
        }
        return factory;
    }

    public ShipTeamNum getShipTeamNum(int shipModelId) {

        for (ShipTeamNum shipTeamNum : ships) {
            if (shipTeamNum.getShipModelId() == shipModelId) {
                return shipTeamNum;
            }
        }
        return null;
    }

    public List<CreateShipInfo> getFactoryAsBuffer() {

        List<CreateShipInfo> infos = new ArrayList<>();

        for (FactoryShip factoryShip : getFactory()) {
            infos.add(factoryShip.packet());
        }

        return infos;

    }

    public void addRepair(int shipModelId, int num) {
        if (repair == null) {
            repair = new ArrayList<>();
        }

        Optional<BruiseShip> optionalBruiseShip = repair.stream().filter(ship -> ship.getShipModelId() == shipModelId).findFirst();
        if (optionalBruiseShip.isPresent()) {
            optionalBruiseShip.get().setNum(optionalBruiseShip.get().getNum() + num);
            return;
        }
        if (repair.size() < 10) {
            repair.add(new BruiseShip(shipModelId, num));
        }

    }

    public boolean hasDesign(int shipModel) {

        return getFactory().stream().anyMatch(factoryShip -> factoryShip.getShipModelId() == shipModel);
    }

    public boolean fabricate(int shipModel, int num, double buildTime) {

        if (getFactory().size() >= 5) {
            return false;
        }

        getFactory().add(FactoryShip.of(shipModel, num, buildTime));
        return true;

    }

    public boolean deleteRepairShip(int shipModelId) {
        return getRepair().removeIf(ship -> ship.getShipModelId() == shipModelId);
    }

    public BruiseShip getRepairShip(int shipModelId) {
        Optional<BruiseShip> optionalBruiseShip = repair.stream().filter(ship -> ship.getShipModelId() == shipModelId).findFirst();
        return optionalBruiseShip.orElse(null);
    }

    public boolean cancelRepair(int shipModelId) {
        if (getRepairFactory() == null || getRepairFactory().getShipModelId() != shipModelId) {
            return false;
        }
        addRepair(shipModelId, getRepairFactory().getNum());
        setRepairFactory(null);
        return true;
    }

    public void addShip(int shipModel, int num) {

        for (ShipTeamNum shipTeamNum : getShips()) {
            if (shipTeamNum.getShipModelId() == shipModel) {
                shipTeamNum.setNum(shipTeamNum.getNum() + num);
                return;
            }
        }
        getShips().add(new ShipTeamNum(shipModel, num));
    }

    public boolean removeShip(int shipModel, int num) {

        for (ShipTeamNum shipTeamNum : getShips()) {
            if (shipTeamNum.getShipModelId() == shipModel) {

                if (shipTeamNum.getNum() < num) {
                    return false;
                }

                shipTeamNum.setNum(shipTeamNum.getNum() - num);
                if (shipTeamNum.getNum() == 0) {
                    getShips().remove(shipTeamNum);
                }

                return true;

            }
        }

        return false;

    }

    public int countStoredShips() {

        int result = 0;

        for (ShipTeamNum team : getShips()) {
            result += team.getNum();
        }

        return result;

    }

    public int countTotalShips(int guid) {

        int result = 0;

        for (Fleet fleet : PacketService.getInstance().getFleetCache().findAllByGuid(guid)) {
            for (ShipTeamNum team : fleet.getFleetBody().getCells()) {
                if (team.getShipModelId() != -1) {
                    result += team.getNum();
                }
            }
        }

        return countStoredShips() + result;

    }

}
