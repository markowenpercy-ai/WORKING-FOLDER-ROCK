package com.go2super.resources.data.meta;

import com.go2super.database.entity.sub.BattleFort;
import com.go2super.database.entity.type.SpaceFortType;
import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EnemyFortificationMeta extends JsonData {

    private int x;
    private int y;

    private int id;
    private int level;

    private String kind;

    public BattleFort toBattleFort(int fortId) {

        BattleFort fort = new BattleFort(SpaceFortType.getFortType(isCommon(), id), level - 1);

        fort.setDefender(false);
        fort.setFortId(fortId);
        fort.setPosX(x);
        fort.setPosY(y);

        fort.setGuid(-1);
        fort.setUserId(-1);

        return fort;

    }

    public boolean isCommon() {

        return kind.equals("common");
    }

}
