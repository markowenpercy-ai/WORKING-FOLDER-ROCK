package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.BattleElementType;
import com.go2super.service.battle.BattleCell;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.pathfinder.GO2Node;
import lombok.*;

import java.io.Serializable;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public abstract class BattleElement implements Serializable {

    public BattleElementType type;

    public BattleElement(BattleElementType type) {

        this.type = type;
    }

    public BattleCell getCell(BattleCell[][] cells) {

        return cells[getPosX()][getPosY()];
    }

    public Node getNode() {

        return new Node(getPosX(), getPosY());
    }

    public GO2Node getGO2Node() {

        return new GO2Node(getPosX(), getPosY());
    }

    public abstract int getPosX();

    public abstract int getPosY();

    public abstract boolean canAttack(BattleElement target);

}
