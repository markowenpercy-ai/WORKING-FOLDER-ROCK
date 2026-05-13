package com.go2super.database.entity.sub;

import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class BattleRound {

    private int roundId;

    private List<BattleFleet> fleets;
    private List<BattleFort> forts;

    private List<BattleAction> actions;

    public void addAction(BattleAction action) {

        actions.add(action);
    }

    public int calculateNextActionId() {

        return actions.size();
    }

}
