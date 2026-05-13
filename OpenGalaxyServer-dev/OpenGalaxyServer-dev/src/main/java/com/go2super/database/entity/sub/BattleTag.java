package com.go2super.database.entity.sub;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BattleTag {

    private String name;
    private int value;

    public static BattleTag of(String name, int value) {

        return new BattleTag(name, value);
    }

}
