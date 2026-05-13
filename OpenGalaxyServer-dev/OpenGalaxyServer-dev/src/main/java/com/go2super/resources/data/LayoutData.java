package com.go2super.resources.data;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.EnemyStatsMeta;
import com.go2super.resources.data.meta.LayoutCommanderMeta;
import com.go2super.resources.data.meta.LayoutDesignMeta;
import com.go2super.resources.localization.Localization;
import com.go2super.service.BattleService;
import com.go2super.service.PacketService;
import com.go2super.service.battle.BattleFleetTeam;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.calculator.ShipTechs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LayoutData extends JsonData {

    private String name;

    private LinkedList<LayoutDesignMeta> layout;
    private LayoutCommanderMeta commander;

    private String targetPriority;
    private String targetRange;

    public BattleFleet getBattleFleet(boolean save, ShipTechs techs, EnemyStatsMeta stats) {

        BattleFleet battleFleet = new BattleFleet();
        ShipTeamBody teamBody = getTeamBody();

        battleFleet.setName(Localization.EN_US.get(name));
        battleFleet.setBodyId(teamBody.getRenderedBody());

        int maxHe3 = getMaxHe3();

        battleFleet.setHe3(maxHe3);
        battleFleet.setMaxHe3(maxHe3);

        battleFleet.setMaxRounds(10);
        battleFleet.setJoinRound(0);
        battleFleet.setShipTeamId(-1);
        battleFleet.setDirection(0);
        battleFleet.setGuid(-1);
        battleFleet.setTarget(BattleService.getTarget(targetRange));
        battleFleet.setTargetInterval(BattleService.getTargetInterval(targetPriority));
        battleFleet.setBattleCommander(commander.getBattleCommander(save, stats));
        battleFleet.setTeam(BattleFleetTeam.fromShipTeamBody(-1, teamBody, battleFleet.getBattleCommander(), techs));
        battleFleet.setTechs(techs); // todo have to change this

        return battleFleet;

    }

    public ShipTeamBody getTeamBody() {

        ShipTeamBody body = new ShipTeamBody();
        int[][] segmentation = MatchRunnable.segmentedMatrix[3];

        body.setCells(Arrays.asList(new ShipTeamNum[9]));
        int fixedPos = 0;

        for (int segmentedIndex = 0; segmentedIndex < segmentation.length; segmentedIndex++) {
            for (int index = 0; index < segmentation[segmentedIndex].length; index++) {

                int position = segmentation[segmentedIndex][index];
                LayoutDesignMeta design = layout.get(position);

                if (design == null) {
                    body.getCells().set(fixedPos++, new ShipTeamNum(-1, 0));
                    continue;
                }

                ShipModel model = design.getModel();
                body.getCells().set(fixedPos++, new ShipTeamNum(model.getShipModelId(), design.getAmount()));

            }
        }

        return body;

    }

    public int getMaxHe3() {

        int storage = 0;

        for (ShipTeamNum teamNum : getTeamBody().cells) {

            if (teamNum.getShipModelId() < 0) {
                continue;
            }

            ShipModel model = PacketService.getShipModel(teamNum.getShipModelId());

            if (model == null) {
                continue;
            }

            storage += (teamNum.getNum() * model.getFuelStorage());

        }

        return storage;

    }

}
