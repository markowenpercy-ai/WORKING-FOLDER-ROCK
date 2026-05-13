package com.go2super.resources.data;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.BattleFort;
import com.go2super.database.entity.sub.FleetInitiator;
import com.go2super.database.entity.sub.FleetMatch;
import com.go2super.database.entity.type.MatchType;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.InstanceType;
import com.go2super.obj.type.JumpType;
import com.go2super.obj.utility.GameCell;
import com.go2super.packet.ship.ResponseCreateShipTeamPacket;
import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.meta.EnemyFleetMeta;
import com.go2super.resources.data.meta.EnemyFortificationMeta;
import com.go2super.resources.data.meta.PlayerFleetMeta;
import com.go2super.resources.data.meta.RewardMeta;
import com.go2super.resources.data.meta.BonusRewardMeta;
import com.go2super.service.AutoIncrementService;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.battle.calculator.ShipTechs;
import com.go2super.service.battle.match.InstanceMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InstanceData extends JsonData {

    private int id;
    private String name;
    private int experienceGain;

    private BonusRewardMeta bonusReward;

    private List<RewardMeta> rewards;

    private List<PlayerFleetMeta> playerFleets;

    private List<EnemyFleetMeta> fleets;
    private List<EnemyFortificationMeta> fortifications;

    public RewardMeta pickOne() {

        List<RewardMeta> list = new ArrayList<>();

        for (RewardMeta rewardMeta : rewards) {
            for (int i = 0; i < rewardMeta.getWeight(); i++) {
                list.add(rewardMeta);
            }
        }

        Collections.shuffle(list);
        return list.get(0);

    }

    public List<BattleFort> getEnemyForts() {

        List<BattleFort> prefab = new ArrayList<>();
        int index = 0;

        if (fortifications != null) {
            for (EnemyFortificationMeta meta : fortifications) {

                BattleFort battleFort = meta.toBattleFort(index);
                prefab.add(battleFort);

                index++;

            }
        }

        return prefab;

    }

    public synchronized void generatePirateEnemies(int galaxyId, boolean attacker, boolean techs) {

        List<BattleFleet> prefab = new ArrayList<>();
        prefabWarInstance(galaxyId, prefab, attacker, techs);

    }

    public synchronized List<BattleFleet> getEnemyFleets(InstanceMatch match) {

        List<BattleFleet> prefab = new ArrayList<>();

        if (match.getType() == InstanceType.INSTANCE) {
            prefabNormalInstance(match.getId(), prefab);
        } else if (match.getType() == InstanceType.RESTRICTED) {
            prefabNormalInstance(match.getId(), prefab);
        } else if (match.getType() == InstanceType.TRIALS) {
            prefabTrialsInstance(match.getId(), prefab);
        } else if (match.getType() == InstanceType.CONSTELLATION) {
            prefabConstellationInstance(match.getId(), prefab);
        }

        return prefab;

    }

    private synchronized void prefabTrialsInstance(String matchId, List<BattleFleet> prefab) {

        List<GameCell> cells = new ArrayList<>();

        for (int i = 10; i < 15; i++) {
            for (int j = 10; j < 15; j++) {
                cells.add(GameCell.of(i, j));
            }
        }

        Collections.shuffle(cells);
        int cellIndex = 0;

        List<ResearchData> allResearchData = new ArrayList<>();

        for (ResearchData data : ResourceManager.getScience().getWeapons()) {
            allResearchData.add(data);
        }
        for (ResearchData data : ResourceManager.getScience().getDefense()) {
            allResearchData.add(data);
        }

        ShipTechs shipTechs = new ShipTechs();
        shipTechs.passData(allResearchData);

        for (EnemyFleetMeta meta : fleets) {

            GameCell randomCell = cells.get(cellIndex++);

            LayoutData data = meta.getLayoutData();
            BattleFleet battleFleet = data.getBattleFleet(false, shipTechs, meta.getStats());

            battleFleet.setDefender(true);
            battleFleet.setPosX(randomCell.getX());
            battleFleet.setPosY(randomCell.getY());

            Fleet fleet = Fleet.builder()
                .shipTeamId(AutoIncrementService.getInstance().getNextFleetId())
                .bodyId(battleFleet.getBodyId())
                .galaxyId(-1)
                .fleetBody(data.getTeamBody())
                .name(battleFleet.getName())
                .commanderId(battleFleet.getBattleCommander().getCommanderId())
                .guid(-1)
                .rangeType(battleFleet.getTarget())
                .preferenceType(battleFleet.getTargetInterval())
                .posX(battleFleet.getPosX())
                .posY(battleFleet.getPosY())
                .match(true)
                .fleetMatch(FleetMatch.builder()
                    .match(matchId)
                    .matchType(MatchType.INSTANCE_MATCH)
                    .galaxyId(-1)
                    .build())
                .build();

            battleFleet.setTechs(shipTechs);
            battleFleet.setShipTeamId(fleet.getShipTeamId());
            prefab.add(battleFleet);

            PacketService.getInstance().getFleetCache().save(fleet);

        }

    }

    private synchronized void prefabWarInstance(int galaxyId, List<BattleFleet> prefab, boolean attacker, boolean techs) {

        List<GameCell> cells = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                if (i == 12 && j == 12) {
                    continue;
                } else {
                    cells.add(GameCell.of(i, j));
                }
            }
        }

        Collections.shuffle(cells);
        int cellIndex = 0;

        List<ResearchData> allResearchData = new ArrayList<>();

        if (techs) {

            for (ResearchData data : ResourceManager.getScience().getWeapons()) {
                allResearchData.add(data);
            }
            for (ResearchData data : ResourceManager.getScience().getDefense()) {
                allResearchData.add(data);
            }

        }

        ShipTechs shipTechs = new ShipTechs();
        shipTechs.passData(allResearchData);

        for (EnemyFleetMeta meta : fleets) {

            GameCell randomCell = cells.get(cellIndex++);

            LayoutData data = meta.getLayoutData();
            BattleFleet battleFleet = data.getBattleFleet(true, shipTechs, meta.getStats());

            battleFleet.setDefender(attacker);
            battleFleet.setPosX(randomCell.getX());
            battleFleet.setPosY(randomCell.getY());

            Fleet fleet = Fleet.builder()
                .shipTeamId(AutoIncrementService.getInstance().getNextFleetId())
                .bodyId(battleFleet.getBodyId())
                .galaxyId(galaxyId)
                .fleetBody(data.getTeamBody())
                .name(battleFleet.getName())
                .commanderId(battleFleet.getBattleCommander().getCommanderId())
                .he3((int) battleFleet.getHe3())
                .guid(-1)
                .rangeType(battleFleet.getTarget())
                .preferenceType(battleFleet.getTargetInterval())
                .posX(battleFleet.getPosX())
                .posY(battleFleet.getPosY())
                .match(true)
                .additionalGrowth(0.0d)
                .forceTechs(techs)
                .fleetMatch(FleetMatch.builder()
                    .match("pirates-match")
                    .matchType(MatchType.PIRATES_MATCH)
                    .galaxyId(galaxyId)
                    .build())
                .fleetInitiator(FleetInitiator.builder()
                    .jumpType(attacker ? JumpType.ATTACK : JumpType.DEFEND)
                    .build())
                .build();

            ResponseCreateShipTeamPacket response = new ResponseCreateShipTeamPacket();

            response.setGalaxyMapId(0);
            response.setGalaxyId(galaxyId);

            for (LoggedGameUser viewer : LoginService.getInstance().getPlanetViewers(galaxyId)) {

                GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

                fleetInfo.setShipTeamId(fleet.getShipTeamId());
                fleetInfo.setShipNum(fleet.ships());
                fleetInfo.setBodyId((short) fleet.getBodyId());
                fleetInfo.setReserve((short) 0);
                fleetInfo.setDirection((byte) 0);

                fleetInfo.setPosX((byte) fleet.getPosX());
                fleetInfo.setPosY((byte) fleet.getPosY());
                fleetInfo.setOwner((byte) BattleService.getInstance().getFleetColor(viewer, battleFleet));

                response.setGalaxyFleetInfo(fleetInfo);
                viewer.getSmartServer().send(response);

            }

            battleFleet.setTechs(shipTechs);
            battleFleet.setShipTeamId(fleet.getShipTeamId());
            prefab.add(battleFleet);

            PacketService.getInstance().getFleetCache().save(fleet);

        }

    }

    private synchronized void prefabNormalInstance(String matchId, List<BattleFleet> prefab) {

        for (EnemyFleetMeta meta : fleets) {

            LayoutData data = meta.getLayoutData();
            BattleFleet battleFleet = data.getBattleFleet(false, new ShipTechs(new ArrayList<>()), meta.getStats());

            battleFleet.setDefender(true);
            battleFleet.setPosX(meta.getX());
            battleFleet.setPosY(meta.getY());

            Fleet fleet = Fleet.builder()
                .shipTeamId(AutoIncrementService.getInstance().getNextFleetId())
                .bodyId(battleFleet.getBodyId())
                .galaxyId(-1)
                .fleetBody(data.getTeamBody())
                .name(battleFleet.getName())
                .commanderId(battleFleet.getBattleCommander().getCommanderId())
                .guid(-1)
                .rangeType(battleFleet.getTarget())
                .preferenceType(battleFleet.getTargetInterval())
                .posX(battleFleet.getPosX())
                .posY(battleFleet.getPosY())
                .match(true)
                .fleetMatch(FleetMatch.builder()
                    .match(matchId)
                    .matchType(MatchType.INSTANCE_MATCH)
                    .galaxyId(-1)
                    .build())
                .build();

            battleFleet.setShipTeamId(fleet.getShipTeamId());
            prefab.add(battleFleet);

            PacketService.getInstance().getFleetCache().save(fleet);

        }

    }

    private synchronized void prefabConstellationInstance(String matchId, List<BattleFleet> prefab) {

        List<ResearchData> allResearchData = new ArrayList<>();

        for (ResearchData data : ResourceManager.getScience().getWeapons()) {
            allResearchData.add(data);
        }
        for (ResearchData data : ResourceManager.getScience().getDefense()) {
            allResearchData.add(data);
        }

        ShipTechs shipTechs = new ShipTechs();
        shipTechs.passData(allResearchData);

        for (EnemyFleetMeta meta : fleets) {

            LayoutData data = meta.getLayoutData();
            BattleFleet battleFleet = data.getBattleFleet(false, shipTechs, meta.getStats());


            battleFleet.setDefender(true);
            battleFleet.setPosX(meta.getX());
            battleFleet.setPosY(meta.getY());

            Fleet fleet = Fleet.builder()
                .shipTeamId(AutoIncrementService.getInstance().getNextFleetId())
                .bodyId(battleFleet.getBodyId())
                .galaxyId(-1)
                .fleetBody(data.getTeamBody())
                .name(battleFleet.getName())
                .commanderId(battleFleet.getBattleCommander().getCommanderId())
                .guid(-1)
                .rangeType(battleFleet.getTarget())
                .preferenceType(battleFleet.getTargetInterval())
                .posX(battleFleet.getPosX())
                .posY(battleFleet.getPosY())
                .match(true)
                .fleetMatch(FleetMatch.builder()
                    .match(matchId)
                    .matchType(MatchType.INSTANCE_MATCH)
                    .galaxyId(-1)
                    .build())
                .build();

            battleFleet.setTechs(shipTechs);
            battleFleet.setShipTeamId(fleet.getShipTeamId());
            prefab.add(battleFleet);

            PacketService.getInstance().getFleetCache().save(fleet);

        }

    }

}
