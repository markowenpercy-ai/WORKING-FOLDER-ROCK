package com.go2super.database.entity.sub;

import com.go2super.database.entity.sub.battle.BattleActionTrigger;
import com.go2super.service.battle.MatchRunnable;
import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class BattleAction {

    private int actionId;

    private String type;
    private int involvedId;

    private LinkedList<BattleActionTrigger> triggers;

    private List<MatchRunnable.AreaEffect> areaEffects;

    public void addTrigger(BattleActionTrigger trigger) {

        triggers.add(trigger);
    }

    public int calculateNextTriggerId() {

        return triggers.size();
    }

    public void addAreaEffects(List<MatchRunnable.AreaEffect> areaEffect) {
        this.areaEffects = areaEffect;
    }

    public void addAreaEffect(MatchRunnable.AreaEffect areaEffect) {
        this.areaEffects.add(areaEffect);
    }

}
