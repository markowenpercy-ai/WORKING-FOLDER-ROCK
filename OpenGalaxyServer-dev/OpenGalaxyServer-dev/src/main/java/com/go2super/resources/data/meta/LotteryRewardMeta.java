package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LotteryRewardMeta extends JsonData {

    private String type;

    private int id;
    private int[] ids;

    private boolean lock;
    private int amount;

    public int pickOne() {

        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < ids.length; i++) {
            list.add(ids[i]);
        }

        Collections.shuffle(list);
        return list.get(0);

    }

}
